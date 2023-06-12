package com.mycompany.datacomprojectTwo;


import com.mycompany.datacomprojectTwo.entities.BatComponent;
import com.almasb.fxgl.core.serialization.Bundle;

import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getExecutor;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.getNetService;
import static com.almasb.fxgl.dsl.FXGL.getService;
import static com.almasb.fxgl.dsl.FXGL.getWorldProperties;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;

import static com.mycompany.datacomprojectTwo.entities.PongConfig.*;
import javafx.scene.input.KeyCode;

/**
 * This class executes server code only.
 * It contains methods that spawn entities, set input events,
 * and allow replication on the server.
 * @author Yassine Ibhir and David Pizzolongo
 */
public class PongServer {

    // Needed to handle input to the client.
    private Input clientInput;

    // Stores the connection handle for connection between client and server
    private Connection<Bundle> connection;

    // Player One Bat (Server)
    private BatComponent playerOneBat;

    // Player Two Bat (Client)
    private BatComponent playerTwoBat;

    // Pong Ball
    private Entity ball;

    /**
     *
     * @param input
     */
    public PongServer(Input input) {
        this.clientInput = input;
    }

    public void setUpServerConnection() {

        // Setup the TCP port that the server will listen at.
        var server = getNetService().newTCPServer(PORT);
        server.setOnConnected(conn -> {
            connection = conn;

            // Setup the entities and other necessary items on the server.
            getExecutor().startAsyncFX(() -> onServer());
        });

        // Start listening on the specified TCP port.
        server.startAsync();
    }

    /**
     * Spawn all entities in server and use multiplayer service to allow
     * spawning in client. Add Input, and property replication with the
     * established connection. Invoke
     */
    private void onServer() {
        
        // Spaw entities
        initPongObjects();
        // server input events
        initServerInputs();
        
        // client Input events
        initClientInputs();
        
        // Multiplayer service replication
        getService(MultiplayerService.class).addInputReplicationReceiver(connection, clientInput);
        getService(MultiplayerService.class).addPropertyReplicationSender(connection, getWorldProperties());

    }

    /**
     * Spawn all entities in server and use multiplayer service to allow
     * spawning in client. Add Input, and property replication with the
     * established connection.
     */
    private void initPongObjects() {

        ball = spawn("ball", new SpawnData(getAppWidth() / 2 - 5, getAppHeight() / 2 - 5).put("isServer", true));

        getService(MultiplayerService.class).spawn(connection, ball, "ball");

        Entity bat1 = spawn("bat", new SpawnData(getAppWidth() / 4, getAppHeight() / 2 - 30).put("isServer", true));
        getService(MultiplayerService.class).spawn(connection, bat1, "bat");

        Entity bat2 = spawn("bat", new SpawnData(3 * getAppWidth() / 4 - 20, getAppHeight() / 2 - 30).put("isServer", false));
        getService(MultiplayerService.class).spawn(connection, bat2, "bat");

        Entity wall = spawn("walls", new SpawnData(0, 0));
        //getService(MultiplayerService.class).spawn(connection, wall, "walls");

        playerOneBat = bat1.getComponent(BatComponent.class);
        playerTwoBat = bat2.getComponent(BatComponent.class);
    }
    
    /**
     * Server Input events. The events
     * handles the bat's movements (server).
     */
    private void initServerInputs() {
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                playerOneBat.up();
            }

            @Override
            protected void onActionEnd() {
                playerOneBat.stop();
            }
        }, KeyCode.UP);

        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                playerOneBat.down();
            }

            @Override
            protected void onActionEnd() {
                playerOneBat.stop();
            }
        }, KeyCode.DOWN);

    }
    
    /**
     * Client input events. The input will
     * get replicated in the server. The events
     * handles the bat's movements (Client).
     */
    private void initClientInputs() {
        
        clientInput.addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                playerTwoBat.up();
            }

            @Override
            protected void onActionEnd() {
                playerTwoBat.stop();
            }
        }, KeyCode.UP);

        clientInput.addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                playerTwoBat.down();
            }

            @Override
            protected void onActionEnd() {
                playerTwoBat.stop();
            }
        }, KeyCode.DOWN);
        
    }

}
