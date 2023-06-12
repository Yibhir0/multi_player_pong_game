package com.mycompany.datacomprojectTwo.crypto;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * This class implements the digital signature functionalities.
 * It contains methods that compute a digital signature of a message file,
 * store the signature inside a storage file, retrieve the signature from
 * the storage file and verify that a message file has not been altered.
 * @author Yassine Ibhir
 */
public class PongDigitalSignature {
    
    /**
     * Method for generating digital signature.
     * @param algorithm
     * @param privatekey
     * @param fileToBeSigned
     * @return signature
     * @throws java.security.NoSuchAlgorithmException 
     * @throws java.security.NoSuchProviderException 
     * @throws java.security.InvalidKeyException 
     * @throws java.io.UnsupportedEncodingException 
     * @throws java.security.SignatureException 
     */
    public byte[] generateSignature (String algorithm, PrivateKey privatekey, String fileToBeSigned) 
            throws NoSuchAlgorithmException, NoSuchProviderException, 
            InvalidKeyException, UnsupportedEncodingException, SignatureException, IOException {
        
        //Create an instance of the signature scheme for the given signature algorithm
        Signature sig = Signature.getInstance(algorithm, "SunEC");
        
        //Initialize the signature scheme
        sig.initSign(privatekey);

        // Read the file to sign
        byte[] fileBytes = Files.readAllBytes(Paths.get(fileToBeSigned));
        
        // Compute the signature
        sig.update(fileBytes);
        
        byte[] digitalSignature = sig.sign();
        
        return digitalSignature;
    }
    
    /**
     * Method for verifying digital signature.
     * return if signature is valid or not.
     * @param signature
     * @param publickey
     * @param algorithm
     * @param receivedFile
     * @return true or false
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws SignatureException
     * @throws IOException 
     */
    public boolean verifySignature(byte[] signature, PublicKey publickey, String algorithm, String receivedFile) 
            throws NoSuchAlgorithmException, NoSuchProviderException, 
            InvalidKeyException, UnsupportedEncodingException, SignatureException, IOException {
        
        //Create an instance of the signature scheme for the given signature algorithm
        Signature sig = Signature.getInstance(algorithm, "SunEC");
        
        //Initialize the signature verification scheme.
        sig.initVerify(publickey);
        
        byte[] fileBytes = Files.readAllBytes(Paths.get(receivedFile));

        sig.update(fileBytes);
        
        //Verify the signature.
        boolean validSignature = sig.verify(signature);
        
        if(validSignature) {
            System.out.println("\nSignature is valid");
        } else {
            System.out.println("\nSignature is NOT valid!!!");
        }
        
        return validSignature;
    }
    
    /**
     * This method reads the file storing signature into a byte array
     * and returns the signature
     * @param fileStoringSignature
     * @return signature
     * @throws IOException 
     */
    public byte[] readSignatureFromFile(String fileStoringSignature) throws IOException{
        
        byte[] signature = Files.readAllBytes(Paths.get(fileStoringSignature));
        
        return signature;
    }
    
    /**
     * This method store the signature inside a file.
     * @param fileToStoreSignature
     * @param signature
     * @throws IOException 
    */
    public void writeSignatureToFile(String fileToStoreSignature, byte[] signature) throws IOException{
        Files.write(Paths.get(fileToStoreSignature), signature);
    }
}
