package server;

import com.almasb.fxgl.parser.tiled.TiledMap;
import com.almasb.fxgl.parser.tiled.TiledObject;
import server.message.Message;
import server.npc.NPCHandler;
import shared.*;
import shared.collision.AlphaCollision;
import shared.collision.AlphaCollisionHandler;
import shared.objects.Fish;

import javax.swing.text.html.parser.Entity;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;

public class GameMap {
    /*
    There is one of these per map. Each one holds positions of npc, random loot, and all objects residing on the server

    maybe create some sort of IDManager so i can let other classes assign unique ids
     */

    private int objectLimit = 1024;

    private NPCHandler npcHandler;

    private AlphaServer server;

    private int mapID;

    private Queue<CharacterPacket> unloadedPlayers = new ConcurrentLinkedQueue<>();


    // this is the entire stack of data the gamemap sends out each time it broadcasts to the client
    private BlockingQueue<Message> objectsToSend = new ArrayBlockingQueue<Message>(10000);


    // 16 seperate threads allowed to access this map
    // maps the (uid, object) of each game entity
    private Map<Integer, Network.GameEntity> entities = new ConcurrentHashMap<>();

    private ProjectileManager projectileManager;

    private TiledMap map;

    private List<TiledObject> staticCollisions = new ArrayList<>();

    private boolean updateInternal = true;

    private double respawnX, respawnY; // a player respawn point in every map

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
            public void handleCollision(TiledObject object, GameObject projectile) {
                removeGameObject(projectile);
                projectileManager.remove(projectile.getUniqueGameId());
                System.out.println("world collision");
            }

            @Override
            public void handleCollision(GameObject object, CharacterPacket player) {
                if (!object.isProjectile()) {
                    // when player runs over an object, he adds it to his inventory
                    removeGameObject(object);
                    addInventory(player, object);
                    System.out.println("picking up non projhectile");
                } else {
                    if (projectileManager.getSource(object.getUniqueGameId()) != player.uid) { // the user cant harm himself with a spell
                        projectileManager.remove(object.getUniqueGameId());
                        removeGameObject(object);

                        // test
                        Network.UpdatePlayerCombat combat = new Network.UpdatePlayerCombat();
                        combat.id = player.uid;
                        player.combat.setHealth(player.combat.getHealth() - 10);
                        combat.object = player.combat;

                        queueMessage(new Message(combat, false));

                        if (player.combat.getHealth() <= 0) {
                            killPlayer(player);
                            System.out.println("Client " + projectileManager.getSource(object.getUniqueGameId()) + " killed client " + object.uid);
                        }

                    }

                }
            }

