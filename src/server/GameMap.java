package server;

import shared.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMap {
    /*
    There is one of these per map. Each one holds positions of npc, random loot, and all objects residing on the server
     */

    private int objectLimit = 128;

    private NPCHandler npcHandler;

    private AlphaServer server;

    private MapType mapType;

    private List<GameObject> objects = new ArrayList<>();

    // 128 is the limit of the number of game objects in one map
    private int[] uniqueObjects = new int[objectLimit];

    public GameMap(AlphaServer server) { // pass in the old npc handler
        this.npcHandler = new NPCHandler();

        this.server = server;

        this.mapType = MapType.STARTER; // This is going to be important later

        create();

        GameObject object = new GameObject(IDs.Food.FISH);
        object.setX(300);
        object.setY(300);
        object.setName(Names.Food.FISH);
        object.setUniqueGameId(assignUniqueId());
        addGameObject(object);

        GameObject object1 = new GameObject(IDs.Food.FISH);
        object1.setX(0);
        object1.setY(200);
        object1.setName(Names.Food.FISH);
        object1.setUniqueGameId(assignUniqueId());
        addGameObject(object1);
    }

    public void create() {
        RoamingBehavior behavior = new RoamingBehavior(500, 500);
        npcHandler = new NPCHandler();
        npcHandler.addNPC(behavior);

    }

    /*
    This is a server update
     */
    public void update() {
        for (NPCBehavior behavior : npcHandler.getNPCs()) {
            server.sendToAllTCP(behavior.formUpdate());
        }



    }

    /*
    This updates in another thread for non-networking updates
     */
    public void updateAction() {
        for (NPCBehavior behavior : npcHandler.getNPCs()) {
            behavior.update();
        }

        for (GameObject object : objects) {
            for (CharacterPacket packet : server.getLoggedIn()) {

                if (AlphaCollision.doesCollide(object, packet)) {
                    removeGameObject(object.getUniqueGameId());
                    System.out.println("collision");
                }
            }
        }
    }

    public void onCharacterAdd(CharacterPacket packet) {
        // We also have to spawn all the npcs in his level
        for (NPCBehavior npcBehavior : npcHandler.getNPCs()) {
            server.sendToTCP(packet.id, npcBehavior.getData());
        }

        for (GameObject object : objects) {
            server.sendToTCP(packet.id, object);
        }
    }

    public void addGameObject(GameObject object) {
        objects.add(object);

        server.sendToAllTCP(object);
    }

    public void removeGameObject(int uid) {
        Network.RemoveGameObject packet = new Network.RemoveGameObject();
        packet.uid = uid;
        server.sendToAllTCP(packet);
    }

    private int assignUniqueId() {
        Random random = new Random();
        int num = random.nextInt(objectLimit);

        for (int i = 0; i < uniqueObjects.length; i++) {
            // if this id has already been assigned
            if (uniqueObjects[i] == num) {
                num = assignUniqueId(); // we want to pick another number if our random has already been chosen
            }
        }

        return num;
    }

    public MapType getMapType() {
        return mapType;
    }

    public NPCHandler getNPCHandler() {
        return npcHandler;
    }
}
