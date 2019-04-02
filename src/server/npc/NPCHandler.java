package server.npc;

import server.IDManager;
import shared.*;
import sun.nio.ch.Net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NPCHandler {

    // maybe a class Spawner which spawns and keeps track of an entity
    private Map<Integer, NPCBehavior> npcs = new ConcurrentHashMap<>();

    private Map<String, Network.NPCPacket> npcTypes = new ConcurrentHashMap<>();


    public NPCHandler() {

    }

    public void update() {
        // should npc respawn
        // npc drops
    }

    public void addNPC(Network.NPCPacket packet) {
        npcTypes.put(packet.name, packet);
    }

    public Network.NPCPacket initMob(double x, double y, String name) {
        Network.NPCPacket packet = new Network.NPCPacket();
        packet.x = x;
        packet.y = y;
        packet.uid = IDManager.assignUniqueId();
        packet.type = EntityType.ENEMY;
        packet.name = name;
        packet.combat = new CombatObject(100,0);
        registerBehavior(packet, BehaviorType.ROAMING);

        return packet;
    }


    public void registerBehavior(Network.NPCPacket packet, BehaviorType type) {
        packet.behaviorType = type;
        NPCBehavior behavior;
        switch (type) {
            case ROAMING:
                behavior = new RoamingBehavior(packet.x, packet.y);
                npcs.put(packet.uid, behavior);
                break;
            case STATIC:
                behavior = new NPCBehavior(packet.x, packet.y);
                behavior.setAllowedToMove(false);
                npcs.put(packet.uid, behavior);
                break;
        }


    }

    public Network.UpdateNPC updateData(int id) {
        NPCBehavior behavior = npcs.get(id);

        behavior.update();

        if (!(behavior.getState() == Data.MovementState.STANDING)) {

            Network.UpdateNPC updateNPC = new Network.UpdateNPC();
            updateNPC.x = behavior.getX();
            updateNPC.y = behavior.getY();
            updateNPC.uid = id;
            updateNPC.moveState = behavior.getState();

//            // also update our packet
//            packet.x = behavior.getX();
//            packet.y = behavior.getY();


            return updateNPC;
        } else {
            return null;
        }


    }

    public void removeNPC(int uid) {
        npcs.remove(uid);
        IDManager.deAllocateId(uid);
    }


    public void addSpawnPoint() {}

    public Map<Integer, NPCBehavior> getNpcs() {
        return npcs;
    }
}
