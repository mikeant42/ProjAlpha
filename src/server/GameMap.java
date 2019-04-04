package server;

import com.almasb.fxgl.parser.tiled.TiledMap;
import com.almasb.fxgl.parser.tiled.TiledObject;
import com.esotericsoftware.minlog.Log;
import server.message.Message;
import server.npc.NPCHandler;
import shared.*;
import shared.collision.AlphaCollision;
import shared.collision.AlphaCollisionHandler;
import shared.objects.Fish;

import java.util.*;
import java.util.concurrent.*;

class SpawnPoint {
    double x,y;
    double respawnTime;
}

public class GameMap {
    /*
    There is one of these per map. Each one holds positions of npc, random loot, and all objects residing on the server

    maybe create some sort of IDManager so i can let other classes assign unique ids
     */

    //private int objectLimit = 1024; // leave room for projectiles, who's uid gets de-allocated when finished

    private NPCHandler npcHandler;

    private AlphaServer server;

    private int mapID;

    private Queue<CharacterPacket> unloadedPlayers = new ConcurrentLinkedQueue<>();


    // this is the entire stack of data the gamemap sends out each time it broadcasts to the client
    private BlockingQueue<Message> objectsToSend = new ArrayBlockingQueue<Message>(10000);


    // 16 seperate threads allowed to access this map
    // maps the (uid, object) of each game entity
    private Map<Integer, Network.GameEntity> entities = new ConcurrentHashMap<>();

    private ProjectileHandler projectileHandler;

    private TiledMap map;

    private List<TiledObject> staticCollisions = new ArrayList<>();

    private double respawnX, respawnY; // a player respawn point in every map

    private long tick = 0;

    private List<Integer> lockedPlayers = new ArrayList<>();

    private Map<Network.NPCPacket, SpawnPoint> spawnPoints = new ConcurrentHashMap<>();


    // 128 is the limit of the number of game objects in one map
    //private int[] uniqueObjects = new int[objectLimit];
    // once an id is assigned, it cannot be used again. this is to prevent old object ids from referencing new objects that they thought was the old object
    //private List<Integer> uniques = new ArrayList<>();

    private AlphaCollision collision;

