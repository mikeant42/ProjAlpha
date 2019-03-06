package server;

import java.util.ArrayList;
import java.util.List;

public class NPCHandler {
    private List<NPC> npcs = new ArrayList<>();


    private Thread npcThread;

    public NPCHandler() {

    }

    public void update() {

    }

    protected void addNPC(NPC npc, int uid) {
        npc.getPacket().uid = uid;
        npcs.add(npc);
    }

    protected void removeNPC(int id) {
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
