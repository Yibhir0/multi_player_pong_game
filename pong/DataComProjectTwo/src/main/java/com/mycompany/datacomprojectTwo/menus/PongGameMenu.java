package com.mycompany.datacomprojectTwo.menus;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import static com.almasb.fxgl.dsl.FXGL.*;

import static com.almasb.fxgl.dsl.FXGL.getSettings;
import static com.almasb.fxgl.dsl.FXGL.getUIFactoryService;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;

import javafx.scene.Node;

import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.*;
import com.mycompany.datacomprojectTwo.crypto.PongDigitalSignature;
import com.mycompany.datacomprojectTwo.crypto.PongKeyStore;
import com.mycompany.datacomprojectTwo.crypto.PongPassword;
import com.mycompany.datacomprojectTwo.crypto.SymmetricCrypto;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import javafx.scene.input.KeyCode;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javax.crypto.SecretKey;

/**
 * This class is The pong game menu. It extends FXGLMenu
 * class and inherits some functionalities from the parent class.
 * The menu is shown by pressing the escape key on the server side.
 * The client side has a static menu that indicates which keys to press
 * to perform certain actions. Note: Client shall not press escape key or use 
 * use the game menu. 
 * @author Yassine & David
 */
public class PongGameMenu extends FXGLMenu {
    
    // Flags when server is running
    private boolean isServer;
    
    // Client input used in the input replication
    private Input clientInput;
    
    // Save button
    private Button save;
    
    // Resume button
    private Button resume;
    
    // Exit button
    private Button exit;
    
    // Message to display in the password field
    private Label message;
    
    // Logger for any exception
    private static final Logger LOGGER = Logger.getLogger(PongGameMenu.class.getName());
    
    /**
     * Constructor initialize game menu
     * and it's body.
     */
    public PongGameMenu() {
        super(MenuType.GAME_MENU);
        
        // Get Fxgl game menu title
        var titleView = getUIFactoryService().newText(getSettings().getTitle(), 48);
        centerTextBind(titleView, getAppWidth() / 2.0, 100);
        
        // Initialize label for password
        message = new Label("");
        
        // Create body
        var body = createMenuButtons();
        
        // Add to the content root
        getContentRoot().getChildren().addAll(titleView, body);

    }
    
    /**
     * Set the server to be true from the main game application.
     * @param server 
     */
    public void setServer(boolean server) {
        this.isServer = server;
    }
    
    /**
     * Set the game menu input used in the client side.
     * this input will get replicated in the server side
     * and that will allow server to perform cryptography components etc.
     * Note : client does not do any cryptography.
     * @param input 
     */
    public void setGameMenuInput(Input input) {

        this.clientInput = input;
        
        // Space to pause engine
        this.clientInput.addAction(new UserAction("SPACE") {

            @Override
            protected void onActionBegin() {
                getExecutor().startAsyncFX(() -> getController().pauseEngine());
            }
        }, KeyCode.SPACE);
        
        // R to resume
        this.clientInput.addAction(new UserAction("Resume") {

            @Override
            protected void onActionBegin() {
                getGameController().resumeEngine();
            }
        }, KeyCode.R);
        
        // S to save 
        this.clientInput.addAction(new UserAction("Save") {

            @Override
            protected void onActionBegin() {
                
                // Ask for password 0 for saving
                passwordBox(0);
            }
        }, KeyCode.S);
        
         // SX to exit
        this.clientInput.addAction(new UserAction("Exit") {

            @Override
            protected void onActionBegin() {
                // Ask for password 1 fro exiting
                 passwordBox(1);
            }
        }, KeyCode.X);
        
        // This event will only work for the first time. The client should never press escape.
        this.clientInput.addAction(new UserAction("ESCAPE") {

            @Override
            protected void onActionBegin() {
                getExecutor().startAsyncFX(() -> getController().pauseEngine());
            }
        }, KeyCode.ESCAPE);
    }
    
    /**
     * Change texts of the game menu buttons
     * of the client side
     */
    public void changeButtonTexts() {
        
        resume.setText("Press escape then ");
        
        save.setText("Press R then ");
        
        exit.setText("Never press escape again !");

    }
    
    /**
     * Create buttons of the game menu. Set the events to get triggered
     * on the server side only
     * @return 
     */
    private Node createMenuButtons() {

        resume = new Button("Resume");
        
        // FXGL resume
        resume.setOnMouseClicked(event -> {

            if (isServer) {
                this.fireResume();
            }

        });
        
        save = new Button("Save And Exit");
        
        // Custom save to perform cryptography
        save.setOnMouseClicked(event -> {

            if (isServer) {
                passwordBox(0);
            }

        });
        
        // Custom exit to perform cryptography
        exit = new Button("Exit");

        exit.setOnMouseClicked(event -> {

            if (isServer) {

                passwordBox(1);
            }

        });

        VBox v = new VBox(resume, save, exit);

        return v;
    }
    
    /**
     * This method access the pongPassword class
     * to  normalize, validate, and hash the user password.
     * @param pass user password
     * @return a hashed password 
     */
    private char[] hashPassword(String pass) {
        PongPassword p = new PongPassword();
        char[] hashed = p.getHashedPassword(pass);
        return hashed;
    }
    
