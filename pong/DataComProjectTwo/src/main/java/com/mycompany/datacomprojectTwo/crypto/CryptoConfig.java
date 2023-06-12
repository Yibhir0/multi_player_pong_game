
package com.mycompany.datacomprojectTwo.crypto;


/**
 * This final class contains all constants
 * variables used in the the cryptography component.
 * @author Yassine Ibhir and David Pizzolongo
 */
public final class CryptoConfig {
    
    
    private CryptoConfig(){}
     
    // Save game data file
    public static final String GAME_STATE_FILE_DEC = "game.sav";
    
    // The encrypted game data file
    public static final String GAME_STATE_FILE_ENC = "game.sav.enc";
    
    // Keystore file name
    public static final String KEY_STORE_FILE = "src/main/resources/Keystore.p12";
   
    
    // Digital signature file
    public static final String DIGITAL_SIGNATURE_FILE = "src/main/resources/PongApp.sig";

                                                                      
    // GCMIV for symmetric encryption
    public static final String GCMIV_FILE = "./src/main/resources/gcmiv";
    
    // AES KEY BITS
    public static final int KEY_BITS = 256;
    
    // Hash function used for hashing the password
    public static String HASH_ALGORITHM = "SHA3-256";
    
    // Algorithm for digital signature
    public static final String ECDSA_ALGORITHM = "SHA256withECDSA";
    
    // File to sign
    public static final String FILE_TO_SIGN = "./src/main/java/com/mycompany/datacomprojectTwo/PongApp.java";

    // Algorithm used for symmetric encryption
    public static final String ALGORITHM = "AES/GCM/NoPadding";
    
    // Command to generate 256 bit key pair using keyTool with SHA256withECDSA algorithm (EC)
    public static String [] KEY_TOOL_CMD = new String[]{"keytool","-genkeypair","-alias","pongkeyPair","-keyalg","EC","-dname","CN=Pong","-storetype","PKCS12"
                                                        ,"-keystore",KEY_STORE_FILE,"-storepass","" };

    // Alias for keyPair'
    public static final String ALIAS_KEY_PAIR = "pongkeyPair";
    
    // Alias for SecretKey'
    public static final String ALIAS_SECRET_KEY = "secret";
    
    // KeyStore Type
    public static final String KEY_STORE_TYPE = "PKCS12";
    
    // Password for testing
    public static final String TESTING_PASSWORD = "1234pp";
            
}