    public GameMap(AlphaServer server) {
        this.npcHandler = new NPCHandler();

        this.server = server;

        this.mapID = 1;

     //   try {
            map = AlphaUtil.parseWorld(getClass().getClassLoader().getResourceAsStream("src/assets/json/starter.xml"));
     //   } catch (FileNotFoundException e) {
     //       e.printStackTrace();
       // }

        addCollidables();

        projectileHandler = new ProjectileHandler();

        collision = new AlphaCollision(new AlphaCollisionHandler() {
            @Override
            public void handleCollision(TiledObject object, GameObject projectile) {
                if (projectile.isProjectile()) {
                    removeGameObject(projectile);
                    projectileHandler.remove(projectile.getUniqueGameId());
                    System.out.println("world collision");
                }
            }

            @Override
            public void handleCollision(GameObject object, CharacterPacket player) {
                if (!object.isProjectile()) {
                    // when player runs over an object, he adds it to his inventory
                    try {
                        addInventory(player, object);
                        removeGameObject(object);
                        System.out.println("picking up non projectile");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    if (projectileHandler.getSourcePlayerID(object.getUniqueGameId()) != player.uid) { // the user cant harm himself with a spell
                        System.out.println("ergergre");

                        // test
                        Network.UpdatePlayerCombat combat = new Network.UpdatePlayerCombat();
                        combat.id = player.uid;
                        if (player.combat.getShield() != Data.Shield.NONE)
                            System.out.println("hitting with shield");
                        player.combat.setHealth(projectileHit(projectileHandler.get(object.getUniqueGameId()), player));
                        combat.object = player.combat;

                        queueMessage(new Message(combat, false));

                        if (player.combat.getHealth() <= 0) {
                            killPlayer(player);
                            System.out.println("Client " + projectileHandler.getSourcePlayerID(object.getUniqueGameId()) + " killed client " + player.uid);
                        }

                        removeGameObject(object);
                        projectileHandler.remove(object.getUniqueGameId());

                    }

                }
            }

            @Override
            public void handleCollision(GameObject object, Network.NPCPacket npc) {
                if (npc.type == EntityType.ENEMY &&  object.isProjectile()) {

                    // gameplay idea - if you attack weak mop npcs, the surrounding mob will start attacking you.

                    Network.UpdateNPCCombat combat = new Network.UpdateNPCCombat();
                    combat.id = npc.uid;
                    npc.combat.setHealth(projectileHit(projectileHandler.get(object.getUniqueGameId()), npc));
                    combat.object = npc.combat;

                    removeGameObject(object);
                    projectileHandler.remove(object.getUniqueGameId());


                    if (npc.combat.getHealth() < 0) {
                        killNPC(npc);
                    } else {
                        queueMessage(new Message(combat, false));
                    }

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
            object.setUniqueGameId(IDManager.assignUniqueId());
//        object.setOnUse(new AlphaCollisionHandler() {
//            @Override
//            public void onUse(CharacterPacket packet) {
//
//            }
//        });
            addGameObjectLocal(object);
        }

    }

    private int projectileHit(Projectile projectile, Network.CombatEntity packet) {
        //System.out.println("expired : " + projectileHandler.hasExpired(projectile.object.getUniqueGameId()));
        if (projectile != null && !projectileHandler.hasExpired(projectile.object.getUniqueGameId())) {
            double damage = projectile.damageEffect;
            if (packet.combat.getShield() == Data.Shield.GRAVITY) {
                damage *= .9;
            }
            return (int) (packet.combat.getHealth() - damage);
        } else {
            return 0;
        }
    }


    private void addSpawnPoint(Network.NPCPacket packet) {
        SpawnPoint point = new SpawnPoint();
        point.respawnTime = 5;
        point.x = packet.x;
        point.y = packet.y;
        //spawn(point, packet);

        spawnPoints.put(packet, point);
        entities.put(packet.uid, packet);

    }

    public void triggerRespawn(Network.NPCPacket packet) {
        SpawnPoint point = spawnPoints.get(packet);
        if (point != null) {
            Network.NPCPacket newPacket = npcHandler.initMob(point.x, point.y, packet.name);
            addSpawnPoint(newPacket);
            queueMessage(new Message(newPacket, true));
        }
    }

    private void addCollidables() {
        staticCollisions.addAll(map.getLayerByName("collision").getObjects());

        for (TiledObject object : map.getLayerByName("spawn").getObjects()) {
            if (object.getType().equals("mob")) {
                Network.NPCPacket npc = npcHandler.initMob(object.getX(), object.getY(), object.getName());
                addSpawnPoint(npc);
            }
            if (object.getName().equals("respawn")) {
                respawnX = object.getX();
                respawnY = object.getY();
//            } else if (object.getName().equals("googon")) {
//                Network.NPCPacket npc = npcHandler.initMob(object.getX(), object.getY(), "googon");
////                Network.NPCPacket packet = new Network.NPCPacket();
////                packet.x = object.getX();
////                packet.y = object.getY();
////                packet.uid = assignUniqueId();
////                packet.type = EntityType.ENEMY;
////                packet.name = "googon";
////                packet.combat = new CombatObject(100,0);
//                //npcHandler.registerBehavior(packet, BehaviorType.ROAMING);
//                //entities.put(npc.uid, npc);
//                addSpawnPoint(npc);
            } else if (object.getName().equals("watcher")) {
                Network.NPCPacket packet = new Network.NPCPacket();
                packet.x = object.getX();
                packet.y = object.getY();
                packet.uid = IDManager.assignUniqueId();
                packet.type = EntityType.INTERACTABLE_NPC;
                packet.name = object.getName();
                //packet.combat = new CombatObject(100,0);
                npcHandler.registerBehavior(packet, BehaviorType.STATIC);
                entities.put(packet.uid, packet);
            }
        }
    }


    /*
    This updates in its own thread at a lower rate than the server action updates
     */
    public void updateExternal(long broadcastTick) {
        // this is the code broadcasting to the clients
        // this code will only run ~10 times a second

        for (Message packet : objectsToSend) {
                packet.send(server);
                objectsToSend.remove(packet);
        }

    }

    public void updatePlayerInventory(int uid, Inventory inventory) {
        CharacterPacket packet = (CharacterPacket) entities.get(uid);
        if (packet != null && packet.isLoaded) {
            packet.inventory = inventory;

        }
    }

    public void removePlayer(int uid) {
        entities.remove(uid);
    }

    public void killNPC(Network.NPCPacket packet) {
        entities.remove(packet.uid);
        npcHandler.removeNPC(packet.uid);

        Network.KillNPC kill = new Network.KillNPC();
        kill.uid = packet.uid;
        kill.killerID = -1;
        queueMessage(new Message(kill, false));

        Fish fish = new Fish();
        fish.setUniqueGameId(IDManager.assignUniqueId());
        fish.setX(packet.x);
        fish.setY(packet.y);
        addGameObject(fish);


        triggerRespawn(packet);
    }

    /*
    This updates in another thread at 60fps
     */
    public void updateAction(long tick) {
        this.tick = tick;

        //projectileHandler.update(tick);

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

                if (loopedObject.isProjectile()) {
                    //System.out.println(loopedObject.getUniqueGameId() + " " + loopedObject.getName());
                    GameObject projObject = projectileHandler.updateProjectile(loopedObject.getUniqueGameId(), tick);
                    // if this is null it means the object is expired, and needs to be removed
                    if (projObject != null) {
                        updateObjectPosition(projObject);
                    } else {
                        removeGameObject(loopedObject);
                        projectileHandler.remove(loopedObject.getUniqueGameId());
                    }
                }
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

                // if the npc dies, this message doesn't need to be sent. This message is wasteful if executed before a death.
                Network.UpdateNPC updateNPC = npcHandler.updateData(npc.uid);
                if (updateNPC != null) {
                    queueMessage(new Message(updateNPC, false));

                    // update the npcs internal values
                    npc.x = updateNPC.x;
                    npc.y = updateNPC.y;
                }

                collision.handleNPCCollision(entities.values(), npc);
            }


        }
    }

    private void addInventory(CharacterPacket packet, GameObject object) throws Exception {
        Network.AddInventoryItem inventoryItem = new Network.AddInventoryItem();
        inventoryItem.object = object;

        boolean added = packet.inventory.addObject(object);

        if (added) {
            queueMessage(new Message(packet.uid, inventoryItem, false));
        } else {
            throw new Exception("Not enough space in inventory");
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

            if (packet.inventory.objects.length > 0) {
                for (int i = 0; i < packet.inventory.objects.length; i++) {
                    GameObject object1 = packet.inventory.getObjectSlot(i);
                    if (object1 == null) {
                        packet.inventory.removeObjectFromSlot(i);
                        Log.error("Player " + packet.uid + " has an invalid inventory object");
                    } else {
                        object1.setUniqueGameId(IDManager.assignUniqueId()); // object needs a new uid
                    }
                }
            }

            entities.put(packet.uid, packet);
        }

    }

    protected void lockPlayer(int uid) {
//        synchronized (lockedPlayers) {
//            lockedPlayers.add(uid);
//        }
    }

    private void killPlayer(CharacterPacket packet) {
        packet.combat.setHealth(50);

        if (packet.inventory.objects.length > 0) {
            for (int i = 0; i < packet.inventory.objects.length; i++) {
                GameObject object = packet.inventory.objects[i];
                packet.inventory.removeObjectFromSlot(i);
                if (object != null) {
                    object.setX(packet.x);
                    object.setY(packet.y);
                    addGameObject(object);
                }
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

    //public void executeOnTick(long tick, Runnable runnable)

    public void addGameObject(GameObject object) {
        addGameObjectLocal(object);
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

       if (object.isProjectile()) {
           IDManager.deAllocateId(object.getUniqueGameId());
       }

       //deAllocateId(object.getUniqueGameId());
    }

//    private void deAllocateId(int id) {
//        synchronized (uniques) {
//            uniques.remove(new Integer(id));
//        }
//    }
//
//    // IT ASSIGNED DUPLICATES!!!!!!!!!!!!!!!!!!!!!!!!!!!!! FIX FIX
//    // this needs to be seperated from the game map, because by this logic objects from two different maps can have the same ids
//    // if you gen too many ids this will cause a stackoverflow
//    public int assignUniqueId() {
//        synchronized (uniques) {
//            int num = ThreadLocalRandom.current().nextInt(AlphaServer.PLAYER_COUNT, objectLimit + 1);
//
//            if (uniques.contains(num)) {
//                num = assignUniqueId();
//            } else {
//                uniques.add(num);
//            }
//
//            return num;
//        }
//    }


    public void addUnloadedPlayer(CharacterPacket packet) {
        unloadedPlayers.add(packet);
    }


    public NPCHandler getNPCHandler() {
        return npcHandler;
    }

    public void addProjectile(Network.AddProjectile packet) {
        Projectile projectile = projectileHandler.addProjectile(packet, tick);
        addGameObjectLocal(projectile.object);
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
