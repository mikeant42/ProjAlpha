package server;

import com.almasb.fxgl.parser.tiled.TiledMap;
import com.almasb.fxgl.parser.tiled.TiledObject;
import server.message.Message;
import server.npc.NPC;
import server.npc.NPCHandler;
import shared.*;
import shared.collision.AlphaCollision;
import shared.collision.AlphaCollisionHandler;
import shared.objects.Fish;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class GameMap {
    /*
    There is one of these per map. Each one holds positions of npc, random loot, and all objects residing on the server
     */

    private int objectLimit = 1024;

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

    // this is the entire stack of data the gamemap sends out each time it broadcasts to the client
    private BlockingQueue<Message> objectsToSend = new ArrayBlockingQueue<Message>(10000);

    private ProjectileManager projectileManager;

    private TiledMap map;

    private List<TiledObject> staticCollisions = new ArrayList<>();

    private boolean updateInternal = true;

    long tick = 0;


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

        projectileManager = new ProjectileManager(this);

        collision = new AlphaCollision(new AlphaCollisionHandler() {
            @Override
            public void handleStaticCollision(TiledObject object, GameObject projectile) {
                removeGameObject(projectile);
                projectileManager.remove(projectile.getUniqueGameId());
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

        for (int i = 0; i < 10; i++) {
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

    }

    private void addCollidables() {
        staticCollisions.addAll(map.getLayerByName("collision").getObjects());

        for (TiledObject object : map.getLayerByName("spawn").getObjects()) {
            if (object.getName().equals("googon")) {
                NPC npc = new NPC("googon", object.getX(), object.getY());
                npc.setEnemy(true);
                npc.setBehavior(BehaviorType.ROAMING);
                npcHandler.addNPC(npc, assignUniqueId());
            } else if (object.getName().equals("watcher")) {
                NPC npc = new NPC("watcher", object.getX(), object.getY());
                npc.setInteractable(true);
                npc.setBehavior(BehaviorType.STATIC);
                npcHandler.addNPC(npc, assignUniqueId());
            }
        }
    }

    public void updateExternal(long broadcastTick) {
        // this is the code broadcasting to the clients
        // this code will only run ~10 times a second

        for (Message packet : objectsToSend) {
                packet.send(server);
                objectsToSend.remove(packet);
        }

    }


    /*
    This updates in another thread at 60fps
     */
    public void updateAction(long tick) {
        this.tick = tick;

        objects.addAll(objectsToAdd);
        objects.removeAll(objectsToRemove);

        objectsToAdd.clear();
        objectsToRemove.clear();

        unLoadedPlayers.addAll(unLoadedPlayersToAdd);
        unLoadedPlayersToAdd.clear();

        unLoadedPlayers.removeAll(unLoadedPlayersToRemove);
        unLoadedPlayersToRemove.clear();


        projectileManager.update(tick);

        // every so often check if the player is somewhere he shouldnt be


        for (CharacterPacket packet : unLoadedPlayers) {
            if (packet.isLoaded) {
                onCharacterAdd(packet);
                System.out.println("loaded char");
                unLoadedPlayersToRemove.add(packet);
            }
        }

        for (GameObject object : objects) {

            collision.handleStaticCollisions(staticCollisions, object);

            for (CharacterPacket packet : server.getLoggedIn()) {
                if (AlphaCollision.doesCollide(object, packet)) {

                    if (!object.isProjectile()) {
                        // when player runs over an object, he adds it to his inventory
                        removeGameObject(object);
                        addInventory(packet.id, object);
                        System.out.println("picking up non projhectile");
                    } else {
                        if (projectileManager.getSource(object.getUniqueGameId()) != packet.id) { // the user cant harm himself with a spell
                            projectileManager.remove(object.getUniqueGameId());
                            removeGameObject(object);

                            // test
                            Network.UpdatePlayerCombat combat = new Network.UpdatePlayerCombat();
                            combat.id = packet.id;
                            packet.combat.setHealth(packet.combat.getHealth() - 10);
                            combat.object = packet.combat;

                            queueMessage(new Message(combat, false));

                        }

                    }
                }
            }

            for (NPC npc : npcHandler.getNPCs()) {
                if (npc.isEnemy() &&  object.isProjectile() && AlphaCollision.doesCollide(object, npc)) {
                    projectileManager.remove(object.getUniqueGameId());
                    removeGameObject(object);

                    // gameplay idea - if you attack weak mop npcs, the surrounding mob will start attacking you.

                    Network.UpdateNPCCombat combat = new Network.UpdateNPCCombat();
                    combat.id = npc.getPacket().uid;
                    npc.getPacket().combat.setHealth(npc.getPacket().combat.getHealth() - 10);
                    combat.object = npc.getPacket().combat;

                    queueMessage(new Message(combat, false));

                    System.out.println("proj-npc collision");
                }


            }


        }

        for (NPC npc : npcHandler.getNPCs()) {

            if (npc.shouldUpdate()) {
                //server.sendToAllReady(npc.formUpdate());
                queueMessage(new Message(npc.formUpdate(), false));
            }
            npc.update();
        }

    }

    public void addInventory(int cid, GameObject object) {
        Network.AddInventoryItem inventoryItem = new Network.AddInventoryItem();
        inventoryItem.object = object;
        //server.sendToTCP(cid, inventoryItem);
        queueMessage(new Message(cid, inventoryItem, false));
    }

    private void onCharacterAdd(CharacterPacket packet) {
        // We also have to spawn all the npcs in his level
        for (NPC npc : npcHandler.getNPCs()) {
            //server.sendWithQueue(packet.id, npc.getPacket(), true);
            queueMessage(new Message(packet.id, npc.getPacket(), true));
        }

        for (GameObject object : objects) {
            //server.sendWithQueue(packet.id, object, true);
            queueMessage(new Message(packet.id, object, true));
        }

    }

    public void addGameObject(GameObject object) {
        objectsToAdd.add(object);
        updateInternal = true;

        System.out.println("adding obj");
        //server.sendToAllReady(object);`
        queueMessage(new Message(object, false));
    }

    public void addGameObjectLocal(GameObject object) {
        objectsToAdd.add(object);
    }


    public void updateObjectPosition(GameObject object) {
        Network.ObjectPositionUpdate update = new Network.ObjectPositionUpdate();
        update.uid = object.getUniqueGameId();
        update.x = object.getX();
        update.y = object.getY();
        //server.sendToAllTCP(update);
        queueMessage(new Message(update, false));

    }

    public void updatePlayerHealthServer(int id, CombatObject object) {
        for (CharacterPacket packet : server.getLoggedIn()) {
            if (id == packet.id) {
                packet.combat = object;
            }
        }
    }

    public void removeGameObject(GameObject object) {
        Network.RemoveGameObject packet = new Network.RemoveGameObject();
        packet.uid = object.getUniqueGameId();
        //server.sendToAllReady(packet);
        queueMessage(new Message(packet, false));

       objectsToRemove.add(object);
       updateInternal = true;
       //deAllocateId(object.getUniqueGameId());
    }

    public void removeGameObject(int uid) {
        for (GameObject object : objects) {
            if (object.getUniqueGameId() == uid) {
                removeGameObject(object);
            }
        }
        updateInternal = true;
    }

//    private void deAllocateId(int id) {
//        // sync needed bacause these can be called from another thread
//        synchronized (uniques) {
//            uniques.remove(new Integer(id));
//        }
//    }

    // IT ASSIGNED DUPLICATES!!!!!!!!!!!!!!!!!!!!!!!!!!!!! FIX FIX
    // this needs to be seperated from the game map, because by this logic objects from two different maps can have the same ids
    // if you gen too many ids this will cause a stackoverflow
    public int assignUniqueId() {
        synchronized (uniques) {
            int num = ThreadLocalRandom.current().nextInt(AlphaServer.PLAYER_COUNT, objectLimit + 1);

            if (uniques.contains(num)) {
                num = assignUniqueId();
            } else {
                uniques.add(num);
            }

            return num;
        }
    }


    public void addUnloadedPlayer(CharacterPacket packet) {
        unLoadedPlayersToAdd.add(packet);
        updateInternal = true;
    }


    public NPCHandler getNPCHandler() {
        return npcHandler;
    }

    public void addProjectile(Network.AddProjectile packet) {
        Projectile projectile = projectileManager.addProjectile(packet, tick);
        queueMessage(new Message(projectile, false));
    }

    public int getMapID() {
        return mapID;
    }

    public void queueMessage(Message message) {
        if (message.isWait()) {
            server.addMessageToQueue(message);
        } else {
            try {
                objectsToSend.put(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("The queue is blocking, probably because there are too many queued messages");
            }
        }
    }
}
