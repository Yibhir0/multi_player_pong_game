
package com.mycompany.datacomprojectTwo;

import com.mycompany.datacomprojectTwo.crypto.PongKeyStore;
import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.*;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.KEY_TOOL_CMD;

/**
 * Class tests PongkeyStore methods.
 * @author Yassine Ibhir
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PongKeyStoreTest {


    /**
     * We create keyStore and stores all the keys first.
     * @throws java.lang.Exception
     */
    @BeforeAll
    public static void initStore()  throws Exception{
        PongKeyStore pks = new PongKeyStore(TESTING_PASSWORD.toCharArray(),KEY_TOOL_CMD);
        pks.createAndStoreKeys();
    }
    
    /**
     * Test of CreateAndStoreKeys method, of class PongKeyStore.
     * @throws Exception 
     */
    
    @Test
    public void testCreateAndStoreKeys() throws Exception {
        
        File f = new File(KEY_STORE_FILE);
       
        boolean fileExist = f.exists();
        
        // Test if file is stored
        assertTrue(fileExist);

    }

    /**
     * Test of getPrivateKey method, of class PongKeyStore.
     */
    @Test
    public void testGetPrivateKey() throws Exception {
        PongKeyStore pks1 = new PongKeyStore(TESTING_PASSWORD.toCharArray());
        // Test if private key is not null
        PrivateKey pk = pks1.getPrivateKey();
        assertNotNull(pk);
        System.out.println("Private Key : " +pk);
    }

    /**
     * Test of getPublicKey method, of class PongKeyStore.
     */
    @Test
    public void testGetPublicKey() throws Exception {
        PongKeyStore pks1 = new PongKeyStore(TESTING_PASSWORD.toCharArray());
         // Test if public key is not null
        PublicKey puk = pks1.getPublicKey();
        assertNotNull(puk);
        System.out.println("PublicKey Key : " +puk.toString());
    }

    /**
     * Test of getSecretKey method, of class PongKeyStore.
     */
    @Test
    public void testGetSecretKey() throws Exception {
        PongKeyStore pks1 = new PongKeyStore(TESTING_PASSWORD.toCharArray());
        
        // Test if secret key is not null
        SecretKey sk = pks1.getSecretKey();
        assertNotNull(sk);
        System.out.println("SecretKey Key : " +sk.toString());
    }

    
}