    /**
     * This method generate a signature before exiting program.
     * The method takes a password as a parameter, hash it and validate it.
     * If the hash is null, not valid or incorrect, the program keeps asking user
     * to enter a valid password until it successfully gets the private key.
     * After getting the private key from the keyStore, it generates the 
     * signature. It also make sure what was the reason for exiting since the
     * method can also be invoked when game is over. For that the method shows
     * the winner on the server side.
     * @param password
     */
    private  void exitAndGenerateSignature(String password) {
        try {
            char[] hashed = hashPassword(password);
            if (hashed == null) {
                message.setText("Your password is incorrect!");
                message.setTextFill(Color.rgb(210, 39, 30));
                passwordBox(3);
            }

            PongKeyStore pks = new PongKeyStore(hashed);
            PongDigitalSignature pdigital = new PongDigitalSignature();

            PrivateKey privateK = pks.getPrivateKey();

            byte[] ditalSig = pdigital.generateSignature(ECDSA_ALGORITHM, privateK, FILE_TO_SIGN);
            pdigital.writeSignatureToFile(DIGITAL_SIGNATURE_FILE, ditalSig);
            this.fireExit();

        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | UnrecoverableKeyException | CertificateException ex) {

            logError(LOGGER);
            message.setText("Your password is incorrect!");
            message.setTextFill(Color.rgb(210, 39, 30));
            passwordBox(3);
        }
    }

    /**
     * This method takes in a password as a parameter. It first
     * hashes the password, then verifies if it's valid. It saves the game
     * ,then accesses the keyStore to get the secret key, and finally encrypts
     * the file that saves the game. When the password is not valid or incorrect
     * the method will keep calling passwordBox method for the purpose of saving
     * the game state until the password is correct.
     * @param password user password
     */
    private void saveAndEncrypt(String password) {
        try {
            // Hash password
            char[] hashed = hashPassword(password);
            
            // Password is not correct. 
            if (hashed == null) {
                // Inform the user
                message.setText("Your password is incorrect!");
                message.setTextFill(Color.rgb(210, 39, 30));
                // Ask for the password again.
                passwordBox(0);
            }
            
            // Access the keystore with the password.          
            PongKeyStore pks = new PongKeyStore(hashed);
            
            // Get the secret key
            SecretKey secretK = pks.getSecretKey();
            
            // Password is correct and secret key is retrieved. We save.
            getSaveLoadService().saveAndWriteTask(GAME_STATE_FILE_DEC).run();
            
            // After saving we access the symmetric encryption class providing a secret key
            SymmetricCrypto sCrypto = new SymmetricCrypto(secretK);
            
            // We read or load GCMIV
            sCrypto.readOrStoreGCMIV();
            
            // Initialize the file to encrypt (game.save)
            File input = new File(GAME_STATE_FILE_DEC);
            
            // Initialize the file to contain the encrypted version (game.save.enc)
            File output = new File(GAME_STATE_FILE_ENC);
            
            // Encrypt the file
            sCrypto.encryptFile(input, output);
            
            message.setText("");
            
            // Exit and generate signature.
            exitAndGenerateSignature(password);
            
        } catch (Exception ex) { // If the Keystore password is not correct.

            logError(LOGGER);

            // Inform the user
            message.setText("Your password is incorrect!");
            message.setTextFill(Color.rgb(210, 39, 30));
            // Ask for the password again.
            passwordBox(0);
        }
    }
    
    /**
     * This method handles the events that requires a password.
     * The events are labeled with an integer which is  given
     * as a parameter. 0 for saving and encrypting and 1 or higher
     * is for exiting/generating a signature.
     * The method creates a password dialog box and handles those events.
     * @param passwordFor defines the method to be called with the password.
    */
    private void passwordBox(int passwordFor) {
        
        // Javafx password field
        PasswordField pb = new PasswordField();
        
        // Build a vbox that contains password, and a label message
        VBox passwordV = new VBox(pb, message);
        
        // Create a button to close the dialog box
        Button btnClose = getUIFactoryService().newButton("OK");
        
        // Set the with of the button
        btnClose.setPrefWidth(300);
        
        // Set the event on the close button 
        btnClose.setOnAction((ActionEvent e) -> {
            // Get the value of the password field
            String userPassword = pb.getText();
            
            // Check what purpose is the password for.           
            if(passwordFor == 0){ // 0 for saving the game and exiting.           
                // Save and encrypt.
                saveAndEncrypt(userPassword);
  
            }
            
            else{ // any other number for exiting and generating a digital signature.
                exitAndGenerateSignature(userPassword);
            }
            
            // Resume the game loop when the close button is triggered.
            getDialogService().onMainLoopResumed();
        });
        
        // Show The password DialogBox.
        getDialogService().showBox("Keystore Password", passwordV, btnClose);
    }
    
    /**
     * Log error to the output
     * @param LOGGER 
     */
    public void logError(Logger LOGGER) {
        LOGGER.log(Level.SEVERE, "Attempt to access keystore with invalid password!");
    }

}
