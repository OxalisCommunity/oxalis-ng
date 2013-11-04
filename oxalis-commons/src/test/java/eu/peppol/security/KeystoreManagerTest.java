/* Created by steinar on 14.05.12 at 00:10 */
package eu.peppol.security;

import eu.peppol.security.KeystoreManager;
import org.testng.annotations.Test;

import java.security.cert.TrustAnchor;

import static org.testng.Assert.assertNotNull;

/**
 * @author Steinar Overbeck Cook steinar@sendregning.no
 */
@Test(groups = {"integration"})
public class KeystoreManagerTest {

    @Test
    public void loadKeystore() throws Exception {

        KeystoreManager instance = KeystoreManager.getInstance();

    }
}
