package server;

import shared.GameObject;
import shared.IDs;
import shared.Names;
import shared.Network;

import java.util.ArrayList;
import java.util.List;

class Projectile {
    public GameObject object;
    public Network.AddProjectile projectile;
}

public class ProjectileManager {
    private List<Projectile> projectiles;
    private GameMap map;

    public ProjectileManager(GameMap map) {
        projectiles = new ArrayList<>();

        this.map = map;
    }

    public void addProjectile(Network.AddProjectile projectile) {
        Projectile projectile1 = new Projectile();
        projectile1.projectile = projectile;

        GameObject projObject = new GameObject(IDs.Spell.TORNADO);
        projObject.setProjectile(true);
        projObject.setX(projectile.destinationX);
        projObject.setY(projectile.destinationY);
        projObject.setName(Names.Spell.TORNADO);
        projObject.setUniqueGameId(map.assignUniqueId());
        map.addGameObject(projObject);

        projectile1.object = projObject;

        projectiles.add(projectile1);

        System.out.println("added projectile");
        // also need to add corresponding gameobject to all of the clients
    }

    public void update() {
        for (Projectile projectile : projectiles) {
            // compute path for projectile
            projectile.object.setX(projectile.object.getX()+0.5);
            map.updateObjectPosition(projectile.object);
            // if projectile is too old delete it
        }
    }
}
