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

    private float playerScale = 0.8f;

    public BaseFactory(ClientHandler handler) {
        this.handler = handler;
    }

    @Spawns("player")
    public Entity spawnPlayer(SpawnData data) {

        AnimatedMovementComponent movementComponent = new AnimatedMovementComponent("player/mage-light.png", 48, 64, 3);
        movementComponent.setIdle(7,8);
        movementComponent.setForward(0,2);
        movementComponent.setRight(3,5);
        movementComponent.setBack(6,8);
        movementComponent.setLeft(9,11);

        Entity player = Entities.builder()
                .at(data.getX(), data.getY())
                .bbox(new HitBox(BoundingShape.box(35,40)))
                .with(new CollidableComponent(true))
                       .build();
        player.setType(EntityType.PLAYER);



        // We need to set the temp id of the player so we are in sync with the client. This data is passed from the server.
        player.addComponent(movementComponent);

        player.addComponent(new NetworkedComponent(data.get("ID"), handler));
        player.addComponent(new OverlayTextComponent(data.get("user")));


        player.setScaleX(playerScale);
        player.setScaleY(playerScale);

        return player;
    }

    @Spawns("localplayer")
    public Entity spawnLocalPlayer(SpawnData data) {
        AnimatedMovementComponent movementComponent = new AnimatedMovementComponent("player/mage-light.png", 48, 64, 3);
        movementComponent.setIdle(7,8);
        movementComponent.setForward(0,2);
        movementComponent.setRight(3,5);
        movementComponent.setBack(6,8);
        movementComponent.setLeft(9,11);

        Entity player = Entities.builder()
                .at(data.getX(), data.getY())
                .bbox(new HitBox(BoundingShape.box(35,40)))
                .with(new CollidableComponent(true))
                .build();
        player.setType(EntityType.LOCAL_PLAYER);

        // We need to set the temp id of the player so we are in sync with the client. This data is passed from the server.
        player.addComponent(movementComponent);

        player.addComponent(new NetworkedComponent(data.get("ID"), handler));
        player.addComponent(new OverlayTextComponent(handler.getUsername()));

//        System.out.println(player.getBoundingBoxComponent().getMaxXWorld());
//        System.out.println(player.getBoundingBoxComponent().getMinXWorld());
//
//        System.out.println(player.getBoundingBoxComponent().getMinYWorld());
//        System.out.println(player.getBoundingBoxComponent().getMaxYWorld());


        player.setScaleX(playerScale);
        player.setScaleY(playerScale);

        return player;
    }

    @Spawns("Roaming NPC")
    public Entity spawnRoamingNPC(SpawnData data) {
        AnimatedMovementComponent movementComponent = new AnimatedMovementComponent("npc/googon.png", 48, 64, 3);
        movementComponent.setIdle(7,8);
        movementComponent.setForward(0,2);
        movementComponent.setRight(3,5);
        movementComponent.setBack(6,8);
        movementComponent.setLeft(9,11);

        Entity npc = Entities.builder()
                .at(data.getX(), data.getY())
                //.viewFromNode(new Rectangle(25, 25, Color.BLUE))
                //.build()
                //.viewFromNodeWithBBox(new Rectangle(25, 25, Color.BLUE))
                //.bbox(new HitBox(BoundingShape.box(25, 25)))
                //.with(new CollidableComponent(true))
                .build();
        npc.setType(EntityType.ROAMING_NPC);

        npc.setProperty("ID", data.get("ID"));
        npc.addComponent(movementComponent);

        return npc;
    }

    @Spawns("Gameobject")
    public Entity newGameObject(SpawnData data) {
        return Entities.builder()
                .from(data)
                .viewFromNode(new Rectangle(25, 25, Color.BLUE))
                .with(new NetworkedComponent(data.get("uid"), handler))
                .build();
    }

    @Spawns("collide")
    public Entity newHut(SpawnData data) {
        return Entities.builder()
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .type(EntityType.Collidable)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("")
    public Entity newNothing(SpawnData data) {
        System.err.println("WARNING: an object with no spawn type has spawned.");
        return Entities.builder()
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .type(EntityType.Collidable)
                .with(new CollidableComponent(true))
                .build();
    }


}
