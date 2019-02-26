package server;

import shared.*;
import shared.objects.Fish;

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

    private List<CharacterPacket> unLoadedPlayers = new ArrayList<>();
    private List<CharacterPacket> unLoadedPlayersToAdd = new ArrayList<>();
    private List<CharacterPacket> unLoadedPlayersToRemove = new ArrayList<>();


    // 128 is the limit of the number of game objects in one map
    private int[] uniqueObjects = new int[objectLimit];

    public GameMap(AlphaServer server) { // pass in the old npc handler
        this.npcHandler = new NPCHandler();

        this.server = server;

        this.mapType = MapType.STARTER; // This is going to be important later

        create();


//        int maxX = 500;
//        int minX = 300;
//        for (int i = 0; i < 5; i++) {
//            Random r = new Random();
//            int next = i*2;
//            GameObject object = new GameObject(IDs.Food.FISH);
//            float finalX = next;
//            float finalY = next^next/2;
//            object.setX(r.nextFloat());
//            object.setY(r.nextFloat());
//            object.setName(Names.Food.FISH);
//            object.setUniqueGameId(assignUniqueId());
//            addGameObject(object);
//        }

        Fish object = new Fish();
        object.setX(200);
        object.setY(200);
        object.setUniqueGameId(assignUniqueId());
//        object.setOnUse(new ObjectUseHandler() {
//            @Override
//            public void onUse(CharacterPacket packet) {
//
//            }
//        });
        addGameObject(object);

    }

    public void create() {
        RoamingBehavior behavior = new RoamingBehavior(500, 500);
        npcHandler = new NPCHandler();
        npcHandler.addNPC(behavior, assignUniqueId());

        RoamingBehavior behavior2 = new RoamingBehavior(500, 200);
        behavior2.stopMoving();
        npcHandler.addNPC(behavior2, assignUniqueId());

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

        unLoadedPlayers.addAll(unLoadedPlayersToAdd);
        unLoadedPlayersToAdd.clear();

        unLoadedPlayers.removeAll(unLoadedPlayersToRemove);
        unLoadedPlayersToRemove.clear();


        for (CharacterPacket packet : unLoadedPlayers) {
            if (packet.isLoaded) {
                onCharacterAdd(packet);
                System.out.println("loaded char");
                unLoadedPlayersToRemove.add(packet);
            }
        }

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

    private void onCharacterAdd(CharacterPacket packet) {
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

        server.sendToAllReady(object);
    }

    public void removeGameObject(GameObject object) {
        Network.RemoveGameObject packet = new Network.RemoveGameObject();
        packet.uid = object.getUniqueGameId();
        server.sendToAllReady(packet);

       objectsToRemove.add(object);
    }

    // this needs to be seperated from the game map, because by this logic objects from two different maps can have the same ids
    private int assignUniqueId() {
        Random random = new Random();
        int num = random.nextInt(objectLimit);

        for (int i = 0; i < uniqueObjects.length; i++) { // We need to make sure this unique uid isnt also a player unique uid!!!
            // if this uid has already been assigned
            if (uniqueObjects[i] == num) {
                num = assignUniqueId(); // we want to pick another number if our random has already been chosen
            }
        }

        return num;
    }

    public void addUnloadedPlayer(CharacterPacket packet) {
        unLoadedPlayersToAdd.add(packet);
    }

    public MapType getMapType() {
        return mapType;
    }

    public NPCHandler getNPCHandler() {
        return npcHandler;
    }
}
