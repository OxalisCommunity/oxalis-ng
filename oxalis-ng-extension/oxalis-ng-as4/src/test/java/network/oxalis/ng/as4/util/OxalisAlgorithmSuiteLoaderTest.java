package network.oxalis.ng.as4.util;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.security.policy.custom.AlgorithmSuiteLoader;
import org.testng.annotations.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
