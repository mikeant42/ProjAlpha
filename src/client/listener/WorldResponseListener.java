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

            //handler.getScreen().setMap(query.map); // NEED to use object queue
            handler.setLatestWorldID(((Network.WorldQuery) object).map);
        }


        if (object instanceof GameObject) {
            GameObject obj = (GameObject)object;
            handler.addGameObject(obj);
        }

        if (object instanceof Network.RemoveGameObject) {
            Network.RemoveGameObject obj = (Network.RemoveGameObject)object;
            handler.removeGameObject(obj.uid);
            System.out.println("remopving obj");
        }
    }
}
