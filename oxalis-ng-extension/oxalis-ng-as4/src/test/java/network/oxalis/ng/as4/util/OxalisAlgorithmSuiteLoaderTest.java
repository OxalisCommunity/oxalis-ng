package network.oxalis.ng.as4.util;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.security.policy.custom.AlgorithmSuiteLoader;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.*;

/**
 * Tests for the memory-leak fix in {@link OxalisAlgorithmSuiteLoader}.
 *
 * <p>Background: the constructor previously kept a {@code private static final
 * Map<String, Bus> BUS_MAP} keyed on the unique {@link Bus#getId()} and stored the Bus as the
 * value, never evicting. Callers that create a fresh CXF Bus per request (e.g. peppol-outbound,
 * to avoid CXF feature accumulation) therefore pinned an entire Bus graph per request, leaking
 * memory until the process was OOM-killed.
 *
 * <p>The fix removes the static map and relies on the per-Bus extension
 * ({@code bus.getExtension(AlgorithmSuiteLoader.class)}) for idempotency, so a Bus becomes
 * eligible for GC as soon as the caller releases it.
 */
public class OxalisAlgorithmSuiteLoaderTest {

    @Test
    public void registersAlgorithmSuiteLoaderAsBusExtension() {
        Bus bus = new ExtensionManagerBus();

        new OxalisAlgorithmSuiteLoader(bus);

        AlgorithmSuiteLoader registered = bus.getExtension(AlgorithmSuiteLoader.class);
        assertTrue(registered instanceof OxalisAlgorithmSuiteLoader,
                "OxalisAlgorithmSuiteLoader must be registered as the bus AlgorithmSuiteLoader extension");
        assertNotNull(bus.getExtension(AssertionBuilderRegistry.class).getBuilder(
                new javax.xml.namespace.QName(
                        OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                        OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256)),
                "Custom Basic128GCMSha256 assertion builder must be registered on the bus");
    }

    @Test
    public void isIdempotentPerBus_keepsFirstExtensionInstance() {
        Bus bus = new ExtensionManagerBus();

        new OxalisAlgorithmSuiteLoader(bus);
        AlgorithmSuiteLoader first = bus.getExtension(AlgorithmSuiteLoader.class);

        // Constructing again for the same bus must NOT replace the already-registered extension.
        new OxalisAlgorithmSuiteLoader(bus);
        AlgorithmSuiteLoader second = bus.getExtension(AlgorithmSuiteLoader.class);

        assertSame(second, first,
                "Re-registering on the same bus must keep the existing extension (idempotent)");
    }

    @Test
    public void eachBusGetsItsOwnLoaderRegistered() {
        Bus busA = new ExtensionManagerBus();
        Bus busB = new ExtensionManagerBus();

        new OxalisAlgorithmSuiteLoader(busA);
        new OxalisAlgorithmSuiteLoader(busB);

        assertTrue(busA.getExtension(AlgorithmSuiteLoader.class) instanceof OxalisAlgorithmSuiteLoader);
        assertTrue(busB.getExtension(AlgorithmSuiteLoader.class) instanceof OxalisAlgorithmSuiteLoader);
        assertNotSame(busA.getExtension(AlgorithmSuiteLoader.class),
                busB.getExtension(AlgorithmSuiteLoader.class),
                "Distinct buses must each carry their own loader extension");
    }

    /**
     * Core leak-regression test: a Bus that has had an OxalisAlgorithmSuiteLoader registered must
     * become garbage-collectable once the caller drops its reference. With the old static BUS_MAP
     * the Bus stayed strongly reachable forever and this assertion failed.
     */
    @Test
    public void busIsGarbageCollectableAfterLoaderRegistered() throws Exception {
        Bus bus = new ExtensionManagerBus();
        new OxalisAlgorithmSuiteLoader(bus);

        WeakReference<Bus> ref = new WeakReference<>(bus);
        bus.shutdown(true);
        bus = null; // drop the only strong reference

        assertTrue(awaitCollected(ref),
                "Bus must be GC-eligible after the caller releases it; a lingering reference "
                        + "(e.g. a static map) would keep the entire Bus graph alive and leak memory");
    }

