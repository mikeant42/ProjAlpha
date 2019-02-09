package server;

import shared.EntityType;
import shared.Network;

import java.util.ArrayList;
import java.util.List;

public class NPCHandler {
    private List<NPCBehavior> npcs = new ArrayList<>();

    private Thread npcThread;

    public NPCHandler() {

    }

    public void update() {

    }

    protected void addNPC(NPCBehavior npc) {
        npcs.add(npc);
    }

    protected void removeNPC(int id) {
        for (NPCBehavior npc : npcs) {
            if (npc.getData().id == id) {
                npcs.remove(npc);
            }
        }
    }

    public List<NPCBehavior> getNPCs() {
        return npcs;
    }

}