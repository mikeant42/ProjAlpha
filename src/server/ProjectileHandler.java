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

    private GameMap map;
    public static final int LIFESPAN_IN_TICKS = 100;

    public ProjectileHandler(GameMap map) {

        this.map = map;
    }

    public Projectile addProjectile(Network.AddProjectile projectile, long tick) {
        Projectile projectile1 = new Projectile();
        projectile1.projectile = projectile;
        projectile1.tickCreated = tick;
       // projectile1.uid = map.assignUniqueId();

        GameObject projObject = new GameObject(IDs.Spell.TORNADO);
        projObject.setProjectile(true);
        projObject.setX(projectile.originX);
        projObject.setY(projectile.originY);
        projObject.setName(Names.Spell.TORNADO);
        projObject.setUniqueGameId(map.assignUniqueId());
        map.addGameObjectLocal(projObject);

        double tx = projectile.originX - projectile.destinationX;
        double ty = projectile.originY - projectile.destinationY;
        double mag = -Math.hypot(tx, ty);



        tx/=mag;
        ty/=mag;
        tx*=projectile1.moveSpeed;
        ty*=projectile1.moveSpeed;
        projectile1.velX = tx;
        projectile1.velY = ty;

        projectile1.object = projObject;

        projectiles.put(projectile1.object.getUniqueGameId(), projectile1);

        System.out.println("added projectile");
        return projectile1;

    }


    public void update(long tick) {

        for (Projectile projectile : projectiles.values()) {
            if (projectile.tickCreated+LIFESPAN_IN_TICKS >= tick) {
                //Point2D newPosition = FXGLMath.lerp(projectile.object.getX(), projectile.object.getY(),
                //        projectile.projectile.destinationX, projectile.projectile.destinationY, 0.01);


                    projectile.object.setX(projectile.object.getX()+projectile.velX);
                    projectile.object.setY(projectile.object.getY()+projectile.velY);

                    map.updateObjectPosition(projectile.object);
            } else {
                map.removeGameObject(projectile.object);
                projectiles.remove(projectile.object.getUniqueGameId());
                System.out.println("proj manager removing object");
            }
        }
    }

    public int getSource(int uid) {
        if (projectiles.get(uid) != null) {
            return projectiles.get(uid).projectile.sourceUser;
        }

        return -1;
    }

    public void remove(int uid) {
        projectiles.remove(uid);
    }


}
