package com.mycompany.datacomprojectTwo.crypto;

import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.ALGORITHM;
import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.GCMIV_FILE;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * The SymmetricCrypto class contains methods to generate an initialization vector,
 * and encrypt and decrypt a file using AES/GCM encryption.
 * @author David Pizzolongo
 */
public class SymmetricCrypto {

    // vector length 
    public static final int GCM_IV_LENGTH = 12;
    
    // tag to identify the vector 
    public static final int GCM_TAG_LENGTH = 16;
    
    /* initialization vector byte array (obtained from the 
       generateGCMIV() method or the readGCMIV() method) */
    private byte[] GCMIV;
    
    // same key used for encryption and decryption
    private SecretKey key;

    /* This parameterized constructor uses the 256-bit secret key from the 
       PongKeyStore class to initialize the secret key. */
    public SymmetricCrypto(SecretKey key) {
        this.key = key;
    }

    /**
     * Since the same GCMIV vector will be used at the start of a new game 
     * and at the end of a previous game, the GCMIV must be stored in a file.
     * The readOrStoreGCMIV method checks if the GCMIV vector has already 
     * been stored in a file. If it does not exist, the method randomly generates
     * an initialization vector and stores it into a file.
     * @throws {IOException} thrown by file input/output methods
     */
    public void readOrStoreGCMIV() throws IOException {
        readGCMIV();
        if (GCMIV == null) {
            generateGCMIV();
            storeGCMIV();
        }
    }

    /**
     * The following method reads the contents of the GCMIV file if it exists and sets 
     * the GCMIV to an array of bytes. It then closes the input stream. However, 
     * if the file does not exist, the GCMIV field is set to null.
     * @throws {FileNotFoundException}
     * @throws {IOException} 
     */
    private void readGCMIV() throws FileNotFoundException, IOException {
        File oldGcmiv = new File(GCMIV_FILE);

        if (oldGcmiv.exists()) {
            InputStream inputGcmiv = new FileInputStream(GCMIV_FILE);
            GCMIV = inputGcmiv.readAllBytes();
            inputGcmiv.close();
        } else {
            GCMIV = null;
        }
    }

    /**
     * This method securely generates a 12-byte GCM initialization vector, 
     * required for the encryption and decryption process. 
     */
    private void generateGCMIV() {
        GCMIV = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(GCMIV);
    }

    /**
     * This method uses an OutputStream object to store a new GCMIV vector
     * into a file. If the file does not exist on the user's system, it creates
     * an empty file and writes to it. This function is invoked after computing the vector. 
     * @throws {FileNotFoundException}
     * @throws {IOException} 
     */
    private void storeGCMIV() throws FileNotFoundException, IOException {
        File newGcmiv = new File(GCMIV_FILE);
        if (!newGcmiv.exists()) {
            newGcmiv.createNewFile();
        }

        OutputStream outputGcmiv = new FileOutputStream(newGcmiv);
        outputGcmiv.write(GCMIV);
        outputGcmiv.close();
    }

    // Method to encrypt a file 
    public void encryptFile(File inputFile, File outputFile)
            throws Exception {

        // create an instance of the Cipher class, passing the algorithm from CryptoConfig
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Create GCMParameterSpec with 128-bit tag
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, GCMIV);

        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

        FileOutputStream outputStream;
        //Create output stream
        try ( FileInputStream inputStream = new FileInputStream(inputFile)) {
            //Create output stream
            outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[64];
            int bytesRead;
            //Read up to 64 bytes of data at a time
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                //Cipher.update method takes byte array, input offset and input lentth
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    //Write the ciphertext for the buffer to the output file
                    outputStream.write(output);
                }
            }   //Encrypt the last buffer of plaintext 
            byte[] output = cipher.doFinal();
            if (output != null) {
                outputStream.write(output);
            }
            //Close the input and output streams
        }
        outputStream.close();
    }

    // Method to decrypt a file (parameters are inversed from encryptFile())
    public void decryptFile(File inputFile, File outputFile) throws Exception {

        //Create an instance of the Cipher class
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, GCMIV);

        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

        FileOutputStream outputStream;
        //Create output stream
        try ( FileInputStream inputStream = new FileInputStream(inputFile)) {
            //Create output stream
            outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[64];
            int bytesRead;
            //Read up to 64 bytes of data at a time
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    //Write the Plaintext to the output file
                    outputStream.write(output);
                }
            }   //Decrypt the last buffer of ciphertext
            byte[] output = cipher.doFinal();
            if (output != null) {
                outputStream.write(output);
            }
            //Close the input and output streams.
        }
        outputStream.close();
    }

}
