package server.npc;

import java.util.ArrayList;
import java.util.List;

public class NPCHandler {
    private List<NPC> npcs = new ArrayList<>();


    public NPCHandler() {

    }

    public void update() {
        // should npc respawn
        // npc drops
    }

    public void addNPC(NPC npc) {
        npcs.add(npc);
    }

    public void addSpawnPoint() {}

    public void removeNPC(int id) {
        for (NPC npc : npcs) {
            if (npc.getPacket().uid == id) {
                npcs.remove(npc);
            }
        }
    }

    public List<NPC> getNPCs() {
        return npcs;
    }

}
