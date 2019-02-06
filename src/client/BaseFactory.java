package client;

import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import shared.EntityType;

public class BaseFactory implements EntityFactory {


    // We need this for networked comps
    private ClientHandler handler;

    public BaseFactory(ClientHandler handler) {
        this.handler = handler;
    }

    @Spawns("player")
    public Entity spawnPlayer(SpawnData data) {

        AnimatedMovementComponent movementComponent = new AnimatedMovementComponent("mage-light.png", 48, 64, 3);

        Entity player = Entities.builder()
                .at(data.getX(), data.getY())
                .bbox(new HitBox(BoundingShape.box(40,50)))
                .with(new CollidableComponent(true))
                       .build();
        player.setType(EntityType.PLAYER);



        // We need to set the temp id of the player so we are in sync with the client. This data is passed from the server.
        player.addComponent(new NetworkedComponent(data.get("ID"), handler));
        player.addComponent(movementComponent);

        return player;
    }

    @Spawns("localplayer")
    public Entity spawnLocalPlayer(SpawnData data) {
        AnimatedMovementComponent movementComponent = new AnimatedMovementComponent("mage-light.png", 48, 64, 3);

        Entity player = Entities.builder()
                .at(data.getX(), data.getY())
                .bbox(new HitBox(BoundingShape.box(40,50)))
                .with(new CollidableComponent(true))
                .build();
        player.setType(EntityType.LOCAL_PLAYER);

        // We need to set the temp id of the player so we are in sync with the client. This data is passed from the server.
        player.addComponent(new NetworkedComponent(data.get("ID"), handler));
        player.addComponent(movementComponent);

        return player;
    }

    @Spawns("Roaming NPC")
    public Entity spawnRoamingNPC(SpawnData data) {
        Entity npc = Entities.builder()
                .at(data.getX(), data.getY())
                .viewFromNode(new Rectangle(25, 25, Color.BLUE))
                //.build()
                //.viewFromNodeWithBBox(new Rectangle(25, 25, Color.BLUE))
                //.bbox(new HitBox(BoundingShape.box(25, 25)))
                .with(new CollidableComponent(true))
                .build();
        npc.setType(EntityType.ROAMING_NPC);

        npc.setProperty("ID", data.get("ID"));

        return npc;
    }

    @Spawns("hut")
    public Entity newHut(SpawnData data) {
        return Entities.builder()
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .type(EntityType.HUT)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("")
    public Entity newNothing(SpawnData data) {
        System.err.println("WARNING: an object with no spawn type has spawned.");
        return Entities.builder()
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .type(EntityType.HUT)
                .with(new CollidableComponent(true))
                .build();
    }
}
