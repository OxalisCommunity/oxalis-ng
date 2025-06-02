package network.oxalis.ng.as4.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DefaultMessageIdGenerator.class)
public class DefaultMessageIdGeneratorTest {

    private DefaultMessageIdGenerator generator;

    @Before
    public void setUp() {
        UUID fixedUUID = UUID.fromString("8196c8e2-820f-4aec-a1ca-288a4d1d4020");

        mockStatic(UUID.class);
        when(UUID.randomUUID()).thenReturn(fixedUUID);

        generator = new DefaultMessageIdGenerator("seller.eu");
    }

    @Test
    public void testGenerateByTransmissionRequest() {
        Assert.assertEquals("8196c8e2-820f-4aec-a1ca-288a4d1d4020@seller.eu", generator.generate());
    }
}
