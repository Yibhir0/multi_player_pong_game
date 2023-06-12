package com.mycompany.datacomprojectTwo.crypto;

import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.HASH_ALGORITHM;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;

/**
 * This class contains methods to normalize, validate, and compute
 * a hashed password that will be used to generate and load the keystore file.
 * It applies secure coding concepts to avoid injection or segmentation faults.
 * @author David Pizzolongo
 */
public class PongPassword {
    
    // initial password from user input
    private String pswd;
    
    // normalized version of the password 
    private String normalizedPswd;
    
    // normalized, validated and hashed password in char array format (final result)
    private char[] hashedPswd;
    
    private static final Logger LOGGER = Logger.getLogger(PongPassword.class.getName());

    /**
     * Main method of the PongPassword class. It is invoked when the user enters a game
     * password to save or load the game. It handles any exceptions that can occur 
     * while computing the hash and returns the hashed password that can be used 
     * by the keystore object.
     * @param {pswd} unnormalized and unvalidated password from the UI's password field
     * @return hashed password if the user's password is valid, according to the 
     * password format. If not, a null value is returned.
     */
    public char[] getHashedPassword(String pswd) {
        
        this.pswd = pswd;

        if (isValidPswd()) {
            try {
                /* after passing all the validation, the password is ready to be hashed by 
                   the SHA3-256 hashing algorithm */
                computeHash();
                return hashedPswd;
            } catch (NoSuchAlgorithmException ex) {
                System.out.println("Hash algorithm does not exist.");
            }
        }
        LOGGER.log(Level.SEVERE, "Invalid password format!");
        return null;
    }

    /**
     * The isValidPswd method is responsible for normalizing the user's password,
     * which is of type string, to NFKC format. It then ensures that the normalized string 
     * matches the validPswd pattern and returns the result.
     * @return boolean value returned by the matcher's method  
     */
    private boolean isValidPswd() {
        
        normalizedPswd = Normalizer.normalize(pswd, Normalizer.Form.NFKC);

        // user's password must contain a minimum of 6 letters and/or numbers
        Pattern validPswd = Pattern.compile("[A-Za-z0-9]{6,70}");
        Matcher matcher = validPswd.matcher(normalizedPswd);

        return matcher.find();
    }

    /**
     * This method computes a message digest using the provided algorithm (SHA3-256)
     * and uses this string of digits to encode the password bytes. It sets the 
     * hashedPswd to this char array.
     * @throws {NoSuchAlgorithmException}
     */
    private void computeHash() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashBytes = digest.digest(normalizedPswd.getBytes(StandardCharsets.UTF_8));

        String hashPswd = Base64.getEncoder().encodeToString(hashBytes);
        char[] hashPswdArray = hashPswd.toCharArray();

        hashedPswd = hashPswdArray;
    }
}