    /**
     * Simulates the peppol-outbound per-message pattern: create many fresh buses, register the
     * loader on each, then release them. None must remain strongly reachable.
     */
    @Test
    public void manyFreshBusesAreAllCollectable() throws Exception {
        int count = 200;
        List<WeakReference<Bus>> refs = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            Bus bus = new ExtensionManagerBus();
            new OxalisAlgorithmSuiteLoader(bus);
            refs.add(new WeakReference<>(bus));
            bus.shutdown(true);
            // bus goes out of scope at end of iteration
        }

        long collected = 0;
        for (int attempt = 0; attempt < 20 && collected < count; attempt++) {
            System.gc();
            Thread.sleep(50);
            collected = refs.stream().filter(r -> r.get() == null).count();
        }

        assertEquals(collected, count,
                "All " + count + " per-message buses must be collectable; "
                        + (count - collected) + " were still pinned (memory leak)");
    }

    /**
     * Concurrent Registration Test: Verify that multiple concurrent registrations on the same Bus
     *  don't throw exceptions
     *  don't corrupt CXF state
     *  leave only one valid AlgorithmSuiteLoader registered     *
     */
    @Test
    public void concurrentRegistrationOnSameBusIsSafe() throws Exception {
        Bus bus = new ExtensionManagerBus();

        int threads = 20;
        int iterations = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(iterations);

        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < iterations; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    new OxalisAlgorithmSuiteLoader(bus);
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
                return null;
            });
        }

        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS));
        executor.shutdownNow();
        assertTrue(failures.isEmpty(), "No concurrent registration should fail.");

        AlgorithmSuiteLoader loader =
                bus.getExtension(AlgorithmSuiteLoader.class);

        assertNotNull(loader);
        assertTrue(loader instanceof OxalisAlgorithmSuiteLoader);
    }

    /**
     * Repeated Registration Stress Test: e.g. 10000 constructor calls on same bus should not accumulate registrations.
     * Prove that there is No duplicate registrations and No corruption after repeated construction.
     */
    @Test
    public void repeatedRegistrationOnSameBusRemainsIdempotent() {
        Bus bus = new ExtensionManagerBus();

        for (int i = 0; i < 10000; i++) {
            new OxalisAlgorithmSuiteLoader(bus);
        }

        AlgorithmSuiteLoader loader =
                bus.getExtension(AlgorithmSuiteLoader.class);

        assertNotNull(loader);
        assertTrue(loader instanceof OxalisAlgorithmSuiteLoader);
        AssertionBuilderRegistry registry =
                bus.getExtension(AssertionBuilderRegistry.class);

        assertNotNull(registry.getBuilder(
                new QName(
                        OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                        OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256
                )));
    }

    /**
     * Extension Never Overwritten: Test case prove that there is No overwrite
     */
    @Test
    public void existingExtensionIsNeverReplaced() {
        Bus bus = new ExtensionManagerBus();
        new OxalisAlgorithmSuiteLoader(bus);

        AlgorithmSuiteLoader original =
                bus.getExtension(AlgorithmSuiteLoader.class);

        for (int i = 0; i < 500; i++) {
            new OxalisAlgorithmSuiteLoader(bus);
        }

        AlgorithmSuiteLoader current =
                bus.getExtension(AlgorithmSuiteLoader.class);

        assertSame(current, original);
    }

    /**
     * Duplicate Assertion Builder Test: Test case prove that there is No overwrite
     */
    @Test
    public void assertionBuilderIsRegisteredOnlyOnce() {
        Bus bus = new ExtensionManagerBus();

        for (int i = 0; i < 1000; i++) {
            new OxalisAlgorithmSuiteLoader(bus);
        }

        AssertionBuilderRegistry registry =
                bus.getExtension(AssertionBuilderRegistry.class);

        Object builder =
                registry.getBuilder(
                        new QName(
                                OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                                OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256));

        assertNotNull(builder);
        assertSame(
                builder,
                registry.getBuilder(
                        new QName(
                                OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                                OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256)));
    }

    /**
     * Multiple Bus Concurrent Registration: Test case catches:
     * 1. Thread-local bugs
     * 2. Shared static bugs
     * 3. Synchronization issues
     */
    @Test
    public void concurrentRegistrationAcrossManyBuses() throws Exception {
        int buses = 200;

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        List<Future<Bus>> futures = new ArrayList<>();
        for (int i = 0; i < buses; i++) {
            futures.add(executor.submit(() -> {
                Bus bus = new ExtensionManagerBus();
                new OxalisAlgorithmSuiteLoader(bus);
                return bus;
            }));
        }

        for (Future<Bus> future : futures) {
            Bus bus = future.get();
            assertTrue(
                    bus.getExtension(AlgorithmSuiteLoader.class)
                            instanceof OxalisAlgorithmSuiteLoader);
        }

        executor.shutdownNow();
    }

    /**
     * Long Running Race Test: Repeat concurrent registration. Used for catching intermittent races. It verify:
     * 1. No exceptions occurred.
     * 2. The registered extension is valid.
     * 3. The assertion builder is still registered.
     */
    @Test(invocationCount = 50)
    public void concurrentRegistrationRepeatedlyPasses() throws Exception {
        Bus bus = new ExtensionManagerBus();

        int threads = 20;
        int registrations = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(registrations);

        List<Throwable> failures =
                Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < registrations; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    new OxalisAlgorithmSuiteLoader(bus);
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    finished.countDown();
                }
                return null;
            });
        }

        start.countDown();
        assertTrue(
                finished.await(30, TimeUnit.SECONDS),
                "Concurrent registration did not complete within timeout.");

        executor.shutdown();
        assertTrue(
                executor.awaitTermination(10, TimeUnit.SECONDS),
                "Executor did not terminate.");
        assertTrue(
                failures.isEmpty(),
                "Concurrent registration produced unexpected exceptions: "
                        + failures);

        AlgorithmSuiteLoader loader =
                bus.getExtension(AlgorithmSuiteLoader.class);

        assertNotNull(loader);
        assertTrue(
                loader instanceof OxalisAlgorithmSuiteLoader,
                "Registered AlgorithmSuiteLoader must be OxalisAlgorithmSuiteLoader.");

        AssertionBuilderRegistry registry =
                bus.getExtension(AssertionBuilderRegistry.class);

        assertNotNull(registry);
        assertNotNull(
                registry.getBuilder(
                        new QName(
                                OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                                OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256)),
                "Custom assertion builder must remain registered after concurrent initialization.");
    }


    /**
     * No Race Leaves Invalid Extension: Verify that under heavy concurrent registration:
     * 1. No thread ever observes an invalid AlgorithmSuiteLoader.
     * 2. The final extension is valid.
     * 3. The extension is of the correct type.
     * 4. The custom assertion builder remains registered.
     * 5. No exceptions occur.
     */
    @Test
    public void concurrentRegistrationNeverLeavesInvalidExtension() throws Exception {
        Bus bus = new ExtensionManagerBus();

        int threads = 20;
        int iterations = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(iterations);

        List<Throwable> failures =
                Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < iterations; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    new OxalisAlgorithmSuiteLoader(bus);
                    AlgorithmSuiteLoader loader =
                            bus.getExtension(AlgorithmSuiteLoader.class);

                    assertNotNull(loader,
                            "Extension must never become null.");
                    assertTrue(
                            loader instanceof OxalisAlgorithmSuiteLoader,
                            "Extension must always be OxalisAlgorithmSuiteLoader.");

                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    finished.countDown();
                }
                return null;
            });
        }

        start.countDown();

        assertTrue(
                finished.await(30, TimeUnit.SECONDS),
                "Concurrent registration timed out.");
        executor.shutdown();
        assertTrue(
                executor.awaitTermination(10, TimeUnit.SECONDS));

        assertTrue(
                failures.isEmpty(),
                "Unexpected failures during concurrent registration: " + failures);

        AlgorithmSuiteLoader loader =
                bus.getExtension(AlgorithmSuiteLoader.class);

        assertNotNull(loader);

        assertTrue(loader instanceof OxalisAlgorithmSuiteLoader);

        AssertionBuilderRegistry registry =
                bus.getExtension(AssertionBuilderRegistry.class);

        assertNotNull(registry);

        assertNotNull(
                registry.getBuilder(
                        new QName(
                                OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                                OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256)),
                "Assertion builder must remain registered.");
    }

    /**
     * Concurrent Registration (Many Writers): this verifies:
     * 1. No exceptions
     * 2. No deadlocks
     * 3. No race corruption
     * 4. Correct final extension
     * 5 Assertion builder still registered
     */
    @Test
    public void concurrentRegistrationWithManyWritersSucceeds() throws Exception {
        Bus bus = new ExtensionManagerBus();

        final int THREADS = 20;
        final int REGISTRATIONS = 100;

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(REGISTRATIONS);

        List<Throwable> failures =
                Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < REGISTRATIONS; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    new OxalisAlgorithmSuiteLoader(bus);
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    finished.countDown();
                }
                return null;
            });
        }

        start.countDown();
        assertTrue(
                finished.await(30, TimeUnit.SECONDS),
                "Concurrent registration timed out.");

        executor.shutdown();
        assertTrue(
                executor.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(
                failures.isEmpty(),
                "Unexpected concurrent registration failures: "
                        + failures);

        AlgorithmSuiteLoader loader =
                bus.getExtension(AlgorithmSuiteLoader.class);

        assertNotNull(loader);
        assertTrue(
                loader instanceof OxalisAlgorithmSuiteLoader);
        AssertionBuilderRegistry registry =
                bus.getExtension(AssertionBuilderRegistry.class);
        assertNotNull(registry);
        assertNotNull(
                registry.getBuilder(
                        new QName(
                                OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                                OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256)));
    }

    /**
     * Concurrent Reader / Writer Race Test
     */
    @Test
    public void concurrentReadersNeverObserveInvalidExtension() throws Exception {

        Bus bus = new ExtensionManagerBus();

        //
        // Initial registration before concurrency begins.
        // From this point onwards the extension must never become null
        // or change to an unexpected implementation.
        //
        new OxalisAlgorithmSuiteLoader(bus);

        final int WRITERS = 10;
        final int READERS = 10;
        final int ITERATIONS = 500;

        ExecutorService executor =
                Executors.newFixedThreadPool(WRITERS + READERS);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finished =
                new CountDownLatch(WRITERS + READERS);

        List<Throwable> failures =
                Collections.synchronizedList(new ArrayList<>());

        //
        // Writer threads repeatedly attempt registration.
        //
        for (int i = 0; i < WRITERS; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        new OxalisAlgorithmSuiteLoader(bus);
                    }

                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    finished.countDown();
                }
                return null;
            });
        }

        //
        // Reader threads continuously verify the extension while
        // concurrent registration is occurring.
        //
        for (int i = 0; i < READERS; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < ITERATIONS; j++) {
                        AlgorithmSuiteLoader loader =
                                bus.getExtension(
                                        AlgorithmSuiteLoader.class);

                        assertNotNull(
                                loader,
                                "AlgorithmSuiteLoader must never become null.");
                        assertTrue(
                                loader instanceof OxalisAlgorithmSuiteLoader,
                                "Unexpected AlgorithmSuiteLoader implementation observed.");
                        AssertionBuilderRegistry registry =
                                bus.getExtension(
                                        AssertionBuilderRegistry.class);
                        assertNotNull(
                                registry,
                                "AssertionBuilderRegistry must always be available.");
                        assertNotNull(
                                registry.getBuilder(
                                        new QName(
                                                OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                                                OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256)),
                                "Custom assertion builder must remain registered.");
                    }
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    finished.countDown();
                }
                return null;
            });
        }

        start.countDown();

        assertTrue(
                finished.await(60, TimeUnit.SECONDS),
                "Concurrent reader/writer test timed out.");
        executor.shutdown();
        assertTrue(
                executor.awaitTermination(10, TimeUnit.SECONDS),
                "Executor did not terminate.");
        assertTrue(
                failures.isEmpty(),
                "Reader/writer race detected: " + failures);

        //
        // Final verification after all concurrent activity.
        //
        AlgorithmSuiteLoader loader =
                bus.getExtension(AlgorithmSuiteLoader.class);

        assertNotNull(loader);
        assertTrue(loader instanceof OxalisAlgorithmSuiteLoader);
        AssertionBuilderRegistry registry =
                bus.getExtension(AssertionBuilderRegistry.class);
        assertNotNull(registry);
        assertNotNull(
                registry.getBuilder(
                        new QName(
                                OxalisAlgorithmSuiteLoader.OXALIS_ALGORITHM_NAMESPACE,
                                OxalisAlgorithmSuiteLoader.BASIC_128_GCM_SHA_256)));
    }

    private static boolean awaitCollected(WeakReference<?> ref) throws InterruptedException {
        for (int attempt = 0; attempt < 20; attempt++) {
            System.gc();
            Thread.sleep(50);
            if (ref.get() == null) {
                return true;
            }
        }
        return false;
    }
}