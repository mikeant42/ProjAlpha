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
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import shared.*;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

    private ConcurrentHashMap<Long, GameState> gameStates = new ConcurrentHashMap<>(); // storing previous gamestates to interpolate


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
    private double snappingDistance = 0; // this doesnt seem to work

    private long playerTick = 0;


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
            if (packet.uid == id) {
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
            if (packet.uid == id) {
                playersToRemove.add(packet);
                removeNetworkedEntity(packet.uid);
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
                if (!isPlayerHere(packet.uid)) {
                    System.out.println("Adding player " + packet.uid);
                    SpawnData data = new SpawnData(packet.x, packet.y);
                    data.put("ID", packet.uid);
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


                    FXGL.getApp().getGameWorld().spawn("Gameobject", data);

                    objectsToAdd.add(object);
                }
            }
        });
    }

    public void addProjectile(Projectile projectile) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!isObjectHere(projectile.object.getUniqueGameId())) {
                    SpawnData data = new SpawnData(projectile.projectile.originX, projectile.projectile.originY);
                    data.put("ID", projectile.object.getId());
                    data.put("uid", projectile.object.getUniqueGameId());
                    data.put("name", projectile.object.getName());
                    data.put("velX", projectile.velX);
                    data.put("velY", projectile.velY);

                    FXGL.getApp().getGameWorld().spawn("projectile", data);
                    System.out.println("adding projectile");

                    objectsToAdd.add(projectile.object);
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

    // this method of interpolation introduces lag - we are seeing what the player was doing instead of what he is
    private Point2D interpolateCharacter(double x, double y, Point2D end, double dtf) {
        double blend = 1f - Math.pow(1f - interpolationConstant, dtf * 60); // we should be at 60fps
        return FXGLMath.lerp(x, y, end.getX(), end.getY(), blend);
    }




    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    public void update(double dtf, long tick) {
        camera.updateBounds(player);


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

        Network.UserChat chat = getChatMsg(clientHandler.getCharacterPacket().uid);
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
                Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("player", packet.uid);
                if (optEnt.isPresent()) {
                    Entity entity = optEnt.get();

                    //System.out.println(entity.getPosition());
                    Network.UserChat entityChat = getChatMsg(packet.uid);

                    //if (packet.uid == entity.getComponent(NetworkedComponent.class).getId()) {
                        // We found the dude we need to update

                        if (entityChat != null) {
                            if (entity.hasComponent(OverlayTextComponent.class)) {
                                entity.getComponent(OverlayTextComponent.class).setText(entityChat.message);
                            } else {
                                entity.addComponent(new OverlayTextComponent(entityChat.message, 5));
                            }

                            messagesToAdd.remove(entityChat);

                        }


                        //if (entity.getX() == packet.x && entity.getY() == packet.y) {
                            //entity.getComponent(AnimatedMovementComponent.class).setState(Data.MovementState.STANDING);

                        //} else {

                            // this block occurs whenever we recieve an update
                            if (entity.hasComponent(AnimatedMovementComponent.class)) {




                                CharacterPacket previousPlayer = new CharacterPacket();

                                if (gameStates.containsKey(tick-10)) {
                                    List<CharacterPacket> previous = gameStates.get(tick-10).getPlayers();

                                    if (previous != null) {
                                        for (CharacterPacket previousPacket : previous) {
                                            if (packet.uid == previousPacket.uid) {
                                                previousPlayer = previousPacket;



                                            }
                                        }
                                    }

                                } else {

                                    previousPlayer.x = packet.x;
                                    previousPlayer.y = packet.y;
                                }

                                 // maybe i need the tick of when i recieved the information


                                entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);

                                double distanceX = Math.abs(previousPlayer.x - entity.getX());
                                double distanceY = Math.abs(previousPlayer.y - entity.getY());
                                if (distanceX < snappingDistance || distanceY < snappingDistance) {
                                    entity.setX(packet.x);
                                    entity.setY(packet.y);

                                } else {
                                    double blend = 1f - Math.pow(1f - interpolationConstant, dtf * 60); // we should be at 60fps
                                    entity.setPosition(interpolateCharacter(previousPlayer.x, previousPlayer.y, entity.getPosition(), dtf));



                                }


//                                entity.getComponent(NetworkedComponent.class).getEntity().setX(packet.x);
//                                entity.getComponent(NetworkedComponent.class).getEntity().setY(packet.y);
                            }

                        //}


                    //}


                }


            }


        }



        // idea
        // have an array of ints, each uid,  that need updating
        // loop through that list instead of the entire list of packets

       // long playerRecievedTick = 0;

        for (Network.NPCPacket packet : npcsHere) {

            if (packet.behaviorType != BehaviorType.STATIC) {
                Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("npc", packet.uid);
                if (optEnt.isPresent()) {
                    Entity entity = optEnt.get();
                    if ((int)entity.getX() == (int)packet.x && (int)entity.getX() == (int)packet.y) {
                        //entity.getComponent(AnimatedMovementComponent.class).setState(Data.MovementState.STANDING);
                        System.out.println("npc is standing still");
                    } else {
                        //playerRecievedTick = tick;
                        entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);
//                        entity.setX(packet.x);
//                        entity.setY(packet.y);
                        // this block occurs whenever we recieve an update
                        Network.NPCPacket previousNPC = new Network.NPCPacket();

                        if (gameStates.containsKey(tick-100)) {
                            List<Network.NPCPacket> previous = gameStates.get(tick-100).getNpcs();

                            if (previous != null) {
                                for (Network.NPCPacket previousPacket : previous) {
                                    if (packet.uid == previousPacket.uid) {
                                        previousNPC = previousPacket;

                                    }
                                }
                            }

                        } else {

                            previousNPC.x = packet.x;
                            previousNPC.y = packet.y;
                        }


                        entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);

                        double distanceX = Math.abs(previousNPC.x - entity.getX());
                        double distanceY = Math.abs(previousNPC.y - entity.getY());
                        if (distanceX < snappingDistance || distanceY < snappingDistance) {
                            entity.setX(packet.x);
                            entity.setY(packet.y);

                        } else {
                            double blend = 1f - Math.pow(1f - interpolationConstant, dtf * 60); // we should be at 60fps
                            entity.setPosition(interpolateCharacter(previousNPC.x, previousNPC.y, entity.getPosition(), dtf));



                        }
                    }

                }
            }


        }


        for (GameObject object : objectsHere) {
            Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("object", object.getUniqueGameId());
            if (optEnt.isPresent()) {
                Entity entity = optEnt.get();

                System.out.println("entity pos upf");
                if (entity.hasComponent(ProjectileComponent.class)) {
                    ProjectileComponent component = entity.getComponent(ProjectileComponent.class);
                    entity.setPosition(FXGLMath.lerp(component.getProjectilePosition().getX(), component.getProjectilePosition().getY(), object.getX(), object.getY(), 0.01));
                    //entity.setX(object.getX());
                    //entity.setY(object.getY());
                }
            }
        }



        messagesToAdd.clear();

        GameState current = new GameState();
        current.setPlayers(playersHere);
        current.setNpcs(npcsHere);

        gameStates.put(tick, current);


        for (Long state : gameStates.keySet()) {
            if (state.longValue() < tick-100) { // delete all the old gamestates
                gameStates.remove(state);
            }
        }

    }



}

