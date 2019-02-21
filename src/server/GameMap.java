package server;

import shared.*;

import java.util.*;

public class GameMap {
    /*
    There is one of these per map. Each one holds positions of npc, random loot, and all objects residing on the server
     */

    private int objectLimit = 128;

    private NPCHandler npcHandler;

    private AlphaServer server;

    private MapType mapType;

    private List<GameObject> objects = new ArrayList<>();

    private List<GameObject> objectsToRemove = new ArrayList<>();
    private List<GameObject> objectsToAdd = new ArrayList<>();


    // 128 is the limit of the number of game objects in one map
    private int[] uniqueObjects = new int[objectLimit];

    public GameMap(AlphaServer server) { // pass in the old npc handler
        this.npcHandler = new NPCHandler();

        this.server = server;

        this.mapType = MapType.STARTER; // This is going to be important later

        create();

        Random r = new Random();
        int maxX = 500;
        int minX = 300;
        for (int i = 0; i < 1; i++) {
            int next = i*2;
            GameObject object = new GameObject(IDs.Food.FISH);
            float finalX = next;
            float finalY = next^next/2;
            object.setX(r.nextFloat());
            object.setY(r.nextFloat());
            object.setName(Names.Food.FISH);
            object.setUniqueGameId(assignUniqueId());
            addGameObject(object);
        }

        GameObject object = new GameObject(IDs.Food.FISH);
        object.setX(200);
        object.setY(200);
        object.setName(Names.Food.FISH);
        object.setUniqueGameId(assignUniqueId());
        addGameObject(object);
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
        objects.addAll(objectsToAdd);
        objects.removeAll(objectsToRemove);

        objectsToAdd.clear();
        objectsToRemove.clear();

        for (NPCBehavior behavior : npcHandler.getNPCs()) {
            behavior.update();
        }

        for (GameObject object : objects) {
            for (CharacterPacket packet : server.getLoggedIn()) {

                if (AlphaCollision.doesCollide(object, packet)) {

                    // when player runs over an object, he adds it to his inventory
                    removeGameObject(object);
                    addInventory(packet.id, object);


                    System.out.println("collision");
                }
            }
        }
    }

    public void addInventory(int cid, GameObject object) {
        Network.AddInventoryItem inventoryItem = new Network.AddInventoryItem();
        inventoryItem.object = object;
        server.sendToTCP(cid, inventoryItem);
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
        objectsToAdd.add(object);

        server.sendToAllTCP(object);
    }

    public void removeGameObject(GameObject object) {
        Network.RemoveGameObject packet = new Network.RemoveGameObject();
        packet.uid = object.getUniqueGameId();
        server.sendToAllTCP(packet);

       objectsToRemove.add(object);
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
