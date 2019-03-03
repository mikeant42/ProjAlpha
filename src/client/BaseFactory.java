package client;

import client.render.AnimatedMovementComponent;
import client.render.CombatComponent;
import client.render.ProjectileComponent;
import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IDComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;
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



        // We need to set the temp uid of the player so we are in sync with the client. This data is passed from the server.
        player.addComponent(movementComponent);

        player.addComponent(new IDComponent("player", data.get("ID")));

        player.addComponent(new NetworkedComponent(data.get("ID"), handler));
        //player.addComponent(new OverlayTextComponent(data.get("user")));


        player.setScaleX(playerScale);
        player.setScaleY(playerScale);

        player.setRenderLayer(RenderLayer.TOP);

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

        // We need to set the temp uid of the player so we are in sync with the client. This data is passed from the server.
        player.addComponent(movementComponent);

        player.addComponent(new IDComponent("player", data.get("ID")));
        player.addComponent(new NetworkedComponent(data.get("ID"), handler));
        player.addComponent(new CombatComponent(handler.getCharacterPacket().combat));
        //player.addComponent(new OverlayTextComponent(handler.getUsername()));

//        System.out.println(player.getBoundingBoxComponent().getMaxXWorld());
//        System.out.println(player.getBoundingBoxComponent().getMinXWorld());
//
//        System.out.println(player.getBoundingBoxComponent().getMinYWorld());
//        System.out.println(player.getBoundingBoxComponent().getMaxYWorld());


        player.setScaleX(playerScale);
        player.setScaleY(playerScale);

        player.setRenderLayer(RenderLayer.TOP);

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
                .bbox(new HitBox(BoundingShape.box(35, 40)))
                .with(new CollidableComponent(true))
                .type(EntityType.NPC)
                .build();



        npc.setRenderLayer(RenderLayer.TOP);

        npc.addComponent(new IDComponent("npc", data.get("ID")));
        npc.addComponent(movementComponent);

        return npc;
    }

    @Spawns("Gameobject")
    public Entity newGameObject(SpawnData data) {
        Entity entity;
        entity = Entities.builder()
                .from(data)
                .with(new NetworkedComponent(data.get("uid"), handler))
                .viewFromTexture("objects/" + data.get("name") + ".png")
                .build();

        entity.setRenderLayer(RenderLayer.TOP);

        return entity;
    }

    @Spawns("collide")
    public Entity newHut(SpawnData data) {
        return Entities.builder()
                .from(data)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .type(EntityType.COLLIDE)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("projectile")
    public Entity newProjectile(SpawnData data) {
        String fileName = "spell/" + data.get("name") + ".png";
        AnimationChannel channel = new AnimationChannel(fileName, 4,256/4, 64, Duration.seconds(1), 0, 3);
        Entity entity =  Entities.builder()
                .from(data)
                .bbox(new HitBox(BoundingShape.box(15,15))) // these need to be the same as the projectile
                //.with(new ProjectileComponent(data.get("mouseX"), data.get("mouseY"), 2))
                .with(new CollidableComponent(true))
                .with(new NetworkedComponent(data.get("uid"), handler))
                .with(new IDComponent("object", data.get("uid")))
                .viewFromAnimatedTexture(new AnimatedTexture(channel))
                .type(EntityType.PROJECTILE)
                //.bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                //.type(EntityType.COLLIDE)
                //.with(new CollidableComponent(true))
                .build();
        entity.setRenderLayer(RenderLayer.TOP);
        //entity.setProperty("doesOwn", data.get("doesOwn"));

        return entity;
    }


}
