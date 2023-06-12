package com.mycompany.datacomprojectTwo.menus;

import com.mycompany.datacomprojectTwo.crypto.SymmetricCrypto;
import com.mycompany.datacomprojectTwo.crypto.PongKeyStore;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import static com.almasb.fxgl.dsl.FXGL.centerTextBind;
import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGL.getSettings;
import static com.almasb.fxgl.dsl.FXGL.getUIFactoryService;
import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.*;
import com.mycompany.datacomprojectTwo.crypto.PongDigitalSignature;
import com.mycompany.datacomprojectTwo.crypto.PongPassword;
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
import javafx.event.ActionEvent;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javax.crypto.SecretKey;

/**
 * The PongMainMenu class contains GUI elements for starting, loading and exiting 
 * the game (buttons) and showing an error dialog box (label). It also keeps track 
 * when the keystore file exists or when the game state file is stored.
 * @author David & Yassine
 */
public class PongMainMenu extends FXGLMenu {

    private Button load;

    private Button exit;

    private Label message;

    private boolean keyStoreExist;
    
    private boolean saveSetateFile;

    /**
     * The default constructor inherits the functionality and styles of the main
     * menu, and adds the menu buttons, contained in a VBox element, to the view.
     */
    public PongMainMenu() {

        super(MenuType.MAIN_MENU);

        var titleView = getUIFactoryService().newText(getSettings().getTitle(), 48);
        centerTextBind(titleView, getAppWidth() / 2.0, 100);

        message = new Label("");
        var body = createBody();

        File keyStore = new File(KEY_STORE_FILE);

        keyStoreExist = keyStore.exists();
        
        File stateFile = new File(GAME_STATE_FILE_ENC);
        
        saveSetateFile = stateFile.exists();

        getContentRoot().getChildren().addAll(titleView, body);
    }

    /**
     * This method creates the new game, load & start and exit buttons 
     * and attaches them to event handlers. The New Game event handler points 
     * to the fireNewGame method (from FXGLMenu) that corresponds to the button functionality
     * of starting a new Pong game. The load and exit buttons invoke the passwordBox method,
     * that will either decrypt and load the file, or save the file and exit.  
     * @return {v} VBox holding all menu action buttons
     */
    private Node createBody() {
        
        // Fire name game
        var newGame = createActionButton("New Game", this::fireNewGame);
        
        exit = new Button("Exit");
        
        // Set event on exit
        exit.setOnMouseClicked(e -> {
            
            // When keystore exist we can generate signature
            if (keyStoreExist) {
                passwordBox(1);
            
            // Dispaly message to acces user to start new game and create keystore.
            } else {
                displayMessage("Sorry you can't generate a signature. Start a new game to create a keystore.");
            }
        });

        load = new Button("Load & Start");
        
        // Load event
        load.setOnMouseClicked(event -> {
            
            // we only load when game state file exist
            if (saveSetateFile) {
                passwordBox(0);
            
                // Ask user to start a new game
            } else {
                displayMessage("No file to load! Please start a new game");
            }

        });

        VBox v = new VBox(newGame, load, exit);

        return v;
    }

    /**
     * Display the result of the digital signature verification.
     * @param message
     */
    private void displayMessage(String message) {
        getDialogService().showMessageBox(message);

    }
    
