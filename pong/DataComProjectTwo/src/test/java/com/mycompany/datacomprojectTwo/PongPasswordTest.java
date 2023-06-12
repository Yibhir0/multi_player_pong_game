package com.mycompany.datacomprojectTwo;

import com.mycompany.datacomprojectTwo.crypto.PongPassword;
import com.mycompany.datacomprojectTwo.crypto.CryptoConfig;
import org.junit.jupiter.api.Test;
import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the getHashedPassword method from the PongPassword class, using 
 * various types of passwords.
 * @author David Pizzolongo
 */
public class PongPasswordTest {

    private String password;

    private final PongPassword pongPassword = new PongPassword();

    /* Base64 encoder (uses 6 bits per character), 256 bits/6 bits = ~43 chars.
    Then, add extra char for the padding (equal sign appended at the end of the string). */
    private final int VALID_HASHED_LENGTH = 44;
    
    // saves the current algorithm into a final variable 
    private final String ORIGINAL_ALGORITHM = HASH_ALGORITHM;

    // valid password tests
    
    /**
     * This test ensures that getHashedPassword() returns a non-null value when 
     * providing a valid password (with letters and numbers). 
     */
    @Test
    public void testGetHashedPasswordLettersNumbers() {
        password = "game101";
        assertNotNull(pongPassword.getHashedPassword(password));
    }

    /**
     * Tests that the getHashedPassword method returns a char array that is greater 
     * than the length of the initial password, when entering a valid password.
     */
    @Test
    public void testGetHashedPasswordOnlyNumbers() {
        password = "202021";
        char[] hashedPassword = pongPassword.getHashedPassword(password);
        assertTrue(hashedPassword.length > password.length());
    }

    /**
     * The following test ensures that getHashedPassword() returns 
     * a char array of 44 characters (SHA-256 hash length) even if the password contains 
     * uppercase and lowercase letters.
     */
    @Test
    public void testGetHashedPasswordLettersMixedCase() {
        password = "pingPoNg";
        char[] hashedPassword = pongPassword.getHashedPassword(password);
        assertEquals(VALID_HASHED_LENGTH, hashedPassword.length);
    }

    /**
     * Tests that getHashedPassword() returns a char array with a valid length,
     * when the user enters a password with special characters.
     */
    @Test
    public void testGetHashedPasswordLettersSymbols() {
        password = "thiswasfun!";
        char[] newPassword = pongPassword.getHashedPassword(password);
        assertEquals(VALID_HASHED_LENGTH, newPassword.length);
    }

    /**
     * Test makes sure that the getHashedPassword method returns a hashed password
     * that is not null, when the password contains accents that are accepted by the 
     * UTF-8 encoding scheme.
     */
    @Test
    public void testGetHashedPasswordAccentedLetters() {
        // accepted by UTF-8
        password = "étudiant48";
        char[] newPassword = pongPassword.getHashedPassword(password);
        assertNotNull(newPassword);
    }
    
    /**
     * Tests that the getHashedPassword method returns a hashed password, when another
     * hashing algorithm is given (SHA-512 instead of SHA-256). The hash length will be greater,
     * as the hashing will use 512 bits rather than 256.
     */
    @Test
    public void testGetHashedPasswordValidAlgorithm() {
        password = "ilovesports*";
        CryptoConfig.HASH_ALGORITHM = "SHA-512";
        // 512/6 + 2 = ~88 chars  (2 chars for padding)
        final int HASH_LENGTH = 88; 
        
        char[] hashedPassword = pongPassword.getHashedPassword(password);
        /* a NoSuchAlgorithmException will be thrown and caught, a custom error message
        will be shown */
        assertNotNull(hashedPassword);
        assertEquals(hashedPassword.length, HASH_LENGTH);
        
        // reset to original algorithm (SHA3-256)
        HASH_ALGORITHM = ORIGINAL_ALGORITHM;
    }

    // invalid password tests
    
    /**
     * This test method verifies that getHashedPassword() returns null when the password
     * given has an incorrect length (less than 6 chars) and has no letters or numbers included.
     */
    @Test
    public void testGetHashedPasswordInvalidSymbols() {
        password = ".....";
        char[] hashedPassword = pongPassword.getHashedPassword(password);
        // computeHash is not invoked and a value of null is returned
        assertNull(hashedPassword);
    }

    /**
     * Tests that getHashedPassword() returns null when the password
     * is an empty string with no characters. 
     */
    @Test
    public void testGetHashedPasswordEmptyString() {
        // incorrect length and no letters or numbers provided
        password = " ";
        char[] hashedPassword = pongPassword.getHashedPassword(password);
        assertNull(hashedPassword);
    }

    /**
     * Tests that getHashedPassword() returns null when the password
     * has a length less than 6 characters (regex will fail).
     */
    @Test
    public void testGetHashedPasswordInvalidLength() {
        // incorrect length (must be at least one more letter before '~')
        password = "javaa~";
        char[] hashedPassword = pongPassword.getHashedPassword(password);
        assertNull(hashedPassword);
    }

    /**
     * Tests that getHashedPassword() with an invalid hashing algorithm. A NoSuchAlgorithmException 
     * will be thrown and caught, a custom error message will be shown.
     */
    @Test
    public void testGetHashedPasswordInvalidAlgorithm() {
        password = "password123";
        HASH_ALGORITHM = "SHAA-789";
        
        char[] hashedPassword = pongPassword.getHashedPassword(password);
        assertNull(hashedPassword);
        
        // reset algorithm (SHA3-256)
        HASH_ALGORITHM = ORIGINAL_ALGORITHM;
    }

}
