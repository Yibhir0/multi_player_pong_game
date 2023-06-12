package com.mycompany.datacomprojectTwo.entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import static com.mycompany.datacomprojectTwo.entities.PongConfig.*;

/**
 * This class tracks the bat's movement. 
 * @author Yassine
 */
public class BatComponent extends Component {

    // Physics component
    protected PhysicsComponent physics;
    
    /**
     * Bat moving up or stopping.
     */
    public void up() {
        if (entity.getY() >= PADDLE_SPEED / 60)
            physics.setVelocityY(-PADDLE_SPEED);
        else
            stop();
    }
    
    /**
     * Bat Moving down or stopping
     */
    public void down() {
        if (entity.getBottomY() <= FXGL.getAppHeight() - (PADDLE_SPEED / 60))
            physics.setVelocityY(PADDLE_SPEED);
        else
            stop();
    }
    
    /**
     * Stop the ball.
     */
    public void stop() {
        physics.setLinearVelocity(0, 0);
    }
}
