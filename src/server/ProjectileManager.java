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

    public void addProjectile(Network.AddProjectile projectile, long tick) {
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
        map.addGameObject(projObject);

        projectile1.object = projObject;

        projectilesToAdd.add(projectile1);

        System.out.println("added projectile");
        // also need to add corresponding gameobject to all of the clients
    }

    /*
    new design
    - send out a smaller number of packets by lowering the server "fps"
    - use ticks on client to interpolate
     */

    public void update(long tick) {
        projectiles.addAll(projectilesToAdd);
        projectilesToAdd.clear();

        projectiles.removeAll(projectilesToRemove);
        projectilesToRemove.clear();


        for (Projectile projectile : projectiles) {
            if (projectile.tickCreated+LIFESPAN_IN_TICKS >= tick) {
                Point2D newPosition = FXGLMath.lerp(projectile.object.getX(), projectile.object.getY(),
                        projectile.projectile.destinationX, projectile.projectile.destinationY, 0.01);

                    projectile.object.setX(newPosition.getX());
                    projectile.object.setY(newPosition.getY());

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
            if (uid == projectile.uid) {
                projectilesToRemove.add(projectile);
            }
        }
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }
}
