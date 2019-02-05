package server;

import shared.EntityType;
import shared.Network;

import java.util.ArrayList;
import java.util.List;

public class NPCHandler {
    private List<Network.NPCPacket> npcs = new ArrayList<>();

    public NPCHandler() {
        Network.NPCPacket npcPacket = new Network.NPCPacket();
        npcPacket.id = 500;
        npcPacket.type = EntityType.ROAMING_NPC;
        npcPacket.x = 50;
        npcPacket.y = 50;
        npcs.add(npcPacket);
    }

    public void update() {

    }

    protected void addNPC(Network.NPCPacket npc) {
        npcs.add(npc);
    }

    protected void removeNPC(int id) {
        for (Network.NPCPacket packet : npcs) {
            if (packet.id == id) {
                npcs.remove(packet);
            }
        }
    }

    protected List<Network.NPCPacket> getNPCs() {
        return npcs;
    }
}
