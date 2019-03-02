package shared.collision;

import client.AnimatedMovementComponent;
import client.MovementComponent.*;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.parser.tiled.TiledObject;
import com.almasb.fxgl.physics.CollisionHandler;
import shared.CharacterPacket;
import shared.EntityType;
import shared.GameObject;
import shared.Network;

import java.util.List;

public class AlphaCollision {

    private AlphaCollisionHandler handler;

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

    public static CollisionHandler setProjectileCollision(EntityType player, EntityType hut) {
        return new CollisionHandler(EntityType.PLAYER, EntityType.PROJECTILE) {

            @Override
            protected void onCollision(Entity player, Entity hut) {
                hut.removeFromWorld();
            }


        };
    }

    public static CollisionHandler setProjectileCollisionNPC(EntityType player, EntityType hut) {
        return new CollisionHandler(EntityType.NPC, EntityType.PROJECTILE) {

            @Override
            protected void onCollision(Entity player, Entity hut) {
                System.out.println("collision");
                hut.removeFromWorld();
            }


        };
    }


    private static boolean doesCollide(double pos1X, double pos1Y, double pos2X, double pos2Y, int pos1Width, int pos1Height, int pos2Width, int pos2Height) {
        int playerWidth = 40;
        int playerHeight = 40;
        // packet - pos1, object - pos2

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
        int playerWidth = 40;
        int playerHeight = 40;

        return doesCollide(packet.x, packet.y, object.getX(), object.getY(), playerWidth, playerHeight, object.getWidth(), object.getHeight());
    }

    public static boolean doesProjectileCollide(GameObject object, Network.NPCPacket packet) {
        int projectileWidth = 10;
        int projectileHeight = 10;

        return doesCollide(packet.x, packet.y, object.getX(), object.getY(), projectileWidth, projectileHeight, object.getWidth(), object.getHeight());
    }


    public void handleStaticCollisions(List<TiledObject> staticCollisions, GameObject projectile) {
        for (TiledObject object : staticCollisions) {
            boolean collides = doesCollide(object.getX(), object.getY(), projectile.getX(), projectile.getY(), object.getWidth(), object.getHeight(),
                    projectile.getWidth(), projectile.getHeight());
            if (collides) {
                handler.handleStaticCollision(object, projectile);
            }
        }
    }

//    public Tuple<Integer, Integer> calculateProjectileMove(Network.AddProjectile projectile, double time) {
//
//    }

}

