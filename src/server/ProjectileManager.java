package server;

import com.almasb.fxgl.core.math.FXGLMath;
import javafx.geometry.Point2D;
import shared.*;

import java.util.ArrayList;
import java.util.List;


public class ProjectileManager {
    private List<Projectile> projectilesToAdd = new ArrayList<>();
    private List<Projectile> projectilesToRemove = new ArrayList<>();
    private List<Projectile> projectiles;
    private GameMap map;
    public static final int LIFESPAN_IN_TICKS = 100;

    public ProjectileManager(GameMap map) {
        projectiles = new ArrayList<>();

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

        projectilesToAdd.add(projectile1);

        System.out.println("added projectile");
        return projectile1;

    }


    public void update(long tick) {
        projectiles.addAll(projectilesToAdd);
        projectilesToAdd.clear();

        projectiles.removeAll(projectilesToRemove);
        projectilesToRemove.clear();


        for (Projectile projectile : projectiles) {
            if (projectile.tickCreated+LIFESPAN_IN_TICKS >= tick) {
                //Point2D newPosition = FXGLMath.lerp(projectile.object.getX(), projectile.object.getY(),
                //        projectile.projectile.destinationX, projectile.projectile.destinationY, 0.01);


                    projectile.object.setX(projectile.object.getX()+projectile.velX);
                    projectile.object.setY(projectile.object.getY()+projectile.velY);

                    map.updateObjectPosition(projectile.object);
            } else {
                map.removeGameObject(projectile.object.getUniqueGameId());
            }
        }
    }

    public int getSource(int uid) {
        for (int i = 0; i < projectiles.size(); i++) {
            if (projectiles.get(i).object.getUniqueGameId() == uid) {
                return projectiles.get(i).projectile.sourceUser;
            }
        }

        return -1;
    }

    public void remove(int uid) {
        for (Projectile projectile : projectiles) {
            if (uid == projectile.object.getUniqueGameId()) {
                projectilesToRemove.add(projectile);
            }
        }
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }
}
