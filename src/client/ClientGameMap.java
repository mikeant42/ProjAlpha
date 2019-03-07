package client;

import client.render.*;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.parser.tiled.TiledMap;
import com.almasb.fxgl.parser.tiled.TiledObject;
import com.almasb.fxgl.util.Optional;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import shared.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
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

    private HashMap<Long, GameState> gameStates = new HashMap<>();


    // chat messages auto-remove
    private List<Network.UserChat> messagesToAdd = new ArrayList<>();

    private TiledMap map;

    private Entity player;

    public static int TILESIZE = 256;

    private ClientHandler clientHandler;

    private boolean isMapLoaded = false;

    private Camera camera;

    private boolean needsChange = true;
    private int changeTick = 0;

    // these are for players and npcs
    private double interpolationConstant = 0.9;
    private double snappingDistance = 0;


    public ClientGameMap() {
    }

    public void init(ClientHandler handler) {
        this.clientHandler = handler;

        camera = new Camera();

        setMap(clientHandler.getLatestWorldID());

        clientHandler.sendReady(true);



    }

    public void setMap(int id) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (id == 1) {
                    try {
                        map = AlphaUtil.parseWorld("src/assets/json/ult.xml");/////

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }


                // This is where all of the pre-game, post-login stuff occurs
                // this isMapLoaded flag alerts the server that we are ready to recieve information
                // this could become a general-use flag to stop the server from sending info

                // disable the spawn layer, this is only for the server
                map.getLayerByName("spawn").setObjects(new ArrayList<TiledObject>());

                FXGL.getApp().getGameWorld().setLevelFromMap(map);


                SpawnData data = new SpawnData(300, 300);
                data.put("ID", clientHandler.getId());
                player = FXGL.getApp().getGameWorld().spawn("localplayer", data);

                camera.bind(player);

                isMapLoaded = true;


            }
        });
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
        if (id == clientHandler.getId()) {
            System.err.println("Attempting to remove our own player!");
            return;
        }

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
        System.out.println("removing " + object.getName());
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

