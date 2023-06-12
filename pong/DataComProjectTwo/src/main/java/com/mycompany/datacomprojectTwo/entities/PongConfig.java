
package com.mycompany.datacomprojectTwo.entities;

/**
 * This static class contains all constants
 * variables used in the pong game.
 * @author Yassine Ibhir
 */
public final class PongConfig {
    
    private PongConfig(){}
    
    // Network config...
    
    // Port number
    public static final int PORT = 7777;
    
    // Local Host default
    public static final String LOCAL_IP_ADDRESS = "localhost";
    
    // Bat config...
    
    // Paddle width
    public static final int PADDLE_WIDTH = 20;

    // Paddle Heigth
    public static final int PADDLE_HEIGHT = 60;
    
    // Paddle Speed
    public static final int PADDLE_SPEED = 420;

    // Ball config...
    
    // Ball size
    public static final int BALL_SIZE = 20;
    
    // Ball speed
    public static final int BALL_SPEED = 5;
    
    // Ball Density
    public static final float DENSITY = 0.3f;
    
    //  Ball restitution
    public static final float RESTITUTION = 1.0f;
    
    // Linear velocity
    public static final int LINEAR_VELOCITY = 5 * 60;
    
    // Ball radius
    public static final double BALL_RADIUS = 5;
    
    // Screen bounds
    public static final int SCREEN_BOUNDS = 150;
    
    // Emitter config...
    
    // Score to change the emitter color
    public static final int EMITTER_SCORE = 10;
    
    // EMITTER WIDTH
    public static final int EMITTER_WIDTH= 5;
    
    // EMITTER HEIGHT
    public static final int EMITTER_HEIGHT= 10;
    
    // EMITTER emission rate
    public static final int EMITTER_RATE= 1;
    
    // Player 1 score key name
    public static final String PLAYER1_SCORE = "score1";
    
    // Player 2 score key name
    public static final String PLAYER2_SCORE = "score2";

}
