package server;

import java.util.ArrayList;
import java.util.List;

public class NPCHandler {
    private List<NPCBehavior> npcs = new ArrayList<>();

    private Thread npcThread;

    public NPCHandler() {

    }

    public void update() {

    }

    protected void addNPC(NPCBehavior npc, int uid) {
        npc.getData().uid = uid;
        npcs.add(npc);
    }

    protected void removeNPC(int id) {
        for (NPCBehavior npc : npcs) {
            if (npc.getData().uid == id) {
                npcs.remove(npc);
            }
        }
    }

    public List<NPCBehavior> getNPCs() {
        return npcs;
    }

}