     /**
     * This method takes in a password as a parameter. It first
     * hashes the password, then verifies if it's valid. 
     * It accesses the keyStore to get the secret key, to decrypt
     * the file that saves the game and finally loads the game.
     * When the password is not valid or incorrect
     * the method will keep calling passwordBox method for the purpose of loading
     * the game state until the password is correct.
     * @param password user password
     */
    private void decryptAndLoad(String userPassword) {
        try {
            // hash password
            char[] hashed = hashPassword(userPassword);
            // ask user wfor password again
            if (hashed == null) {
                message.setText("Your password must contain at least 6 characters!");
                message.setTextFill(Color.rgb(210, 39, 30));
                passwordBox(0);
            }
            
            // Acces keyStore 
            PongKeyStore pks = new PongKeyStore(hashed);
            
            // Get secret key
            SecretKey secretK = pks.getSecretKey();
            
            // Symetric encryption class
            SymmetricCrypto sCrypto = new SymmetricCrypto(secretK);
            // read gcmiv file
            sCrypto.readOrStoreGCMIV();
            
            // initialize the decrypted file (game.sav)
            File input = new File(GAME_STATE_FILE_DEC);
            // initialize the encrypted file (game.sav.enc)
            File output = new File(GAME_STATE_FILE_ENC);
            // decrypt file
            sCrypto.decryptFile(output, input);
            // start a new game
            this.fireNewGame();
            
            // load the state in the decrypted file
            getSaveLoadService().readAndLoadTask(GAME_STATE_FILE_DEC).run();
            message.setText("");
        
            // Ask for password again
        } catch (Exception ex) {
            message.setText("Your password is incorrect!");
            message.setTextFill(Color.rgb(210, 39, 30));
            passwordBox(0);
        }
    }
      /**
     * This method access the pongPassword class
     * to normalize, validate, and hash the user password.
     * @param pass user password
     * @return a hashed password 
     */
    private char[] hashPassword(String pass) {
        PongPassword p = new PongPassword();
        char[] hashed = p.getHashedPassword(pass);
        return hashed;
    }

    /**
     * Creates a new button with given name that performs given action on
     * click/press.
     * @param name button name (with binding)
     * @param action button action
     * @return new button
     */
    private Node createActionButton(String name, Runnable action) {

        var btn = new Button(name);

        btn.setOnMouseClicked(e -> action.run());

        return btn;
    }

    /**
     * This method displays a dialog box that allows the user to enter the
     * password to access the keyStore. When the close button event is
     * triggered, it invokes the method that will either save and exit
     * (passwordFor == 0) or just exit(passwordFor > 0)
     * @param passwordFor
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
            if (passwordFor == 0) { // 0 for loading and starting a new game.

                // decrypt and laod.
                decryptAndLoad(userPassword);

            } else { // any other number for exiting and generating a digital signature.
                exitAndGenerateSignature(userPassword);
            }

            // Resume the game loop when the close button is triggered.
            getDialogService().onMainLoopResumed();
        });

        // Show The password DialogBox.
        getDialogService().showBox("Keystore Password", passwordV, btnClose);
    }
    
     /**
     * This method generate a signature before exiting program.
     * The method takes a password as a parameter, hash it and validate it.
     * If the hash is null, not valid or incorrect, the program keeps asking user
     * to enter a valid password until it successfully gets the private key.
     * After getting the private key from the keyStore, it generates the 
     * signature ,stores it in a file and exit program.
     * @param password
     */
    public void exitAndGenerateSignature(String password) {
        
        try {
            // hash password
            char[] hashed = hashPassword(password);
            
            // Ask for password when the hash is null
            if (hashed == null) {
                message.setText("Your password is incorrect!");
                message.setTextFill(Color.rgb(210, 39, 30));
                passwordBox(3);
            }
            
            // Access keystore
            PongKeyStore pks = new PongKeyStore(hashed);
            
            
            // Get private key
            PrivateKey privateK = pks.getPrivateKey();
            
            // Digital signature class
            PongDigitalSignature pdigital = new PongDigitalSignature();
            // generate signature
            byte[] ditalSig = pdigital.generateSignature(ECDSA_ALGORITHM, privateK, FILE_TO_SIGN);
            
            // Store signature
            pdigital.writeSignatureToFile(DIGITAL_SIGNATURE_FILE, ditalSig);
            
            // exit
            this.fireExit();
        
            // If something happens ask user for password
        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | UnrecoverableKeyException | CertificateException ex) {
            message.setText("Your password is incorrect!");
            message.setTextFill(Color.rgb(210, 39, 30));
            passwordBox(3);
        }
    }

}
