
package com.mycompany.datacomprojectTwo;

import com.mycompany.datacomprojectTwo.crypto.PongKeyStore;
import com.mycompany.datacomprojectTwo.crypto.PongDigitalSignature;
import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.*;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

/**
 * 
 * @author Yassine Ibhir
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PongDigitalSignatureTest {

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
     * Test of generateSignature method, of class PongDigitalSignature.
     */
    @Test
    public void testGenerateSignature() throws Exception {
        
        // Load Keystore
        PongKeyStore pks = new PongKeyStore(TESTING_PASSWORD.toCharArray());
        
        // Get Private key 
        PrivateKey pk = pks.getPrivateKey();
        
        // Initialize DigitalSignature class
        PongDigitalSignature pds = new PongDigitalSignature();
        
        // Generate digital signature
        byte[] signature = pds.generateSignature(ECDSA_ALGORITHM , pk, FILE_TO_SIGN);
        
        assertNotNull(signature);
       
    }

    /**
     * Test of verifySignature method, of class PongDigitalSignature.
     */
    @Test
    public void testVerifySignature() throws Exception {
         // Load Keystore
        PongKeyStore pks = new PongKeyStore(TESTING_PASSWORD.toCharArray());
        
        // Get Public key 
        PublicKey puk = pks.getPublicKey();
        
         // Get Private key 
        PrivateKey pk = pks.getPrivateKey();
        
        // Initialize DigitalSignature class
        PongDigitalSignature pds = new PongDigitalSignature();
       
        // Generate digital signature
        byte[] signature = pds.generateSignature(ECDSA_ALGORITHM , pk, FILE_TO_SIGN);
        
        // Verify signature
        boolean isValid = pds.verifySignature(signature, puk, ECDSA_ALGORITHM, FILE_TO_SIGN);
        
        assertTrue(isValid);
        
    }

    /**
     * Test of readSignatureFromFile method, of class PongDigitalSignature.
     */
    @Test
    public void testReadSignatureFromFile() throws Exception {
        
        // Load Keystore
        PongKeyStore pks = new PongKeyStore(TESTING_PASSWORD.toCharArray());
        
        // Get Private key 
        PrivateKey pk = pks.getPrivateKey();
        // Initialize DigitalSignature class
        PongDigitalSignature pds = new PongDigitalSignature();
        
        // Generate digital signature
        byte[] signature = pds.generateSignature(ECDSA_ALGORITHM , pk, FILE_TO_SIGN);
        
        // Write signature to file
        pds.writeSignatureToFile(DIGITAL_SIGNATURE_FILE, signature);
        
        // Read Signature 
        byte[] signatureFile = pds.readSignatureFromFile(DIGITAL_SIGNATURE_FILE);
        
        assertNotNull(signatureFile);
        
        
    }

    /**
     * Test of writeSignatureToFile method, of class PongDigitalSignature.
     */
    @Test
    public void testWriteSignatureToFile() throws Exception {
        
        // Load Keystore
        PongKeyStore pks = new PongKeyStore(TESTING_PASSWORD.toCharArray());
        
        // Get Private key 
        PrivateKey pk = pks.getPrivateKey();
        
        // Initialize DigitalSignature class
        PongDigitalSignature pds = new PongDigitalSignature();
        
        // Generate digital signature
        byte[] signature = pds.generateSignature(ECDSA_ALGORITHM , pk, FILE_TO_SIGN);
        
        // Write signature to file
        pds.writeSignatureToFile(DIGITAL_SIGNATURE_FILE, signature);
        
        // Check if file was stored
        File f = new File(DIGITAL_SIGNATURE_FILE );
       
        boolean fileExist = f.exists();
        
        // Test if file is stored
        assertTrue(fileExist);
    }
    
}
