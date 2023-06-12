package com.mycompany.datacomprojectTwo.entities;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.*;
import static java.lang.Math.*;
import static com.mycompany.datacomprojectTwo.entities.PongConfig.*;

/**
 * This class deals with the ball movements.
 * It adjusts the ball's velocity and prevents it
 * from appearing off-screen.
 * @author Yassine Ibhir
*/
public class BallComponent extends Component {
    
    // Physics component
    private PhysicsComponent physics;
    
    
    /**
     * This overrides the Component onUpdate method
     * It runs every frame per second. It uses helper
     * method to track the ball's movements.
     * @param tpf 
     */
    @Override
    public void onUpdate(double tpf) {
        
        // We first check if the ball has the physics component (server and not client) 
        if(physics != null){
              limitVelocity();
              checkOffscreen();
          }
    }
    
    /**
     * This method ensures that the ball does not move
     * too slow in X direction and not to fast in y direction.
    */
    private void limitVelocity() {
        
        // we don't want the ball to move too slow in X direction
        if (abs(physics.getVelocityX()) < LINEAR_VELOCITY) {
            physics.setVelocityX(signum(physics.getVelocityX()) * LINEAR_VELOCITY);
        }

        // we don't want the ball to move too fast in Y direction
        if (abs(physics.getVelocityY()) > LINEAR_VELOCITY * 2) {
            physics.setVelocityY(signum(physics.getVelocityY()) * LINEAR_VELOCITY);
        }
    }
    
    /**
     * this is a hack:
     * we use a physics engine, so it is possible to push the ball through a wall to outside of the screen
     */
    
    private void checkOffscreen() {
        
        if (getEntity().getBoundingBoxComponent().isOutside(getGameScene().getViewport().getVisibleArea())) {
            physics.overwritePosition(new Point2D(
                    getAppWidth() / 2,
                    getAppHeight() / 2
            ));
        }
    }
}
