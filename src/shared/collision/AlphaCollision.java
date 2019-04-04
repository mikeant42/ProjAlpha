package shared.collision;

import client.render.AnimatedMovementComponent;
import client.MovementComponent.*;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.IDComponent;
import com.almasb.fxgl.parser.tiled.TiledObject;
import com.almasb.fxgl.physics.CollisionHandler;
import shared.*;

import java.util.Collection;
import java.util.List;

public class AlphaCollision {

    private AlphaCollisionHandler handler;
    private static int playerWidth = 30;
    private static int playerHeight = 30;

    public AlphaCollision(AlphaCollisionHandler handler) {
        this.handler = handler;
    }
    public static CollisionHandler setClientCollision(EntityType player, EntityType hut) {
        return new CollisionHandler(EntityType.LOCAL_PLAYER, EntityType.COLLIDE) {

            /*
            This is sort of a home cooked collision system. There is a bug with this system that causes the player to sometimes get stuck if they are colliding to the right or left, but
            they cannot move up or down.
            This collision system is only meant for map entities that are static on all clients.
             */
            @Override
            protected void onCollision(Entity player, Entity hut) {

                if (!(hut.getBoundingBoxComponent().getMinYWorld() < player.getBoundingBoxComponent().getMinYWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).addMove(INVALID_MOVE.DOWN);
                }
                if (!(hut.getBoundingBoxComponent().getMaxYWorld() > player.getBoundingBoxComponent().getMaxYWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).addMove(INVALID_MOVE.UP);
                }
                if (!(hut.getBoundingBoxComponent().getMaxXWorld() > player.getBoundingBoxComponent().getMaxXWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).addMove(INVALID_MOVE.LEFT);
                }
                if (!(hut.getBoundingBoxComponent().getMinXWorld() < player.getBoundingBoxComponent().getMinXWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).addMove(INVALID_MOVE.RIGHT);
                }
            }


            @Override
            public void onCollisionEnd(com.almasb.fxgl.entity.Entity player, Entity hut) {
                player.getComponent(AnimatedMovementComponent.class).resetMoves();
            }

        };

    }


    public static CollisionHandler setImpactCollision(EntityType player, EntityType hut) {
        return new CollisionHandler(player, hut) {

            @Override
            protected void onCollision(Entity player, Entity hut) {
                SpawnData data = new SpawnData(player.getX(), player.getY()+60);
                FXGL.getApp().getGameWorld().spawn("spell impact", data);
                System.out.println("prof3erwfgvegf");
            }


        };
    }


    public static CollisionHandler setTransparentOverlayCollision(EntityType player, EntityType hut) {
        return new CollisionHandler(player, hut) {

            @Override
            protected void onCollision(Entity player, Entity hut) {
                if (player.getType() == EntityType.LOCAL_PLAYER) {
                    hut.getView().setOpacity(0.7);
                } else
                if (player.getInt("order") < hut.getInt("order")) {

                    player.getView().setOpacity(0.7);
                } else {
                    hut.getView().setOpacity(0.7);
                }
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity hut) {
                player.getView().setOpacity(1);
                hut.getView().setOpacity(1);
            }


        };
    }



    public static CollisionHandler setDialogueCollision(EntityType player, EntityType hut) {
        return new CollisionHandler(player, hut) {

            @Override
            protected void onCollision(Entity player, Entity hut) {

            }


        };
    }


    private static boolean doesCollide(double pos1X, double pos1Y, double pos2X, double pos2Y, int pos1Width, int pos1Height, int pos2Width, int pos2Height) {
        if(pos1X < pos2X + pos2Width &&
                pos1X + pos1Width > pos2X &&
                pos1Y < pos2Y + pos1Height &&
                pos1Y + pos1Height > pos2Y)
        {
            return true;
        }
        return false;
    }

    /*
    Server side collision is simple
     */
    public static boolean doesCollide(GameObject object, CharacterPacket packet) {
        int playerWidth = 35;
        int playerHeight = 40;

        return doesCollide(packet.x, packet.y, object.getX(), object.getY(), playerWidth, playerHeight, object.getWidth(), object.getHeight());
    }

    public static boolean doesCollide(Projectile projectile, CharacterPacket packet) {
        int playerWidth = 35;
        int playerHeight = 40;

        return doesCollide(packet.x, packet.y, projectile.object.getX(), projectile.object.getY(), playerWidth, playerHeight, projectile.width, projectile.height);
    }


    public void handleStaticCollisions(List<TiledObject> staticCollisions, GameObject projectile) {
        for (TiledObject object : staticCollisions) {
            boolean collides = doesCollide(object.getX(), object.getY(), projectile.getX(), projectile.getY(), object.getWidth(), object.getHeight(),

                    projectile.getWidth(), projectile.getHeight());

            if (collides) {
                handler.handleCollision(object, projectile);
            }
        }
    }

    public void handlePlayerCollisions(Collection<Network.GameEntity> entities, CharacterPacket packet) {
        for (Network.GameEntity entity : entities) {
            if (entity instanceof GameObject) {
                GameObject object = (GameObject)entity;
                boolean collides = doesCollide(object.getX(), object.getY(), packet.x, packet.y, object.getWidth(), object.getHeight(),
                        playerWidth, playerHeight);
                if (collides) {
                    handler.handleCollision((GameObject) entity, packet);
                }
            }
        }
    }

    public void handleNPCCollision(Collection<Network.GameEntity> entities, Network.NPCPacket packet) {
        for (Network.GameEntity entity : entities) {
            if (entity instanceof GameObject) {
                GameObject object = (GameObject)entity;
                boolean collides = doesCollide(object.getX(), object.getY(), packet.x, packet.y, object.getWidth(), object.getHeight(),
                        playerWidth, playerHeight);
                if (collides) {
                    handler.handleCollision((GameObject) entity, packet);
                }
            }
        }
    }

//    public Tuple<Integer, Integer> calculateProjectileMove(Network.AddProjectile projectile, double time) {
//
//    }

}

