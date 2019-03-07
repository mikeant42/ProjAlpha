package server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.GameObject;
import shared.Network;
import sun.nio.ch.Net;

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

           // handler.getServer().sendToTCP(c.getID(), query);
            handler.getServer().getMap().queueMessage(new Message(c.getID(), query, false));
            //handler.getServer().setIsLoaded(c.getID(), true);
        }

        if (object instanceof Network.ReadyToRecieve) {
            Network.ReadyToRecieve readyToRecieve = (Network.ReadyToRecieve)object;
            handler.getServer().setIsLoaded(readyToRecieve.cid, readyToRecieve.ready);
        }

        if (object instanceof Network.AddProjectile) {
            Network.AddProjectile gameObject = (Network.AddProjectile) object;
            handler.getServer().getMap().addProjectile(gameObject);

            //handler.getServer().sendToAllReady(gameObject);
           // handler.getServer().sendToAllReady(gameObject);
        }

        if (object instanceof Network.UpdatePlayerCombat) {
            Network.UpdatePlayerCombat combat = (Network.UpdatePlayerCombat)object;

            handler.getServer().getMap().updatePlayerHealthServer(combat.id, combat.object);

            //handler.getServer().sendToAllReady(object);
            handler.getServer().getMap().queueMessage(new Message(object, false));
        }
    }
}
