package com.mycompany.datacomprojectTwo;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import static com.almasb.fxgl.app.GameApplication.launch;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import static com.almasb.fxgl.dsl.FXGL.*;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;
import com.almasb.fxgl.ui.UI;
import static com.mycompany.datacomprojectTwo.crypto.CryptoConfig.*;
import com.mycompany.datacomprojectTwo.crypto.PongDigitalSignature;
import com.mycompany.datacomprojectTwo.crypto.PongKeyStore;
import com.mycompany.datacomprojectTwo.crypto.PongPassword;
import com.mycompany.datacomprojectTwo.entities.EntityType;
import static com.mycompany.datacomprojectTwo.entities.PongConfig.*;
import com.mycompany.datacomprojectTwo.entities.PongFactory;
import com.mycompany.datacomprojectTwo.menus.PongGameMenu;
import com.mycompany.datacomprojectTwo.menus.PongMainMenu;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * JavaFx main class uses FXGL (JavaFX Game Library) to build a multiplayer Pong
 * Game. The class extends GameApplication and implements the methods needed to
 * run and manage the game loop.
 * @author Yassine Ibhir, David Pizzolongo, and the author of FXGL, AlmasB.
 */
public class PongApp extends GameApplication {

    // Flags when server  running
    private boolean isServer = false;

    // Needed to handle input to the client.
    private Input clientInput;

    // Pong game menu
    private PongGameMenu gameMenu;
    
    // Pong main menu
    private PongMainMenu mainMenu;

    // Flags when game is over
    private boolean gameOver;
    
    // Label for password message
    private Label message;
    
    // Stores the server score
    private int server_final_score;
   
    /**
     * Set the game settings. Title, version, menus and add multiplayer service
     * engine.
     * @param settings of the game
     */
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Multiplayer Pong Game");
        settings.setVersion("1.0");
        settings.setFontUI("pong.ttf");
        settings.addEngineService(MultiplayerService.class);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        
        // Custom menus
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newGameMenu() {
                gameMenu = new PongGameMenu();
                return gameMenu;
            }

