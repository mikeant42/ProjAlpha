package server;

import com.almasb.fxgl.parser.tiled.TiledMap;
import com.almasb.fxgl.parser.tiled.TiledObject;
import shared.*;
import shared.objects.Fish;

import java.io.FileNotFoundException;
import java.util.*;

public class GameMap {
    /*
    There is one of these per map. Each one holds positions of npc, random loot, and all objects residing on the server
     */

    private int objectLimit = 128;

    private NPCHandler npcHandler;

    private AlphaServer server;

    private int mapID;

    // idea - this list manages multiple maps
    private List<GameObject> objects = new ArrayList<>();

    private List<GameObject> objectsToRemove = new ArrayList<>();
    private List<GameObject> objectsToAdd = new ArrayList<>();

    private List<CharacterPacket> unLoadedPlayers = new ArrayList<>();
    private List<CharacterPacket> unLoadedPlayersToAdd = new ArrayList<>();
    private List<CharacterPacket> unLoadedPlayersToRemove = new ArrayList<>();

    private ProjectileManager projectileManager;

    private TiledMap map;

    private List<TiledObject> staticCollisions = new ArrayList<>();


    // 128 is the limit of the number of game objects in one map
    //private int[] uniqueObjects = new int[objectLimit];
    private List<Integer> uniques = new ArrayList<>();

    private AlphaCollision collision;

    public GameMap(AlphaServer server) { // pass in the old npc handler
        this.npcHandler = new NPCHandler();

        this.server = server;

        this.mapID = 1;

        try {
            map = AlphaUtil.parseWorld("src/assets/json/ult.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        addCollidables();

        create();

        projectileManager = new ProjectileManager(this);

        collision = new AlphaCollision(new AlphaCollisionHandler() {
            @Override
            public void handleStaticCollision(TiledObject object, GameObject projectile) {
                removeGameObject(projectile);
                System.out.println("world collision");
            }
        });


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
//        object.setOnUse(new AlphaCollisionHandler() {
//            @Override
//            public void onUse(CharacterPacket packet) {
//
//            }
//        });
        addGameObject(object);

    }

    private void addCollidables() {
        for (TiledObject object : map.getLayerByName("collision").getObjects()) {
            // add collidable objects to server
            // we get server-side collisidon detection
            // this is necessary if we have objects that interact with the environment
            staticCollisions.add(object);

        }
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

        projectileManager.update();

        // every so often check if the player is somewhere he shouldnt be


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

            collision.handleStaticCollisions(staticCollisions, object);

            // player vs object collision
            for (CharacterPacket packet : server.getLoggedIn()) {

                if (AlphaCollision.doesCollide(object, packet)) {

                    if (!object.isProjectile()) {
                        // when player runs over an object, he adds it to his inventory
                        removeGameObject(object);
                        addInventory(packet.id, object);
                        System.out.println("picking up non projhectile");
                    } else if (object.isProjectile()) {
                        if (projectileManager.getSource(object) != packet.id) { // the user cant harm himself with a spell
                            removeGameObject(object);
                            projectileManager.remove(object);
                        }

                    }


                }
            }

            for (NPCBehavior behavior : npcHandler.getNPCs()) {
                if (AlphaCollision.doesProjectileCollide(object, behavior.getData())) {
                    System.out.println("projectile-npc collision");
                    removeGameObject(object);
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
            server.sendWithQueue(packet.id, npcBehavior.getData(), true);
        }

        for (GameObject object : objects) {
            server.sendWithQueue(packet.id, object, true);
        }

    }

    public void addGameObject(GameObject object) {
        objectsToAdd.add(object);

        server.sendToAllTCP(object);
    }

    /*
    Useful method
     */
    public void markUnloaded(CharacterPacket packet) {

    }

    public void updateObjectPosition(GameObject object) {
        Network.ObjectPositionUpdate update = new Network.ObjectPositionUpdate();
        update.uid = object.getUniqueGameId();
        update.x = object.getX();
        update.y = object.getY();
        server.sendToAllTCP(update);

    }

    public void removeGameObject(GameObject object) {
        Network.RemoveGameObject packet = new Network.RemoveGameObject();
        packet.uid = object.getUniqueGameId();
        server.sendToAllReady(packet);

       objectsToRemove.add(object);
    }

    // this needs to be seperated from the game map, because by this logic objects from two different maps can have the same ids
    public int assignUniqueId() {
        if (uniques.size() <= objectLimit) {
            Random random = new Random();
            int num = random.nextInt(objectLimit);

            for (int i = 0; i < uniques.size(); i++) { // We need to make sure this unique uid isnt also a player unique uid!!!
                // if this uid has already been assigned
                if (uniques.get(i) != null && uniques.get(i) == num) {
                    num = assignUniqueId(); // we want to pick another number if our random has already been chosen
                    return num;
                }
            }

            uniques.add(num);

            return num;
        } else {
            System.err.println("Object limit reached! Don't add anymore objects!");
            return -1;
        }
    }

    public void addUnloadedPlayer(CharacterPacket packet) {
        unLoadedPlayersToAdd.add(packet);
    }


    public NPCHandler getNPCHandler() {
        return npcHandler;
    }

    public void addProjectile(Network.AddProjectile packet) {
        projectileManager.addProjectile(packet);
    }

    public int getMapID() {
        return mapID;
    }
}
