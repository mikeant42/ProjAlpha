package server;

import shared.MapType;

public class GameMap {
    /*
    There is one of these per map. Each one holds positions of npc, random loot, and all objects residing on the server
     */

    private NPCHandler npcHandler;

    private AlphaServer server;

    private MapType mapType;

    public GameMap(AlphaServer server) { // pass in the old npc handler
        this.npcHandler = new NPCHandler();

        this.server = server;

        this.mapType = MapType.STARTER; // This is going to be important later

        create();
    }

    public void create() {
        RoamingBehavior behavior = new RoamingBehavior(500, 500);
        npcHandler = new NPCHandler();
        npcHandler.addNPC(behavior);

    }

    public void update() {
        for (NPCBehavior behavior : npcHandler.getNPCs()) {
            server.sendToAllTCP(behavior.formUpdate());
        }

    }

    public MapType getMapType() {
        return mapType;
    }

    public NPCHandler getNPCHandler() {
        return npcHandler;
    }
}
