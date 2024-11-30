package network.oxalis.as4.inbound;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import java.nio.file.Path;
import java.security.KeyStore;

public class As4ServletTest {

    @Mock
    private Path confFolder;

    private KeyStore trustStore = null;

    @InjectMocks
    private As4Servlet servlet = new As4Servlet();


    private KeyStore generateEmptyKeyStore() {
        try {
            KeyStore ks = KeyStore.getInstance("jks");
            ks.load(null, null);

            return ks;
        } catch (Exception e) {
            return null;
        }
    }

    @BeforeTest
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void beforeMethod() {
        trustStore = generateEmptyKeyStore();
        Whitebox.setInternalState(servlet, "trustStore", trustStore);
    }
}
