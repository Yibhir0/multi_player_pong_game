package com.mycompany.datacomprojectTwo.entities;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.beans.binding.Bindings;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import static com.mycompany.datacomprojectTwo.entities.PongConfig.*;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getip;
import com.almasb.fxgl.multiplayer.NetworkComponent;

/**
 * Class builds and spawns the entities. The pong game entities are the ball,
 * the bats, and the walls. The factory does not spawn the client's entities
 * with the physics component.
 * @author Yassine Ibhir
 */
public class PongFactory implements EntityFactory {

    /**
     * This method builds and returns the ball entity. In the client side the
     * ball is pawned without physics. In the server side the ball is spawned
     * with the physics component.
     * @param data
     * @return Entity ball.
     */
    @Spawns("ball")
    public Entity newBall(SpawnData data) {

        // Determines if the ball belongs to server or client
        boolean isServer = data.hasKey("isServer");
        
        // Particle component (Emitter) 
        var endGame = getip(PLAYER1_SCORE).isEqualTo(EMITTER_SCORE).or(getip(PLAYER2_SCORE).isEqualTo(EMITTER_SCORE));

        ParticleEmitter emitter = ParticleEmitters.newFireEmitter();
        emitter.startColorProperty().bind(
                Bindings.when(endGame)
                        .then(Color.LIGHTYELLOW)
                        .otherwise(Color.LIGHTYELLOW)
        );

        emitter.endColorProperty().bind(
                Bindings.when(endGame)
                        .then(Color.RED)
                        .otherwise(Color.LIGHTBLUE)
        );

        emitter.setBlendMode(BlendMode.SRC_OVER);
        emitter.setSize(EMITTER_WIDTH, EMITTER_HEIGHT);
        emitter.setEmissionRate(EMITTER_RATE);
        
        // Spawn with physics
        if (isServer) {
            // Physics component
            PhysicsComponent physics = new PhysicsComponent();
            physics.setBodyType(BodyType.DYNAMIC);
            physics.setFixtureDef(new FixtureDef().density(DENSITY).restitution(RESTITUTION));
            physics.setOnPhysicsInitialized(() -> physics.setLinearVelocity(LINEAR_VELOCITY, -LINEAR_VELOCITY));
            return entityBuilder(data)
                    .type(EntityType.BALL)
                    .bbox(new HitBox(BoundingShape.circle(BALL_RADIUS)))
                    .with(physics)
                    .with(new CollidableComponent(true))
                    .with(new ParticleComponent(emitter))
                    .with(new BallComponent())
                    .with(new NetworkComponent()) //Needed for network service
                    .build();
        
        // Spawns without the physics
        } else {
            return entityBuilder(data)
                    .type(EntityType.BALL)
                    .bbox(new HitBox(BoundingShape.circle(BALL_RADIUS)))
                    .with(new CollidableComponent(true))
                    .with(new ParticleComponent(emitter))
                    .with(new BallComponent())
                    .with(new NetworkComponent()) //Needed for network service
                    .build();
        }

    }

    /**
     * This method determines which bat to spawn. When data has "isServer" key,
     * the server bat is spawned with the physics, otherwise the client's bat is
     * spawned without the physics.
     * @param data
     * @return the players Bat
     */
    @Spawns("bat")
    public Entity newBat(SpawnData data) {

        boolean isServer = data.hasKey("isServer");

        if (isServer) {
            // Spawn the server's bat
            return serverBat(data);
        }

        // Spawn the client's bat
        return clientBat(data);

    }

    /**
     * This method builds and returns the bat entity for the server. The
     * difference between this bat and the client's bat is the physics
     * component, which is only attached to the server bat.
     * @param data
     * @return Entity server Bat.
     */
    private Entity serverBat(SpawnData data) {

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);

        return entityBuilder(data)
                .type(EntityType.PLAYER_BAT)
                .viewWithBBox(new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT, Color.LIGHTGRAY))
                .with(new CollidableComponent(true))
                .with(physics)
                .with(new BatComponent())
                .with(new NetworkComponent()) //Needed for network service
                .build();
    }

    /**
     * This method builds and returns the client's bat entity.
     * This bat has not physics component attached to it.
     * @param data
     * @return Entity client Bat.
     */
    private Entity clientBat(SpawnData data) {
        return entityBuilder(data)
                .type(EntityType.PLAYER_BAT)
                .viewWithBBox(new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT, Color.LIGHTGRAY))
                .with(new CollidableComponent(true))
                .with(new BatComponent())
                .with(new NetworkComponent()) //Needed for network service
                .build();
    }

    /**
     * This method builds and returns the wall entity.
     * @param data
     * @return Wall Entity
     */
    @Spawns("walls")
    public Entity initScreenBounds(SpawnData data) {

        Entity walls = entityBuilder()
                .type(EntityType.WALL)
                .collidable()
                .with(new NetworkComponent())
                .buildScreenBounds(SCREEN_BOUNDS);

        return walls;

    }
}
