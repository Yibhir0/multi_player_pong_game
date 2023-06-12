
package com.mycompany.datacomprojectTwo.crypto;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.SecretKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;

import java.security.cert.CertificateException;

/**
 * This class implements the keyStore functionalities.
 * It contains two parameterized constructors. The first constructor
 * instantiate the pongKeyStore when the program has no KeyStore file,
 * and calls the methods to generate the keys and store them. The second
 * constructor is used to access and existent keyStore. 
 * The instance of the second constructor will be used to retrieve all the keys.
 * @author Yassine Ibhir
 */
public class PongKeyStore {
    // Password to use in keystore
    private char[] pswd;
    
    //Keystore instance
    private KeyStore ks;
    
    // Operating system command to generate keyPair with keyTool
    private String [] command;
    
    /**
     * This constructor  will be invoked when we first create the keyStore.
     * The constructor takes as arguments the password to access the keyStore
     * and the operating system command to generate a keyPair using keyTool.
     * The command arguments are static except the password given by user.
     * @param pswd hashed password to create and access keyStore
     * @param command to generate the keyPair used for digital signature
     * @throws java.security.KeyStoreException
     */
    public PongKeyStore(char[] pswd, String [] command) throws KeyStoreException{
        this.pswd = pswd;
        this.command = command;
        // Set password to command
        this.command[this.command.length-1] = String.valueOf(this.pswd);
        
        ks = KeyStore.getInstance(KEY_STORE_TYPE);
    }
    
     /**
     * This constructor will be invoked when we access
     * an existent keyStore. It LoadS keyStore,
     * and set the password used to retrieve
     * the keys stored in the keyStore. 
     * @param pswd hashed password to access keyStore.
     * @throws java.security.KeyStoreException
     */
    public PongKeyStore(char[] pswd) throws KeyStoreException{
        this.pswd = pswd;
        ks = KeyStore.getInstance(KEY_STORE_TYPE);
    }
    
    /**
     * This method generates all the cryptography keys,
     * stores them inside a keyStore instance, and writes
     * the keyStore instance to a file. The method gets invoked
     * when the program has no stored keyStore file. It is invoked
     * with the instance constructed by the constructor with password
     * and command parameters.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException 
     */
    public void createAndStoreKeys() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException{
        
        // Gnerate keyPair and store it in keyStore
        generateKeyPairProcess();
        
        // Generate SecretKey and store it in keyStore
        generateSecretKeyProcess();
        
        // Store keystore in a file
        writeKeyStoreToFile();

    }
    
    /**
     * This method loads the file containing the keyStore instance,
     * generate a secret key and store it in the keyStore.
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws CertificateException 
     */
    private void generateSecretKeyProcess() throws NoSuchAlgorithmException, KeyStoreException, FileNotFoundException, IOException, CertificateException{
        
        // Load the keystore created by keyTool proces
        FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream(KEY_STORE_FILE);
            ks.load(fis, this.pswd);
            } finally {
            if (fis != null) {
                fis.close();
            }
        }
       
        // SetUp protection password
        ProtectionParameter protParam = new PasswordProtection(this.pswd);
        
        // Generate secret key
        SecretKey secretkey = generateSecretKey();
        
        // Set up SecretKey entry
        SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretkey);
        
        // Set the entry
        ks.setEntry(ALIAS_SECRET_KEY, skEntry, protParam);
        
    }
    
    /**
     * Method stores the KeyStore into a file
     * @throws FileNotFoundException
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException 
     */
    private void writeKeyStoreToFile() throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException{
    
        // store away the keystore
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(KEY_STORE_FILE);
            ks.store(fos, this.pswd);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    /**
     * Method uses process builder to run the command that
     * generates 256-bit asymmetric key.
     * @throws IOException 
    */
    private void generateKeyPairProcess() throws IOException{
        ProcessBuilder builder = new ProcessBuilder(
            this.command);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);
        }
        r.close();
    }
    
    /**
    * This method loads the existent keyStore file
    * and retrieves the private key.
    * @return PrivateKey 
    * @throws FileNotFoundException
    * @throws KeyStoreException
    * @throws IOException
    * @throws NoSuchAlgorithmException
    * @throws CertificateException
    * @throws UnrecoverableKeyException 
    */    
    public PrivateKey getPrivateKey() throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException{
       
        this.ks.load(new FileInputStream(KEY_STORE_FILE), this.pswd);
        PrivateKey privateKey = 
        (PrivateKey) this.ks.getKey(ALIAS_KEY_PAIR, this.pswd);
        return privateKey;
    }
    
    /**
     * This method loads the existent keyStore file
     * and retrieves the public key. 
     * @return public key
     * @throws KeyStoreException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException 
     */
    public PublicKey getPublicKey() throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException{
        this.ks.load(new FileInputStream(KEY_STORE_FILE), this.pswd);
        Certificate certificate = this.ks.getCertificate(ALIAS_KEY_PAIR);
        PublicKey publicKey = certificate.getPublicKey();
        
        return publicKey;
    }
    
    /**
     * Method generates 256 bits AES symmetric-key (secret key) 
     * @return SecretKey
     * @throws NoSuchAlgorithmException 
     */
    private SecretKey generateSecretKey() throws NoSuchAlgorithmException{
        
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(KEY_BITS); //Initialize the key generator
        SecretKey key = keyGenerator.generateKey(); //Generate the key
        return key;
    }
    
    /**
     * This method loads the existent keyStore file
     * and retrieves the secret key. 
     * @return Secret key
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException 
     */
    public SecretKey getSecretKey() throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException{
        this.ks.load(new FileInputStream(KEY_STORE_FILE), this.pswd);
        SecretKey secretKey = (SecretKey) this.ks.getKey(ALIAS_SECRET_KEY, this.pswd);
       
        return secretKey;
      
    }
    
}
