package server;

import com.almasb.fxgl.parser.tiled.TiledMap;
import com.almasb.fxgl.parser.tiled.TiledObject;
import shared.*;
import shared.collision.AlphaCollision;
import shared.collision.AlphaCollisionHandler;
import shared.objects.Fish;

import java.io.FileNotFoundException;
import java.util.*;
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

    private ProjectileManager projectileManager;

    private TiledMap map;

    private List<TiledObject> staticCollisions = new ArrayList<>();

    private boolean updateInternal = true;

    int tick = 0;


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
        staticCollisions.addAll(map.getLayerByName("collision").getObjects());

        for (TiledObject object : map.getLayerByName("spawn").getObjects()) {
            if (object.getName().equals("googon")) {
                NPC npc = new NPC("snake", (float)object.getX(), (float)object.getY());
                npc.setBehavior(BehaviorType.ROAMING);
                npcHandler.addNPC(npc, assignUniqueId());
            } else if (object.getName().equals("watcher")) {
                NPC npc = new NPC("watcher", (float)object.getX(), (float)object.getY());
                npc.setInteractable(true);
                npc.setBehavior(BehaviorType.STANDING);
                npcHandler.addNPC(npc, assignUniqueId());
            }
        }
    }


    /*
    This updates in another thread
     */
    public void updateAction(int tick) {
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
                            packet.combat.setHealth(packet.combat.getHealth()-10);
                            combat.object = packet.combat;
                            server.sendToAllReady(combat);

                        }

                    }
                }
            }

            for (NPC npc : npcHandler.getNPCs()) {
                if (AlphaCollision.doesCollide(object, npc) && object.isProjectile()) {
                    projectileManager.remove(object.getUniqueGameId());
                    removeGameObject(object);

                    Network.UpdateNPCCombat combat = new Network.UpdateNPCCombat();
                    combat.id = npc.getPacket().uid;
                    npc.getPacket().combat.setHealth(npc.getPacket().combat.getHealth()-10);
                    combat.object = npc.getPacket().combat;
                    server.sendToAllReady(combat);

                    System.out.println("proj-npc collision");
                }



            }


        }

        for (NPC npc : npcHandler.getNPCs()) {
            npc.update();
            if (npc.shouldUpdate()) {
                server.sendToAllReady(npc.formUpdate());
                System.out.println("updating npcs");
            }
        }

        //for (CharacterPacket packet : server.getLoggedIn()) {

            //for (Projectile projectile : projectileManager.getProjectiles()) {
//                if (AlphaCollision.doesCollide(projectile, packet)) {
//                    if (projectile.projectile.sourceUser != packet.id) { // the user cant harm himself with a spell
//                        projectileManager.remove(projectile.uid);
//                        System.out.println("collision " + projectile.x + " , " + projectile.y);
//
//                        // test
//                        Network.UpdatePlayerCombat combat = new Network.UpdatePlayerCombat();
//                        combat.id = packet.id;
//                        packet.combat.setHealth(packet.combat.getHealth()-10);
//                        combat.object = packet.combat;
//                        server.sendToAllReady(combat);
//
//                    }
//                }

           // }


//            for (GameObject object : objects) {
//                if (AlphaCollision.doesCollide(object, packet)) {
//
//                    if (!object.isProjectile()) {
//                        // when player runs over an object, he adds it to his inventory
//                        removeGameObject(object);
//                        addInventory(packet.id, object);
//                        System.out.println("picking up non projhectile");
//                    } else {
//                        if (projectileManager.getSource(object.getUniqueGameId()) != packet.id) { // the user cant harm himself with a spell
//                            projectileManager.remove(object.getUniqueGameId());
//                            removeGameObject(object);
//
//                             // test
//                            Network.UpdatePlayerCombat combat = new Network.UpdatePlayerCombat();
//                            combat.id = packet.id;
//                            packet.combat.setHealth(packet.combat.getHealth()-10);
//                            combat.object = packet.combat;
//                            server.sendToAllReady(combat);
//
//                        }
//
//                    }
//                }
//
//
//
//            }
//        }
//
//        for (NPC npc : npcHandler.getNPCs()) {
//            for (GameObject object : objects) {
//                if (AlphaCollision.doesCollide(object, npc)) {
//                    projectileManager.remove(object.getUniqueGameId());
//                    System.out.println("proj-npc collision");
//                }
//
//            }
//            npc.update();
//        }


    }



//        for (GameObject object : objects) {
//
//            collision.handleStaticCollisions(staticCollisions, object);
//
//            // player vs object collision
//            for (CharacterPacket packet : server.getLoggedIn()) {
//
//                if (AlphaCollision.doesCollide(object, packet)) {
//
//                    if (!object.isProjectile()) {
//                        // when player runs over an object, he adds it to his inventory
//                        removeGameObject(object);
//                        addInventory(packet.id, object);
//                        System.out.println("picking up non projhectile");
//                    }
//
//
//
//                }
//            }

//            for (NPC npc : npcHandler.getNPCs()) {
//                if (AlphaCollision.doesProjectileCollide(object, npc)) {
//                    System.out.println("projectile-npc collision");
//                    removeGameObject(object);
//                }
//            }


      //  }
    //}

    public void addInventory(int cid, GameObject object) {
        Network.AddInventoryItem inventoryItem = new Network.AddInventoryItem();
        inventoryItem.object = object;
        server.sendToTCP(cid, inventoryItem);
    }

    private void onCharacterAdd(CharacterPacket packet) {
        // We also have to spawn all the npcs in his level
        for (NPC npc : npcHandler.getNPCs()) {
            server.sendWithQueue(packet.id, npc.getPacket(), true);
        }

        for (GameObject object : objects) {
            server.sendWithQueue(packet.id, object, true);
        }

    }

    public void addGameObject(GameObject object) {
        objectsToAdd.add(object);
        updateInternal = true;

        server.sendToAllReady(object);
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
        server.sendToAllReady(packet);

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
        projectileManager.addProjectile(packet, tick);
    }

    public int getMapID() {
        return mapID;
    }
}
