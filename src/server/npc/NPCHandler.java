package server.npc;

import shared.BehaviorType;
import shared.Data;
import shared.Network;
import sun.nio.ch.Net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NPCHandler {

    private Map<Network.NPCPacket, NPCBehavior> npcs = new ConcurrentHashMap<>();


    public NPCHandler() {

    }

    public void update() {
        // should npc respawn
        // npc drops
    }


    public void registerBehavior(Network.NPCPacket packet, BehaviorType type) {
        packet.behaviorType = type;
        NPCBehavior behavior;
        switch (type) {
            case ROAMING:
                behavior = new RoamingBehavior(packet.x, packet.y);
                npcs.put(packet, behavior);
                return;
            case STATIC:
                behavior = new NPCBehavior(packet.x, packet.y);
                behavior.setAllowedToMove(false);
                npcs.put(packet, behavior);
        }


    }

    public Network.UpdateNPC updateData(Network.NPCPacket packet) {
        NPCBehavior behavior = npcs.get(packet);

        behavior.update();

        if (!(behavior.getState() == Data.MovementState.STANDING)) {

            Network.UpdateNPC updateNPC = new Network.UpdateNPC();
            updateNPC.x = behavior.getX();
            updateNPC.y = behavior.getY();
            updateNPC.uid = packet.uid;
            updateNPC.moveState = behavior.getState();

            // also update our packet
            packet.x = behavior.getX();
            packet.y = behavior.getY();


            return updateNPC;
        } else {
            return null;
        }


    }


    public void addSpawnPoint() {}

    public Map<Network.NPCPacket, NPCBehavior> getNpcs() {
        return npcs;
    }
}
