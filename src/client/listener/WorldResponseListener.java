package client.listener;

import client.ClientHandler;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.GameObject;
import shared.Network;

public class WorldResponseListener extends Listener {
    private ClientHandler handler;


    public WorldResponseListener(ClientHandler h) {
        handler = h;
    }

    @Override
    public void received (Connection c, Object object) {
        if (object instanceof Network.WorldQuery) {
            Network.WorldQuery query = (Network.WorldQuery)object;

            //handler.getAlphaClientApp().setMap(query.map); // NEED to use object queue
            handler.setLatestWorldID(((Network.WorldQuery) object).map);

        }


        if (object instanceof GameObject) {
            GameObject obj = (GameObject)object;
            handler.addGameObject(obj);
        }

        if (object instanceof Network.RemoveGameObject) {
            Network.RemoveGameObject obj = (Network.RemoveGameObject)object;
            handler.removeGameObject(obj.uid);
            System.out.println("Removing obj");
        }

        if (object instanceof Network.ObjectPositionUpdate) {
            Network.ObjectPositionUpdate update = (Network.ObjectPositionUpdate)object;
            handler.updateObjectLocal(update.uid, update.x, update.y);
        }

//        if (object instanceof Network.AddProjectile) {
//            Network.AddProjectile projectile = (Network.AddProjectile)object;
//            handler.getAlphaClientApp().getActiveWorld().addGameObject(projectile.object);
//        }

        if (object instanceof Network.UpdatePlayerCombat) {
            Network.UpdatePlayerCombat combat = (Network.UpdatePlayerCombat)object;
            handler.updatePlayerHealth(combat.id, combat.object);
        }

        if (object instanceof Network.UpdateNPCCombat) {
            Network.UpdateNPCCombat combat = (Network.UpdateNPCCombat)object;
            handler.updateNPCHealth(combat.id, combat.object);
        }
    }
}
