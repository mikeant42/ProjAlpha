package client;

import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyDef;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import shared.CharacterPacket;

public class BaseFactory implements EntityFactory {


    // We need this for networked comps
    private ClientHandler handler;

    public BaseFactory(ClientHandler handler) {
        this.handler = handler;
    }

    @Spawns("player")
    public Entity spawnPlayer(SpawnData data) {
        Entity player = Entities.builder()
                .at(data.getX(), data.getY())
                .viewFromNodeWithBBox(new Rectangle(25, 25, Color.BLUE))
                //.bbox(new HitBox(BoundingShape.box(25, 25)))
                .with(new CollidableComponent(true))
                       .build();
        player.setType(EntityType.PLAYER);

        // We need to set the temp id of the player so we are in sync with the client. This data is passed from the server.
        player.addComponent(new NetworkedComponent(data.get("ID"), handler));
        player.addComponent(new MovementComponent(10));
        return player;
    }

    @Spawns("hut")
    public Entity newHut(SpawnData data) {
        System.err.println("spawned a hut");
        return Entities.builder()
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .type(EntityType.HUT)
                .with(new CollidableComponent(true))
                .build();
    }
}
