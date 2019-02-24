package client.listener;

import client.ClientHandler;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.Network;

import java.util.ArrayList;
import java.util.List;

public class NPCResponseListener extends Listener {
    private ClientHandler handler;

    public NPCResponseListener(ClientHandler h) {
        handler = h;
    }


    @Override
    public void received (Connection c, Object object) {
        if (object instanceof Network.NPCPacket) {
            Network.NPCPacket packet = (Network.NPCPacket)object;
            System.out.println("npcs added");
            if (!handler.hasNPC(packet.id)) {
                handler.getNpcs().add(packet);
                handler.getScreen().getActiveWorld().addNPC(packet);
            }
        }

        if (object instanceof Network.UpdateNPC) {
            Network.UpdateNPC packet = (Network.UpdateNPC)object;
            handler.updateNPC(packet.x, packet.y, packet.moveState, packet.id);
        }
    }

}
