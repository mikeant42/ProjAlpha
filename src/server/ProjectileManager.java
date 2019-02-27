package server;

import shared.GameObject;
import shared.Network;

import java.util.ArrayList;
import java.util.List;

public class ProjectileManager {
    private List<Network.AddProjectile> projectiles;

    public ProjectileManager() {
        projectiles = new ArrayList<>();
    }

    public void addProjectile(Network.AddProjectile projectile) {
        projectiles.add(projectile);
    }

    public void update() {
        for (Network.AddProjectile projectile : projectiles) {

        }
    }
}
