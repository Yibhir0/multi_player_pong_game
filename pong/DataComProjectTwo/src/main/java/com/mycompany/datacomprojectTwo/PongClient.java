package com.mycompany.datacomprojectTwo;

import com.almasb.fxgl.core.serialization.Bundle;
import static com.almasb.fxgl.dsl.FXGL.getDialogService;
import static com.almasb.fxgl.dsl.FXGL.getExecutor;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.getNetService;
import static com.almasb.fxgl.dsl.FXGL.getService;
import static com.almasb.fxgl.dsl.FXGL.getWorldProperties;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;
import static com.mycompany.datacomprojectTwo.entities.PongConfig.*;
import java.io.IOException;
import java.net.InetAddress;
import java.text.Normalizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class runs the client code. It establishes connection with the server.
 * and allows communication and replication. The class ensures that the IP
 * address provided is reachable.
 *
 * @author David and Yassine
 */
public class PongClient {

    // Stores the connection handle for connection between client and server
    private Connection<Bundle> connection;

    // Ip address
    private String ipAddress;

    // Logger for exceptions
    private static final Logger LOGGER = Logger.getLogger(PongClient.class.getName());

    /**
     * This method set up and start the client connection.
     */
    private void startClient() {
        //if (initializeSocket()) {
        var client = getNetService().newTCPClient(ipAddress, PORT);
        client.setOnConnected(conn -> {
            connection = conn;

            // Enable the client to receive data from the server.
            getExecutor().startAsyncFX(() -> onClient());
        });

        // Establish the connection to the server.
        client.connectAsync();

    }

    /**
     * Asks user to enter the IP address of the server. Normalize and validate
     * the user input and start the client connection if the IP is valid. the
     * method recursively calls its self when IP address is not valid. To determine
     * if IP address is reachable the method invokes another that pings the IP given by user,
     */
    public void setClientConnection() {

        // User input dialog box
        getDialogService().showInputBox("Enter the IP address of the server", serverIP -> {

            // Set ip address
            ipAddress = serverIP;

            // Normalize
            ipAddress = Normalizer.normalize(serverIP, Normalizer.Form.NFKC);

            // This is for the string localhost
            ipAddress = ipAddress.toLowerCase();
            
            if(ipAddress.equals("localhost")){
                ipAddress = "127.0.0.1";
            }
            
            // IPv4 address format: num_1.num_2.num_3.num_4, where num_n is at most 3 digits long 
            Pattern validServerIP = Pattern.compile("^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$");
            Matcher matcher = validServerIP.matcher(ipAddress);
                
            // not a valid IP (contain letters or does not conform to official format)
            if (!matcher.find()) {
                // Provide a message and ask for ip again to set connection.
                getDialogService().showErrorBox("The ip address is not valid! Try again.", () -> {
                    // Ask for ip again
                    setClientConnection();
                });

            } 
            // Check connection with the ip and port using socket
            else if (!pingIpAddress(ipAddress)) {
                // Provide a message and ask for ip again to set connection.
                getDialogService().showErrorBox("The ip address is not valid! Try again.", () -> {
                    // Ask for ip again
                    setClientConnection();
                });

            } else {
                // Start connection whe socket method returns true.
                this.startClient();

                }

        });

    }

    /**
     * This method allow the client to receive and send data to the server.
     */
    private void onClient() {

        // Allow replication receiver for game world
        getService(MultiplayerService.class).addEntityReplicationReceiver(connection, getGameWorld());

        // Allow replication receiver for game properties
        getService(MultiplayerService.class).addPropertyReplicationReceiver(connection, getWorldProperties());

        // Allow replication sender for client input
        getService(MultiplayerService.class).addInputReplicationSender(connection, getInput());
    }


    /**
     * This method pings the IP address to check if it can 
     * be reachable.
     * @param host
     * @return 
     */
    private boolean pingIpAddress(String host) {

        boolean isIpValid = false;

        try {
            
            // Init Address to ping
            InetAddress ping = InetAddress.getByName(host);

            // Timeout required - it's in milliseconds
            int timeout = 2000;
            // check if ip is reachable
            if (ping.isReachable(timeout)) {
                
                System.out.println("Pinging");
                isIpValid = true;
            }

        } catch (IOException e) {
             LOGGER.log(Level.SEVERE, "Invalid IP!!");
        }

        return isIpValid;
    }
}
