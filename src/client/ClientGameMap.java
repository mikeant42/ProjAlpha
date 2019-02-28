package client;

import client.render.Camera;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.parser.tiled.TMXParser;
import com.almasb.fxgl.parser.tiled.TiledMap;
import com.almasb.fxgl.util.Optional;
import javafx.application.Platform;
import shared.CharacterPacket;
import shared.GameObject;
import shared.Network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClientGameMap {

    private List<CharacterPacket> playersHere = new ArrayList<>();
    private List<Network.NPCPacket> npcsHere = new ArrayList<>();
    private List<GameObject> objectsHere = new ArrayList<>();

    private List<CharacterPacket> playersToAdd = new ArrayList<>();
    private List<GameObject> objectsToAdd = new ArrayList<>();
    private List<Network.NPCPacket> npcsToAdd = new ArrayList<>();

    private List<CharacterPacket> playersToRemove = new ArrayList<>();
    private List<GameObject> objectsToRemove = new ArrayList<>();
    private List<Network.NPCPacket> npcsToRemove = new ArrayList<>();


    private List<Network.UserChat> messagesToAdd = new ArrayList<>();

    private TiledMap map;

    private Entity player;

    public static int TILESIZE = 256;

    private ClientHandler clientHandler;

    private boolean isMapLoaded = false;

    private Camera camera;


    public ClientGameMap() {
    }

    public void init(ClientHandler handler) {
        this.clientHandler = handler;

        camera = new Camera();

        setMap(clientHandler.getLatestWorldID());



    }

    public void setMap(int id) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (id == 1) {
                    try {
                        map = parseWorld("src/assets/json/ult.xml");/////

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }



                FXGL.getApp().getGameWorld().setLevelFromMap(map);


                SpawnData data = new SpawnData(50, 50);
                data.put("ID", clientHandler.getId());
                player = FXGL.getApp().getGameWorld().spawn("localplayer", data);

                camera.bind(player);

                isMapLoaded = true;


            }
        });
    }

    private TiledMap parseWorld(String file) throws FileNotFoundException{
        File initialFile = new File(file);
        InputStream targetStream = new FileInputStream(initialFile);


        TMXParser parser = new TMXParser();
        TiledMap map = parser.parse(targetStream);
//        map.getLayerByName("Treetop").setDraworder("topdown");

        return map;
    }



    private boolean isPlayerHere(int id) {
        for (CharacterPacket packet : playersHere) {
            if (packet.id == id) {
                return true;
            }
        }
        return false;
    }

    private boolean isNPCHere(int id) {
        for (Network.NPCPacket packet : npcsHere) {
            if (packet.uid == id) {
                return true;
            }
        }
        return false;
    }

    private boolean isObjectHere(int uid) {
        for (GameObject object : objectsHere) {
            if (object.getUniqueGameId() == uid) {
                return true;
            }
        }
        return false;
    }



    public void removeNetworkedEntity(int id) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                List<Entity> charr = FXGL.getApp().getGameWorld().getEntitiesByComponent(NetworkedComponent.class);
                for (Entity c : charr) {
                    if (c.getComponent(NetworkedComponent.class).getId() == id) {
                        c.removeFromWorld();
                    }
                }
            }
        });

    }

    public void removeGameObject(GameObject object) {
        objectsToRemove.add(object);
        System.out.println("removing" + object.getName());
        removeNetworkedEntity(object.getUniqueGameId());
    }

    public void removePlayer(int id) {
        for (CharacterPacket packet : playersHere) {
            if (packet.id == id) {
                playersToRemove.add(packet);
                removeNetworkedEntity(packet.id);
                return;
            }
        }

    }

    public void addChatMsg(Network.UserChat chat) {
        messagesToAdd.add(chat);
    }


    public Network.UserChat getChatMsg(int cid) {
        for (Network.UserChat chat : messagesToAdd) {
            if (chat.cid == cid) {
                return chat;
            }
        }

        return null;
    }

    public Entity getPlayer() {
        return player;
    }

    public void addPlayer(CharacterPacket packet) {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                if (!isPlayerHere(packet.id)) {
                    System.out.println("Adding player " + packet.id);
                    SpawnData data = new SpawnData(packet.x, packet.y);
                    data.put("ID", packet.id);
                    data.put("user", packet.name);
                    FXGL.getApp().getGameWorld().spawn("player", data);
                    playersToAdd.add(packet);
                }
            };
        });
    }

    public void addGameObject(GameObject object) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!isObjectHere(object.getUniqueGameId())) {
                    SpawnData data = new SpawnData(object.getX(), object.getY());
                    data.put("ID", object.getId());
                    data.put("uid", object.getUniqueGameId());
                    data.put("name", object.getName());

                    if (object.isProjectile()) {
                        FXGL.getApp().getGameWorld().spawn("projectile", data);
                        System.out.println("adding projectile");
                    } else {
                        FXGL.getApp().getGameWorld().spawn("Gameobject", data);
                    }

                    objectsToAdd.add(object);
                }
            }
        });
    }

    public void addNPC(Network.NPCPacket packet) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!isNPCHere(packet.uid)) {
                    System.out.println("Spawning npc " + packet.uid);
                    SpawnData data = new SpawnData(packet.x, packet.y);
                    data.put("ID", packet.uid);
                    FXGL.getApp().getGameWorld().spawn("Roaming NPC", data);
                    npcsToAdd.add(packet);
                }
            }
        });
    }


    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    public void update() {
        playersHere.addAll(playersToAdd);
        playersToAdd.clear();

        playersHere.removeAll(playersToRemove);
        playersToRemove.clear();

        objectsHere.addAll(objectsToAdd);
        objectsToAdd.clear();

        objectsHere.removeAll(objectsToRemove);
        objectsToRemove.clear();

        npcsHere.addAll(npcsToAdd);
        npcsToAdd.clear();

        npcsHere.removeAll(npcsToRemove);
        npcsToRemove.clear();


        // 1. Have the server send us the world.
        // 2. Draw and update all visible players
        //    - All of the networked npcs/players should update within their own loop

        // haven't been able to reproduce bug where the networkedcomponent is null. needs more investigation
        if (player.hasComponent(NetworkedComponent.class))
            player.getComponent(NetworkedComponent.class).update();

        Network.UserChat chat = getChatMsg(clientHandler.getCharacterPacket().id);
        if (chat != null) {
            if (player.hasComponent(OverlayTextComponent.class)) {
                player.getComponent(OverlayTextComponent.class).setText(chat.message);
            } else {
                player.addComponent(new OverlayTextComponent(chat.message, 5));
            }
            messagesToAdd.remove(chat);
        }


        if (!clientHandler.getOtherPlayers().isEmpty()) {

            for (CharacterPacket packet : playersHere) {

                // Update the other players
                Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("player", packet.id);
                if (optEnt.isPresent()) {
                    Entity entity = optEnt.get();

                    //System.out.println(entity.getPosition());
                    Network.UserChat entityChat = getChatMsg(packet.id);

                    if (packet.id == entity.getComponent(NetworkedComponent.class).getId()) {
                        // We found the dude we need to update

                        if (entityChat != null) {
                            if (entity.hasComponent(OverlayTextComponent.class)) {
                                entity.getComponent(OverlayTextComponent.class).setText(entityChat.message);
                            } else {
                                entity.addComponent(new OverlayTextComponent(entityChat.message, 5));
                            }

                            messagesToAdd.remove(entityChat);

                        }

                        entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);
                        int moveState = packet.moveState;
                        entity.getComponent(NetworkedComponent.class).getEntity().setX(packet.x);
                        entity.getComponent(NetworkedComponent.class).getEntity().setY(packet.y);


                    }


                }


            }


        }



        for (Network.NPCPacket packet : npcsHere) {
//            if (!isNPCHere(packet.uid)) {
//                System.out.println("Spawning npc " + packet.uid);
//                SpawnData data = new SpawnData(packet.x, packet.y);
//                data.put("ID", packet.uid);
//                FXGL.getApp().getGameWorld().spawn("Roaming NPC", data);
//                npcsHere.add(packet);
//            }

            Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("npc", packet.uid);
            if (optEnt.isPresent()) {
                Entity entity = optEnt.get();
//            List<Entity> entities = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.NPC);
//            for (Entity entity : entities) {
                //if (packet.uid == entity.getInt("ID")) {
                    entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);
                    entity.setX(packet.x);
                    entity.setY(packet.y);
                //}
            }


        }


        for (GameObject object : clientHandler.getObjects()) {
            Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("npc", object.getUniqueGameId());
            if (optEnt.isPresent()) {
                Entity entity = optEnt.get();

                System.out.println(object.getX());
                entity.setX(object.getX());
                entity.setY(object.getY());
            }
        }

//        for (GameObject object : clientHandler.getObjects()) {
//            if (!isObjectHere(object.getUniqueGameId())) {
//                SpawnData data = new SpawnData(object.getX(), object.getY());
//                data.put("ID", object.getId());
//                data.put("uid", object.getUniqueGameId());
//                data.put("name", object.getName());
//
//                FXGL.getApp().getGameWorld().spawn("Gameobject", data);
//
//                objectsHere.add(object);
//            }
//        }


        messagesToAdd.clear();

    }



}

