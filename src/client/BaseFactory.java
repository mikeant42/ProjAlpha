package client;

import client.render.AnimatedMovementComponent;
import client.render.CombatComponent;
import client.render.ProjectileComponent;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IDComponent;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import shared.EntityType;


import static com.almasb.fxgl.app.DSLKt.play;
import static com.almasb.fxgl.app.DSLKt.texture;

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
                .bbox(new HitBox(BoundingShape.box(30,30)))
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
                .bbox(new HitBox(BoundingShape.box(30,30)))
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
        AnimatedMovementComponent movementComponent = new AnimatedMovementComponent("npc/"+data.get("name")+".png", 48, 64, 3);
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
                .bbox(new HitBox(BoundingShape.box(30, 30)))
                .with(new CollidableComponent(true))
                .type(data.get("type"))
                .build();



        npc.setRenderLayer(RenderLayer.TOP);

        npc.addComponent(new IDComponent("npc", data.get("ID")));
        npc.addComponent(movementComponent);

        return npc;
    }

    @Spawns("Standing NPC")
    public Entity spawnStandingNPC(SpawnData data) {
//        AnimatedMovementComponent movementComponent = new AnimatedMovementComponent("npc/googon.png", 48, 64, 3);
//        movementComponent.setIdle(7,8);
//        movementComponent.setForward(0,2);
//        movementComponent.setRight(3,5);
//        movementComponent.setBack(6,8);
//        movementComponent.setLeft(9,11);

        Entity npc = Entities.builder()
                .at(data.getX(), data.getY())
                //.viewFromNode(new Rectangle(25, 25, Color.BLUE))
                //.build()
                //.viewFromNodeWithBBox(new Rectangle(25, 25, Color.BLUE))
                .bbox(new HitBox(BoundingShape.box(30, 30)))
                .with(new CollidableComponent(true))
                .viewFromTexture("npc/" + data.get("name") + ".png")
                .type(data.get("type"))
                .build();



        npc.setRenderLayer(RenderLayer.TOP);

        if ((boolean)data.get("interactable") == true) {
            npc.setType(EntityType.INTERACTABLE_NPC);
        }


        npc.addComponent(new IDComponent("npc", data.get("ID")));
        //npc.addComponent(movementComponent);

        return npc;
    }

    @Spawns("Gameobject")
    public Entity newGameObject(SpawnData data) {
        Entity entity;
        entity = Entities.builder()
                .from(data)
                .with(new NetworkedComponent(data.get("uid"), handler))
                //.with(new IDComponent("object", data.get("uid"))) // this is needed for us to update the object
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
                .with(new ProjectileComponent(data.get("velX"), data.get("velY")))
                .with(new CollidableComponent(true))
                .with(new NetworkedComponent(data.get("uid"), handler))
                .with(new IDComponent("object", data.get("uid")))
                .with(new ExpireComponent(2))
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

    @Spawns("spell impact")
    public Entity newImpactEffect(SpawnData data) {
        String fileName = "particle/impact.png";
        AnimationChannel channel = new AnimationChannel(fileName, 6,600/6, 600/6, Duration.seconds(0.3), 0, 35);
        Entity entity =  Entities.builder()
                .from(data)
                .viewFromAnimatedTexture(new AnimatedTexture(channel))
                .type(EntityType.PARTICLE)
                .with(new ExpireComponent(0.3))
                //.with(new ExpireCleanControl(Duration.seconds(1.8))
                //.bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                //.type(EntityType.COLLIDE)
                //.with(new CollidableComponent(true))
                .build();
        entity.setRenderLayer(RenderLayer.TOP);


        //entity.setProperty("doesOwn", data.get("doesOwn"));

        return entity;
    }

    @Spawns("walking effect")
    public Entity spawnDirt(SpawnData data) {
       // play("explosion-0" + (int) (Math.random() * 8 + 1) + ".wav");

        // explosion particle effect
        ParticleEmitter emitter = ParticleEmitters.newSmokeEmitter();
        emitter.setSize(10, 15);
        emitter.setNumParticles(20);
        emitter.setExpireFunction(i -> Duration.seconds(0.2));
        emitter.setVelocityFunction(i -> Vec2.fromAngle(360 / 24 *i).toPoint2D().multiply(FXGLMath.random(45, 50)));
        emitter.setBlendMode(BlendMode.SRC_OVER);
        emitter.setStartColor(Color.GREEN);
        emitter.setEndColor(Color.GREEN.interpolate(Color.BROWN, 0.5));
        emitter.setSourceImage(FXGL.getAssetLoader().loadImage("particle/dirt.png"));

//        ParticleControl control = new ParticleControl(emitter);
//
//        Entity explosion = Entities.builder()
//                .at(data.getX() - 5, data.getY() - 10)
//                .with(control)
//                .buildAndAttach();
//
//        control.setOnFinished(explosion::removeFromWorld);


        return Entities.builder()
                .at(data.getX(), data.getY())
                //.viewFromNode(texture("explosion.png", 80 * 48, 80).toAnimatedTexture(48, Duration.seconds(2)))
                .with(new ParticleComponent(emitter))
                .with(new ExpireComponent(0.4))
                //.with(new ExpireCleanControl(Duration.seconds(1.8)))
                .build();
    }


}
