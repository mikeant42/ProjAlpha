package server;

import com.almasb.fxgl.core.math.FXGLMath;
import javafx.geometry.Point2D;
import shared.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ProjectileHandler {
//    private List<Projectile> projectilesToAdd = new ArrayList<>();
//    private List<Projectile> projectilesToRemove = new ArrayList<>();
//    private List<Projectile> projectiles;

    private Map<Integer, Projectile> projectiles = new ConcurrentHashMap<>();

    public static final int LIFESPAN_IN_TICKS = 100;

    public ProjectileHandler() {

    }

    public Projectile addProjectile(int uid, Network.AddProjectile projectile, long tick) {
        Projectile projectile1 = new Projectile();
        projectile1.projectile = projectile;
        projectile1.tickCreated = tick;
       // projectile1.uid = map.asdddddddddddddddsignUniqueId();

        GameObject projObject = new GameObject(IDs.Spell.TORNADO);
        projObject.setProjectile(true);
        projObject.setX(projectile.originX);
        projObject.setY(projectile.originY);
        projObject.setName(Names.Spell.TORNADO);
        projObject.setUniqueGameId(uid);
        //map.addGameObjectLocal(projObject);

        double tx = projectile.originX - projectile.destinationX;
        double ty = projectile.originY - projectile.destinationY;
        double mag = -Math.hypot(tx, ty);




        tx/=mag;
        ty/=mag;
        System.out.println(tx + " , " + ty);
        tx*=projectile1.moveSpeed;
        ty*=projectile1.moveSpeed;
        projectile1.velX = tx;
        projectile1.velY = ty;

        projectile1.object = projObject;


        projectile1.damageEffect = 10;

        projectiles.put(projectile1.object.getUniqueGameId(), projectile1);

        System.out.println("added projectile");
        return projectile1;

    }

    public GameObject updateProjectile(int uid, long tick) {
        Projectile projectile = projectiles.get(uid);
        if (projectile == null) {
            // I have no idea why this would be null, but if it is it may be the cause of a bug.
            return null;
        }
        if (projectile.tickCreated+LIFESPAN_IN_TICKS >= tick) {
            projectile.object.setX(projectile.object.getX()+projectile.velX);
            projectile.object.setY(projectile.object.getY()+projectile.velY);
            return projectile.object;
        } else {
            return null;
        }

    }


//    public void update(long tick) {
//
//        for (Projectile projectile : projectiles.values()) {
//            if (projectile.tickCreated+LIFESPAN_IN_TICKS >= tick) {
//                //Point2D newPosition = FXGLMath.lerp(projectile.object.getX(), projectile.object.getY(),
//                //        projectile.projectile.destinationX, projectile.projectile.destinationY, 0.01);
//
//
//                    projectile.object.setX(projectile.object.getX()+projectile.velX);
//                    projectile.object.setY(projectile.object.getY()+projectile.velY);
//
//                    map.updateObjectPosition(projectile.object);
//            } else {
//                map.removeGameObject(projectile.object);
//                projectiles.remove(projectile.object.getUniqueGameId());
//                System.out.println("proj manager removing object");
//            }
//        }
//    }

    public int getSourcePlayerID(int uid) {
        if (projectiles.get(uid) != null) {
            return projectiles.get(uid).projectile.sourceUser;
        }

        return -1;
    }

    public Projectile get(int id) {
        return projectiles.get(id);
    }

    public void remove(int uid) {
        projectiles.remove(uid);
    }

    public boolean hasExpired(int uid) {
        return !(projectiles.get(uid).tickCreated+LIFESPAN_IN_TICKS >= projectiles.get(uid).tickCreated);
    }


}
