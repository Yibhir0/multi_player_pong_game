package com.mycompany.datacomprojectTwo;

import com.mycompany.datacomprojectTwo.crypto.SymmetricCrypto;
import com.mycompany.datacomprojectTwo.crypto.PongKeyStore;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

/**
 * The SymmetricCryptoTest class tests the readOrStoreGCMIV, encryptFile and 
 * decryptFile methods.
 * @author David Pizzolongo
 */
public class SymmetricCryptoTest {

    // used for all the tests
    private static SecretKey key;
    private SymmetricCrypto symmCrypto;

    /**
     * Runs once before all tests. It performs the keytool command to generate and store a SecretKey.
     * The method then retrieves the key which will be used for encryption and decryption.
     * @throws Exception
     */
    @BeforeAll
    public static void initEncryption() throws Exception {
        PongKeyStore keyStore = new PongKeyStore(TESTING_PASSWORD.toCharArray(), KEY_TOOL_CMD);
        keyStore.createAndStoreKeys();
        key = keyStore.getSecretKey();
    }

    /**
     * This test method ensures that readOrStoreGCMIV() stores a file containing the 
     * GCMIV vector. 
     * @throws IOException
     */
    @Test
    public void testReadOrStoreGCMIV() throws IOException {
        symmCrypto = new SymmetricCrypto(key);
        symmCrypto.readOrStoreGCMIV();
        
        File gcmiv = new File(GCMIV_FILE);
        assertTrue(gcmiv.isFile());
        
        // contents are not empty 
        InputStream inputGcmiv = new FileInputStream(GCMIV_FILE);
        assertTrue(inputGcmiv.readAllBytes().length > 0);
    }

    /**
     * This test verifies that the encryptFile method encrypts the contents of a game file
     * and saves this encrypted file to the file system. The original file and the encrypted file
     * will have different file contents. 
     * @throws IOException
     * @throws Exception 
     */
    @Test
    public void testEncryptFile() throws IOException, Exception{
        symmCrypto = new SymmetricCrypto(key);
        symmCrypto.readOrStoreGCMIV();
        
        File gameFile = new File("game_test.sav");
        File gameFileEnc = new File("game_test.enc");
        
        InputStream file = new FileInputStream(gameFile);
        String fileContent = new String(file.readAllBytes(), StandardCharsets.UTF_8);
        
        symmCrypto.encryptFile(gameFile, gameFileEnc);
        
        InputStream encFile = new FileInputStream(gameFileEnc);
        String encFileContent = new String(encFile.readAllBytes(), StandardCharsets.UTF_8);
        
        assertNotEquals(fileContent, encFileContent);
    }
    
    /**
     * Tests encryptFile() providing invalid filenames. The test makes sure that 
     * a FileNotFoundException will be thrown and prints an appropriate message to the console.
     * @throws IOException
     * @throws Exception 
     */
    @Test
    public void testEncryptFileInvalidFiles() throws IOException, Exception {
        symmCrypto = new SymmetricCrypto(key);
        try {
            symmCrypto.readOrStoreGCMIV();
            symmCrypto.encryptFile(new File("inputFile"), new File("outputFile.enc"));
        } catch (FileNotFoundException invalidFileEx) {
            System.out.println("Input file does not exist! Encrypt test passed.");
        }
    }
    
    /**
     * Tests the decryptFile method providing files that do not exist. The test handles 
     * a FileNotFoundException, and shows an error message to the user.
     * @throws IOException
     * @throws Exception 
     */
    @Test
    public void testDecryptFileInvalidFiles() throws IOException, Exception {
        symmCrypto = new SymmetricCrypto(key);
        try {
            symmCrypto.readOrStoreGCMIV();
            symmCrypto.decryptFile(new File("output.enc"), new File("input"));
        } catch (FileNotFoundException invalidFileEx) {
            System.out.println("Input and output files do not exist! Decrypt test passed.");
        }
    }
    
    /**
     * This test validates that after encrypting and decrypting a game file, 
     * the decrypted file's contents will be identical to the original file's contents.
     * It verifies that the entire process is successful (reading/storing the GCMIV,
     * encryption and decryption).
     * @throws IOException
     * @throws Exception 
     */
    @Test
    public void testEncryptAndDecryptFile() throws IOException, Exception{
        symmCrypto = new SymmetricCrypto(key);
        symmCrypto.readOrStoreGCMIV();
        
        File gameFile = new File("game_test.sav");
        File gameFileEnc = new File("game_test.enc");
        
        InputStream file = new FileInputStream(gameFile);
        String fileContent = new String(file.readAllBytes(), StandardCharsets.UTF_8);
        
        symmCrypto.encryptFile(gameFile, gameFileEnc);
        symmCrypto.decryptFile(gameFileEnc, gameFile);
        
        InputStream decFile = new FileInputStream(gameFile);
        String decFileContent = new String(decFile.readAllBytes(), StandardCharsets.UTF_8);
        
        // compares both strings (original file and decrypted file)
        assertEquals(fileContent, decFileContent);
    }
   
}