            @Override
            public void handleCollision(GameObject object, Network.NPCPacket npc) {
                if (npc.type == EntityType.ENEMY &&  object.isProjectile()) {
                    projectileManager.remove(object.getUniqueGameId());
                    removeGameObject(object);

                    // gameplay idea - if you attack weak mop npcs, the surrounding mob will start attacking you.

                    Network.UpdateNPCCombat combat = new Network.UpdateNPCCombat();
                    combat.id = npc.uid;
                    npc.combat.setHealth(npc.combat.getHealth() - 10);
                    combat.object = npc.combat;

                    queueMessage(new Message(combat, false));

                    System.out.println("proj-npc collision");
                }
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
            if (object.getName().equals("respawn")) {
                respawnX = object.getX();
                respawnY = object.getY();
            } else if (object.getName().equals("googon")) {
                Network.NPCPacket packet = new Network.NPCPacket();
                packet.x = object.getX();
                packet.y = object.getY();
                packet.uid = assignUniqueId();
                packet.type = EntityType.ENEMY;
                packet.name = "googon";
                packet.combat = new CombatObject(100,0);
                npcHandler.registerBehavior(packet, BehaviorType.ROAMING);
                entities.put(packet.uid, packet);
            } else if (object.getName().equals("watcher")) {
                Network.NPCPacket packet = new Network.NPCPacket();
                packet.x = object.getX();
                packet.y = object.getY();
                packet.uid = assignUniqueId();
                packet.type = EntityType.INTERACTABLE_NPC;
                packet.name = object.getName();
                //packet.combat = new CombatObject(100,0);
                npcHandler.registerBehavior(packet, BehaviorType.STATIC);
                entities.put(packet.uid, packet);
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

    public void removePlayer(int uid) {
        entities.remove(uid);
    }


    /*
    This updates in another thread at 60fps
     */
    public void updateAction(long tick) {
        this.tick = tick;

        projectileManager.update(tick);

        // every so often check if the player is somewhere he shouldnt be


        for (CharacterPacket packet : unloadedPlayers) {
            if (packet.isLoaded) {
                onCharacterAdd(packet);
                System.out.println("loaded char");
                unloadedPlayers.remove(packet);
            }
        }


        for (Network.GameEntity entity : entities.values()) {

            if (entity instanceof GameObject) {
                GameObject loopedObject = (GameObject) entity;
                collision.handleStaticCollisions(staticCollisions, loopedObject);
            }

            if (entity instanceof CharacterPacket) {
                CharacterPacket player = (CharacterPacket) entity;

                if (player.combat.getHealth() <= 0) {
                    killPlayer(player);
                }

                collision.handlePlayerCollisions(entities.values(), player);

            }

            if (entity instanceof Network.NPCPacket) {
                Network.NPCPacket npc = (Network.NPCPacket) entity;

                collision.handleNPCCollision(entities.values(), npc);

                Network.UpdateNPC updateNPC = npcHandler.updateData(npc);
                if (updateNPC != null) {
                    queueMessage(new Message(updateNPC, false));
                }
            }


        }
    }

    public void addInventory(CharacterPacket packet, GameObject object) {
        Network.AddInventoryItem inventoryItem = new Network.AddInventoryItem();
        inventoryItem.object = object;

        boolean added = packet.inventory.addObject(object);

        if (added) {
            queueMessage(new Message(packet.uid, inventoryItem, false));
        } else {
            System.err.println("Not enough space in inventory");
        }


    }

    private void onCharacterAdd(CharacterPacket packet) {

        for (Network.GameEntity object : entities.values()) {
            if (object instanceof GameObject) {
                queueMessage(new Message(packet.uid, object, true));
            }
            if (object instanceof Network.NPCPacket) {
                queueMessage(new Message(packet.uid, object, true));
            }

            entities.put(packet.uid, packet);
        }

    }

    private void killPlayer(CharacterPacket packet) {
        packet.combat.setHealth(50);

        for (int i = 0; i < packet.inventory.objects.length; i++) {
            GameObject object = packet.inventory.objects[i];
            packet.inventory.removeObjectFromSlot(i);
            if (object != null) {
                object.setX(packet.x);
                object.setY(packet.y);
                addGameObject(object);
            }
        }

        packet.x = respawnX;
        packet.y = respawnY;

        //packet.isLoaded = false; // we stop comms with the player until the client says its fine again

        Network.UpdateCharacterPacket update = new Network.UpdateCharacterPacket();
        update.player = packet;
        update.hasDied = true;

        queueMessage(new Message(update, false));
    }

    public void addGameObject(GameObject object) {
        addGameObjectLocal(object);

        updateInternal = true;

        System.out.println("adding obj");
        //server.sendToAllReady(object);`
        queueMessage(new Message(object, false));
    }

    public void addGameObjectLocal(GameObject object) {
        entities.put(object.getUniqueGameId(), object);
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
        CharacterPacket packet = (CharacterPacket) entities.get(id);
        packet.combat = object;
    }

    public void removeGameObject(GameObject object) {
        Network.RemoveGameObject packet = new Network.RemoveGameObject();
        packet.uid = object.getUniqueGameId();
        //server.sendToAllReady(packet);
        queueMessage(new Message(packet, false));


       entities.remove(object.getUniqueGameId());

       //deAllocateId(object.getUniqueGameId());
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
        unloadedPlayers.add(packet);
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