//    public void addProjectile(Network.AddProjectile projectile) {
//        System.out.println("should add");
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                SpawnData data = new SpawnData(projectile.originX, projectile.originY);
//                data.put("mouseX", projectile.destinationX);
//                data.put("mouseY", projectile.destinationY);
//                data.put("name", "tornado");
//                if (projectile.sourceUser == clientHandler.getId()) {
//                    data.put("doesOwn", true);
//                } else {
//                    data.put("doesOwn", false);
//                }
//
//                FXGL.getApp().getGameWorld().spawn("projectile", data);
//            }
//        });
//
//    }

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
                    data.put("name", packet.name);

                    if (packet.behaviorType == BehaviorType.ROAMING) {
                        FXGL.getApp().getGameWorld().spawn("Roaming NPC", data);
                    } else if (packet.behaviorType == BehaviorType.STATIC) {
                        // only standing npcs can trade and dialogue
                        data.put("interactable", packet.interactable);
                        data.put("trader", packet.trader);

                        FXGL.getApp().getGameWorld().spawn("Standing NPC", data);
                    }

                    npcsToAdd.add(packet);
                }
            }
        });
    }


    public void updatePlayerCombat(int id, CombatObject object) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updateCombatLocal("player", id, object);
            }
        });
    }

    public void updateNPCCombat(int id, CombatObject object) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updateCombatLocal("npc", id, object);
            }
        });
    }


    private void updateCombatLocal(String type, int id, CombatObject object) {
        Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID(type, id);
        if (optEnt.isPresent()) {
            Entity entity = optEnt.get();
            if (entity.hasComponent(CombatComponent.class)) {
                entity.getComponent(CombatComponent.class).setCombatObject(object);
            } else {
                entity.addComponent(new CombatComponent(object));
                entity.addComponent(new OverlayHealthComponent(Color.GREEN));
            }
        }
    }


    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    public void update(double dtf, long tick) {
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

                    //if (packet.id == entity.getComponent(NetworkedComponent.class).getId()) {
                        // We found the dude we need to update

                        if (entityChat != null) {
                            if (entity.hasComponent(OverlayTextComponent.class)) {
                                entity.getComponent(OverlayTextComponent.class).setText(entityChat.message);
                            } else {
                                entity.addComponent(new OverlayTextComponent(entityChat.message, 5));
                            }

                            messagesToAdd.remove(entityChat);

                        }


                        if (entity.getX() == packet.x && entity.getY() == packet.y) {
                            //entity.getComponent(AnimatedMovementComponent.class).setState(Data.MovementState.STANDING);

                        } else {

                            // this block occurs whenever we recieve an update
                            if (entity.hasComponent(AnimatedMovementComponent.class)) {



                                CharacterPacket previousPlayer = new CharacterPacket();

                                if (gameStates.containsKey(tick-20)) {
                                    List<CharacterPacket> previous = gameStates.get(tick - 20).getPlayers();

                                    if (previous != null) {
                                        for (CharacterPacket previousPacket : previous) {
                                            if (packet.id == previousPacket.id) {
                                                previousPlayer = previousPacket;

                                            }
                                        }
                                    }

                                } else {

                                    previousPlayer.x = entity.getX();
                                    previousPlayer.y = entity.getY();
                                }


                                entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);

                                double distanceX = previousPlayer.x - entity.getX();
                                double distanceY = previousPlayer.y - entity.getY();
                                if (distanceX < snappingDistance && distanceY < snappingDistance) {
                                    entity.setX(packet.x);
                                    entity.setY(packet.y);

                                } else {
                                    //entity.setX(distanceX * dtf * interpolationConstant);
                                    //entity.setY(distanceY * dtf * interpolationConstant);
                                    entity.setPosition(FXGLMath.lerp(previousPlayer.x, previousPlayer.y, entity.getX(), entity.getY(), interpolationConstant));
                                    System.out.println(distanceX);
                                }


//                                entity.getComponent(NetworkedComponent.class).getEntity().setX(packet.x);
//                                entity.getComponent(NetworkedComponent.class).getEntity().setY(packet.y);
                            }

                        }


                    //}


                }


            }


        }



        // idea
        // have an array of ints, each uid,  that need updating
        // loop through that list instead of the entire list of packets


        for (Network.NPCPacket packet : npcsHere) {

            if (packet.behaviorType != BehaviorType.STATIC) {
                Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("npc", packet.uid);
                if (optEnt.isPresent()) {
                    Entity entity = optEnt.get();
                    if ((int)entity.getX() == (int)packet.x && (int)entity.getX() == (int)packet.y) {
                        //entity.getComponent(AnimatedMovementComponent.class).setState(Data.MovementState.STANDING);
                        System.out.println("npc is standing still");
                    } else {
                        entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);
//                        entity.setX(packet.x);
//                        entity.setY(packet.y);
                        // this block occurs whenever we recieve an update
                        Network.NPCPacket previousNPC = new Network.NPCPacket();

                        if (gameStates.containsKey(tick-20)) {
                            List<Network.NPCPacket> previous = gameStates.get(tick - 20).getNpcs();

                            if (previous != null) {
                                for (Network.NPCPacket previousPacket : previous) {
                                    if (packet.uid == previousPacket.uid) {
                                        previousNPC = previousPacket;

                                    }
                                }
                            }

                        } else {

                            previousNPC.x = entity.getX();
                            previousNPC.y = entity.getY();
                        }


                        entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);

                        double distanceX = previousNPC.x - entity.getX();
                        double distanceY = previousNPC.y - entity.getY();
                        if (distanceX < snappingDistance && distanceY < snappingDistance) {
                            entity.setX(packet.x);
                            entity.setY(packet.y);

                        } else {
                            //entity.setX(distanceX * dtf * interpolationConstant);
                            //entity.setY(distanceY * dtf * interpolationConstant);
                            entity.setPosition(FXGLMath.lerp(previousNPC.x, previousNPC.y, entity.getX(), entity.getY(), interpolationConstant));
                            System.out.println(distanceX);
                        }

//                                entity.getComponent(NetworkedComponent.class).getEntity().setX(packet.x);
//                                entity.getComponent(NetworkedComponent.class).getEntity().setY(packet.y);
                    }

                }
            }


        }


        for (GameObject object : objectsHere) {
            Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("object", object.getUniqueGameId());
            if (optEnt.isPresent()) {
                Entity entity = optEnt.get();

                System.out.println("entity pos upf");
                entity.setX(object.getX());
                entity.setY(object.getY());
            }
        }



        messagesToAdd.clear();

        GameState current = new GameState();
        current.setPlayers(playersHere);
        current.setNpcs(npcsHere);

        gameStates.put(tick, current);

    }



}