            @Override
            public FXGLMenu newMainMenu() {
                
                return new PongMainMenu();
                
            }
        });
    }

    /**
     * Initialize the game properties.
     * @param vars Map stores properties and values
     */
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put(PLAYER1_SCORE , 0);
        vars.put(PLAYER2_SCORE , 0);
    }

    /**
     * Initialize and start interacting with the user. The method asks users if
     * there are the host or not. The server and client code is different. The
     * server's contain all the game logic. The client replicates the server
     * code.
     */
    @Override
    protected void initGame() {
        
        // Set background color
        getGameScene().setBackgroundColor(Color.rgb(0, 0, 5));

        // This line is needed in order for entities to be spawned 
        getGameWorld().addEntityFactory(new PongFactory());

        // set event to end the game
        setGameOverEvent();
        
        // Ask User if there are the host
        runOnce(() -> {
            getDialogService().showConfirmationBox("Are you the host?", yes -> {
                
                // Set the value to the user choice.
                isServer = yes;

                if (isServer) {
                    
                    // Initialize label for password message.
                    message = new Label("");
                    
                    // Locate the keyStore file
                    File keyStre = new File(KEY_STORE_FILE);

                    // If the keyStore does not exist the signature won't exist either.
                    if (!keyStre.exists()) {
                        // We ask user to provide a password to create a keyStore. 
                        passwordBox(0); 
                    }
                    // The keyStore exist so does the signature.
                    else {
                        // We ask user to provide a password to virify signature . 
                        passwordBox(1);                    }

                } else {
                    // When user is not the host we set connection on client.
                    initializeClient();
                }
            });
        }, Duration.seconds(0.5));
    }

    /**
     * Initialize client and set parameters needed for client side code. 
     * Start connection with the server.
    */
    private void initializeClient() {
        
        // Change the name of the gameMenu buttons 
        gameMenu.changeButtonTexts();
        
        // Create a UI for the client menu.
        HBox clientMenu = new HBox(new Text("Press SPACE to pause"),new Text("Press R to resume"),new Text("Press S to Save"),new Text("Press X to exit"));
        
        clientMenu.setSpacing(100);
        
        clientMenu.setStyle( " -fx-font-size: 14px; -fx-font-weight:bold;");
       
        clientMenu.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
        
        // Add UI to the client scene.
        getGameScene().addUINode(clientMenu);
        
        // Create a cielnt Instance
        PongClient client = new PongClient();

        // Set Client Conenction .
        client.setClientConnection();

    }

    /**
     * Initialize server and set parameters needed for server side code. 
     * start connection on the server.
     */
    private void initializeServer() {
        
        // Set the event to consume the close window buuton.
        closeEvent();
        
        // Initialize Client Input
        clientInput = new Input();
        
        // Provide the input to the game menu.
        gameMenu.setGameMenuInput(clientInput);
        
        // set server to true in game menu
        gameMenu.setServer(isServer);
 
        // Create server instance with the client Input
        PongServer server = new PongServer(clientInput);

        // SetUp server connection 
        server.setUpServerConnection();

    }

    /**
     * Display the message in a dialog box.
     * @param message
     */
    private void displayMessage(String message) {
        getDialogService().showMessageBox(message);

    }

    /**
     * Create keyStore for the first time. With all cryptographic keys.
     * The method takes a password as parameter, hash it and validate it.
     * If the hash is null or not valid, the program keeps asking user
     * to enter a valid password until they keyStore is successfully created.
     * After creating a keyStore, the method will initialize server connection.
     * @param password
     */
    private void createKeyStore(String password) {
        try {
            // Normalize, validate, and hash password.
            char[] hashed = hashPassword(password);
            
            // Provide a message and ask for password again.
            if (hashed == null) {
                message.setText("Your password must contain at least 6 characters!");
                message.setTextFill(Color.rgb(210, 39, 30));
                passwordBox(0);
            
            
            } else {
                // Create a new keystore 
                PongKeyStore pks = new PongKeyStore(hashed, KEY_TOOL_CMD);
                pks.createAndStoreKeys();
                // Inform user
                displayMessage("Keystore was created.");
                // Start connection on the server.
                initializeServer();
            }
            // Remove the password message.
            message.setText("");
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
            passwordBox(0);
        }
    }

    /**
     * This method handles the events that requires a password.
     * The events are labeled with an integer which is  given
     * as a parameter. 0  for creating a keyStore, 1 for verifying the
     * signature and 2 or higher is for exiting/generating a signature.
     * The method creates a password dialog box and handles those events.
     * @param passwordFor defines the method to be called with the password.
    */
    private void passwordBox(int passwordFor) {
        
        // Password field
        PasswordField pb = new PasswordField();
        
        // Vbox with password field and password message
        VBox passwordV = new VBox(pb,message);
        
        // Password button
        Button passwordBtn = getUIFactoryService().newButton("OK");

        passwordBtn.setPrefWidth(300);
        
        // Handle the password event
        passwordBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                
                // Get the password entered.
                String userPassword = pb.getText();
                
                switch (passwordFor) {
                    case 0:
                        // 0 for creating a keystore
                        createKeyStore(userPassword);
                        break;
                    case 1:
                        // 1 for verifying a signature
                        verifySignature(userPassword);
                        break;
                        // any other number for exiting and generating a keystore
                    default:
                        exitAndGenerateSignature(userPassword);
                        break;
                }
                
                // Closes the password box.
                getDialogService().onMainLoopResumed();

            }
        });
        
        // Display the password box.
        getDialogService().showBox("Keystore Password", passwordV, passwordBtn);
    }
    
    /**
     * This method ensure that the signature gets
     * generated when the game is over. It sets game over to true
     * to acknowledge the state. It invokes passwordBox for generating
     * the signature if user is the host. It displays a message with the winner
     * to the client.
     * @param winner 
     */
    private void showGameOver(String winner) {
        
        // Set game over to true
        gameOver = true;
        
        // Call password to generate key and exit program 
        if(isServer){
            passwordBox(3);
        }
        
        // Else thank client for playing
        else{
          thankUsers(winner);  
        }

    }
    /**
     * Display dialog with the winner, and exit the game.
     * @param winner
    */
    private void thankUsers(String winner){
        getDialogService().showMessageBox(winner + " won! Game over\nThanks for playing", getGameController()::exit);
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
    private void exitAndGenerateSignature(String password) {
        
        try {
            // hash password
            char[] hashed = hashPassword(password);
            
            // Ask user for password when it's not valid
            if (hashed == null) {
                message.setText("Your password is incorrect!");
                message.setTextFill(Color.rgb(210, 39, 30));
                passwordBox(3);
            }
            
            // Access keyStore
            PongKeyStore pks = new PongKeyStore(hashed);
            
            // Get private key
            PrivateKey privateK = pks.getPrivateKey();
            
            // Initialiaze digital signature class
            PongDigitalSignature pdigital = new PongDigitalSignature();
            
            // Invoke method to generate signature with SHA256withECDSA alg,private key, and pangApp file.
            byte[] ditalSig = pdigital.generateSignature(ECDSA_ALGORITHM, privateK, FILE_TO_SIGN);
            
            // Write signature to the file system.
            pdigital.writeSignatureToFile(DIGITAL_SIGNATURE_FILE, ditalSig);
            
            // Check if game is over and thank user before exiting
            if (gameOver) {
                    if (server_final_score < 11) {
                        thankUsers("Client");
                    }
                    else {thankUsers("Server");} 
                }
            else{
                getGameController().exit();
            }
        
        // When password is incorrect, ask user again.
        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | UnrecoverableKeyException | CertificateException ex) {
            message.setText("Your password is incorrect!");
            message.setTextFill(Color.rgb(210, 39, 30));
            System.out.println(ex.toString());
            
            passwordBox(3);
        }
    }

    /**
     * Method verifies signature of pongApp.java 
     * and informs user wether file was altered or not.
     * The method takes a password as a parameter, hash it and validate it.
     * If the hash is null, not valid, or incorrect, the program keeps asking user
     * to enter a valid password until it successfully gets the public key.
     * After getting the public key from the keyStore, it verifies the 
     * signature. After verifying and informing the user about the signature.
     * The program starts the server code.
     * @param password
     */ 
    private void verifySignature(String userPassword) {
        try {
            
            // Hash password 
            char[] hashed = hashPassword(userPassword);
            
            // Ask user for password again to verify signature.
            if (hashed == null) {
                message.setText("Your password must contain at least 6 characters!");
                message.setTextFill(Color.rgb(210, 39, 30));
                passwordBox(1);
            // Verify signature
            } else {
                
                // Access keystore
                PongKeyStore pks = new PongKeyStore(hashed);
                
                // get public key
                PublicKey publicK = pks.getPublicKey();
                
                // Initialize digital signature class
                PongDigitalSignature pdigital = new PongDigitalSignature();
                
                // Read the digital signature that was generated when program exited
                byte[] d = pdigital.readSignatureFromFile(DIGITAL_SIGNATURE_FILE);
                
                // Verifies signature with original file and public key
                boolean isSignatureValid = pdigital.verifySignature(d, publicK, ECDSA_ALGORITHM, FILE_TO_SIGN);
                
                // Display message about the integrity of the file
                if (isSignatureValid) {
                    displayMessage("Signature is valid");
                } else {
                    displayMessage("Signature is not valid");
                }
                
                // Initialize server connection
                initializeServer();
                
                // reset password message
                message.setText("");
            }
        // Ask user again when password is incorrect    
        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | CertificateException ex) {
            gameMenu.logError(Logger.getLogger(PongApp.class.getName()));
            message.setText("Your password is incorrect. Try again!");
            message.setTextFill(Color.rgb(210, 39, 30));
            passwordBox(1);
            
        }
    }
    /**
     * This method uses the ponPassword class
     * to normalize, validate and hash the password.
     * @param pass
     * @return hashed password
     */
    private char[] hashPassword(String pass) {
        PongPassword p = new PongPassword();
        char[] hashed = p.getHashedPassword(pass);
        return hashed;
    }

    /**
     * Initialize UI and add it the game scene.
     */
    @Override
    protected void initUI() {
        
        MainUIController controller = new MainUIController();
        UI ui = getAssetLoader().loadUI("main.fxml", controller);

        controller.getLabelScorePlayer().textProperty().bind(getip(PLAYER1_SCORE).asString());
        controller.getLabelScoreEnemy().textProperty().bind(getip(PLAYER2_SCORE).asString());
        
        getGameScene().addUI(ui);
    }

    /**
     * Initialize the physics world with collision handlers that update the
     * score and apply animation on the bats.
     */
    @Override
    protected void initPhysics() {

        getPhysicsWorld().setGravity(0, 0);
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BALL, EntityType.WALL) {
            @Override
            protected void onHitBoxTrigger(Entity a, Entity b, HitBox boxA, HitBox boxB) {
                if (boxB.getName().equals("LEFT")) {
                    inc(PLAYER2_SCORE, +1);
                } else if (boxB.getName().equals("RIGHT")) {
                    inc(PLAYER1_SCORE, +1);
                }
                getGameScene().getViewport().shakeTranslational(5);
            }
        });

        // Initialize the colission handler
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER_BAT, EntityType.BALL) {

            //Order of types isSignatureValid the same as passed into the constructor
            @Override
            protected void onCollisionBegin(Entity bat, Entity ball) {

                playHitAnimation(bat);

            }
        });
    }

    /**
     * Add event listener for the the end of the game.
     * tracks server final score.
     */
    private void setGameOverEvent() {
        getWorldProperties().<Integer>addListener(PLAYER1_SCORE, (old, newScore) -> {
            if (newScore == 11) {
                // server final score
                server_final_score = newScore;
                showGameOver("Server");
            }
        });

        getWorldProperties().<Integer>addListener(PLAYER2_SCORE, (old, newScore) -> {
            if (newScore == 11) {
                showGameOver("Client");
            }
        });
    }

    /**
     * Animation for the bat
     * @param bat 
     */
    private void playHitAnimation(Entity bat) {
        animationBuilder()
                .autoReverse(true)
                .duration(Duration.seconds(0.5))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .rotate(bat)
                .from(FXGLMath.random(-25, 25))
                .to(0)
                .buildAndPlay();
    }

    /**
     * This method runs every fps. It Make sure that the server is running so
     * that the state is only updated in the server and get synchronized in both
     * end.
     * @param tpf
     */
    @Override
    protected void onUpdate(double tpf) {

        // Update the client input
        if (isServer && clientInput != null) {
            clientInput.update(tpf);
        }
    }

    /**
     * Method set the event handler for saving and loading the game state.
     */
    @Override
    protected void onPreInit() {
        getSaveLoadService().addHandler(new SaveLoadHandler() {
            @Override
            public void onSave(DataFile data) {
                // create a new bundle to store your data
                var bundle = new Bundle("gameData");

                // store score 1
                int score1 = geti(PLAYER1_SCORE);
                bundle.put(PLAYER1_SCORE, score1);

                // store score 2
                int score2 = geti(PLAYER2_SCORE);
                bundle.put(PLAYER2_SCORE, score2);

                // Give the bundle to data file
                data.putBundle(bundle);
            }

            @Override
            public void onLoad(DataFile data) {
                // get your previously saved bundle
                var bundle = data.getBundle("gameData");

                // retrieve score 1
                int score1 = bundle.get(PLAYER1_SCORE);
                // retrieve score 2
                int score2 = bundle.get(PLAYER2_SCORE);

                // Set the score to the world properties
                getWorldProperties().setValue(PLAYER1_SCORE, score1);
                getWorldProperties().setValue(PLAYER2_SCORE, score2);

            }
        });
    }
    
  
    
    /**
     * This method set an event on the windows close button
     * to make sure signature gets generated before exiting.
     */
    private  void closeEvent(){
       
        // Set event on the close window.
        getGameScene().getRoot().getScene().getWindow().setOnCloseRequest(event -> {
            
            // consume the event.
            event.consume();
            
            // Ask for password to generate signature.
            passwordBox(3);
        
        });   
    }
    
    /**
     * Main program method. 
     * @param args 
     */
    public static void main(String[] args) {
        launch(args);
        
    }
}
