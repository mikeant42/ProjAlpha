package server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.GameObject;
import shared.Network;

public class WorldListener extends Listener {
    private ServerHandler handler;

    public WorldListener(ServerHandler h) {
        handler = h;
    }

    @Override
    public void received (Connection c, Object object) {
        if (object instanceof Network.WorldQuery) {
            Network.WorldQuery query = (Network.WorldQuery)object;

            query.map = handler.getServer().getMap().getMapID(); // eventually the player will have it on him in the db

            handler.getServer().sendToTCP(c.getID(), query);
            handler.getServer().setIsLoaded(c.getID(), true);
        }

        if (object instanceof Network.AddProjectile) {
            Network.AddProjectile gameObject = (Network.AddProjectile) object;
            handler.getServer().getMap().addProjectile(gameObject);

            //handler.getServer().sendToAllReady(gameObject);
            handler.getServer().sendToAllReady(gameObject);
        }
    }
}
