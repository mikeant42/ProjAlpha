package server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
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

            query.map = 1;

            handler.getServer().sendToTCP(c.getID(), query);
        }
    }
}