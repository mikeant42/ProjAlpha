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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import shared.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientGameMap {
    private Map<Integer, Network.GameEntity> entities = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, GameState> gameStates = new ConcurrentHashMap<>(); // storing previous gamestates to interpolate


    // chat messages auto-remove
    private List<Network.UserChat> messagesToAdd = new ArrayList<>();

    private TiledMap map;

    private Entity player;

    private ClientHandler clientHandler;

    private boolean isMapLoaded = false;

    private Camera camera;

    private int changeTick = 0;

    // these are for players and npcs
    private double interpolationConstant = 0.9;
    private double snappingDistance = 0.9; //

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
                        map = AlphaUtil.parseWorld("src/assets/json/starter.xml");/////

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }


                // This is where all of the pre-game, post-login stuff occurs
                // this isMapLoaded flag alerts the server that we are ready to recieve information
                // this could become a general-use flag to stop the server from sending info

                // disable the spawn layer, this is only for the server
                map.getLayerByName("spawn").setObjects(new ArrayList<TiledObject>());
                map.setBackgroundcolor("Green");

                FXGL.getApp().getGameWorld().setLevelFromMap(map);


                SpawnData data = new SpawnData(300, 300);
                data.put("ID", clientHandler.getId());
                player = FXGL.getApp().getGameWorld().spawn("localplayer", data);

                camera.bind(player);

                isMapLoaded = true;


            }
        });
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
                if (entities.get(packet.uid) == null) {
                    System.out.println("Adding player " + packet.uid);
                    SpawnData data = new SpawnData(packet.x, packet.y);
                    data.put("ID", packet.uid);
                    data.put("user", packet.name);
                    FXGL.getApp().getGameWorld().spawn("player", data);
                    //playersToAdd.add(packet);
                    entities.put(packet.uid, packet);
                }
            };
        });
    }


    public void addGameObject(GameObject object) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (entities.get(object.getUniqueGameId()) == null) {
                    SpawnData data = new SpawnData(object.getX(), object.getY());
                    data.put("ID", object.getId());
                    data.put("uid", object.getUniqueGameId());
                    data.put("name", object.getName());


                    FXGL.getApp().getGameWorld().spawn("Gameobject", data);

                    //objectsToAdd.add(object);
                    entities.put(object.getUniqueGameId(), object);
                }
            }
        });
    }

    public void addProjectile(Projectile projectile) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (entities.get(projectile.object.getUniqueGameId()) == null) {
                    SpawnData data = new SpawnData(projectile.projectile.originX, projectile.projectile.originY);
                    data.put("ID", projectile.object.getId());
                    data.put("uid", projectile.object.getUniqueGameId());
                    data.put("name", projectile.object.getName());
                    data.put("velX", projectile.velX);
                    data.put("velY", projectile.velY);

                    FXGL.getApp().getGameWorld().spawn("projectile", data);
                    System.out.println("adding projectile");

                    //objectsToAdd.add(projectile.object);
                    entities.put(projectile.object.getUniqueGameId(), projectile.object);
                }
            }
        });
    }

    public void addNPC(Network.NPCPacket packet) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (entities.get(packet.uid) == null) {
                    System.out.println("Spawning npc " + packet.uid);
                    SpawnData data = new SpawnData(packet.x, packet.y);
                    data.put("ID", packet.uid);
                    data.put("name", packet.name);
                    data.put("type", packet.type);

                    if (packet.behaviorType == BehaviorType.ROAMING) {
                        FXGL.getApp().getGameWorld().spawn("Roaming NPC", data);
                    } else if (packet.behaviorType == BehaviorType.STATIC) {
                        // only standing npcs can trade and dialogue
                        data.put("interactable", packet.interactable);
                        data.put("trader", packet.trader);

                        FXGL.getApp().getGameWorld().spawn("Standing NPC", data);
                    }

                    //npcsToAdd.add(packet);
                    entities.put(packet.uid, packet);
                }
            }
        });
    }



    public void updateCombatLocal(int id, CombatObject object) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Entity entity = null;
                try {
                    entity = getEntityFromId(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (entity != null) {
                    if (entity.hasComponent(CombatComponent.class)) {
                        entity.getComponent(CombatComponent.class).setCombatObject(object);
                    } else {
                        entity.addComponent(new CombatComponent(object));
                        entity.addComponent(new OverlayHealthComponent(Color.GREEN));
                    }
                    entity.getComponent(CombatComponent.class).updateShield();

                }
            }
        });
    }

    private Entity getEntityFromId(int uid) throws Exception {
        Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("entity", uid);
        if (optEnt.isPresent()) {
            Entity entity = optEnt.get();
            return entity;
        } else {
            throw new Exception("Tried to select entity that does not exist in client game world");
        }

    }

    public void removeEntityLater(int uid) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    getEntityFromId(uid).removeFromWorld();
                    Network.GameEntity object = entities.get(uid);
                    if (object instanceof GameObject) {
                        GameObject object1 = (GameObject)object;
                        if (object1.isProjectile()) {
                            FXGL.getApp().getGameWorld().spawn("spell impact", object1.getX(), object1.getY());
                        }
                    }
                    entities.remove(uid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    // this method of interpolation introduces lag - we are seeing what the player was doing instead of what he is
    private Point2D interpolateCharacter(double x, double y, Point2D end, double dtf) {
        double blend = 1f - Math.pow(1f - interpolationConstant, dtf * 60); // we should be at 60fps
        return FXGLMath.lerp(x, y, end.getX(), end.getY(), blend);
    }

    private Point2D interpolate(double startX, double startY, double posX, double posY){

        double differenzX = posX-startX;
        double differenzY = posY-startY;

        startX += differenzX/2;
        startX += differenzY/2;

        return new Point2D(startX, startY);

    }




    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    public void update(double dtf, long tick) {
        camera.updateBounds(player);


        // 1. Have the server send us the world.
        // 2. Draw and update all visible players
        //    - All of the networked npcs/players should update within their own loop

        // haven't been able to reproduce bug where the networkedcomponent is null. needs more investigation
        if (player.hasComponent(LocalPlayerComponent.class))
            player.getComponent(LocalPlayerComponent.class).update();

        Network.UserChat chat = getChatMsg(clientHandler.getCharacterPacket().uid);
        if (chat != null) {
            if (player.hasComponent(OverlayTextComponent.class)) {
                player.getComponent(OverlayTextComponent.class).setText(chat.message);
            } else {
                player.addComponent(new OverlayTextComponent(chat.message, 5));
            }
            messagesToAdd.remove(chat);
        }


        if (!entities.values().isEmpty()) {

            for (Network.GameEntity entity : entities.values()) {

                if (entity instanceof CharacterPacket) {
                    CharacterPacket packet = (CharacterPacket)entity;

                    // Update the other players
                    Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("entity", packet.uid);
                    if (optEnt.isPresent()) {
                        Entity playerEntity = optEnt.get();


                        //System.out.println(entity.getPosition());
                        Network.UserChat entityChat = getChatMsg(packet.uid);

                        //if (packet.uid == entity.getComponent(LocalPlayerComponent.class).getId()) {
                        // We found the dude we need to update

                        if (entityChat != null) {
                            if (playerEntity.hasComponent(OverlayTextComponent.class)) {
                                playerEntity.getComponent(OverlayTextComponent.class).setText(entityChat.message);
                            } else {
                                playerEntity.addComponent(new OverlayTextComponent(entityChat.message, 5));
                            }

                            messagesToAdd.remove(entityChat);

                        }


                        //if (entity.getX() == packet.x && entity.getY() == packet.y) {
                        //entity.getComponent(AnimatedMovementComponent.class).setState(Data.MovementState.STANDING);

                        //} else {

                        // this block occurs whenever we recieve an update
                        if (playerEntity.hasComponent(AnimatedMovementComponent.class)) {


                            playerEntity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);

                            double distanceX = Math.abs(packet.x - playerEntity.getX());
                            double distanceY = Math.abs(packet.y - playerEntity.getY());

                            if (distanceX < snappingDistance && distanceY < snappingDistance) {
//                            entity.setX(packet.x);
//                            entity.setY(packet.y);
                                playerEntity.getComponent(AnimatedMovementComponent.class).setState(Data.MovementState.STANDING);

                            } else {




                            }
                            double blend = 1f - Math.pow(1f - 0.2, dtf * 60); // we should be at 60fps
                            playerEntity.setPosition(interpolateCharacter(packet.x, packet.y, playerEntity.getPosition(), dtf));
                            //playerEntity.setX(packet.x);
                            //playerEntity.setY(packet.y);

                            // maybe i need the tick of when i recieved the information


//                                entity.getComponent(LocalPlayerComponent.class).getEntity().setX(packet.x);
//                                entity.getComponent(LocalPlayerComponent.class).getEntity().setY(packet.y);
                        }

                        //}


                        //}


                    }


                }

                if (entity instanceof Network.NPCPacket) {
                    Network.NPCPacket packet = (Network.NPCPacket)entity;


                    if (packet.behaviorType != BehaviorType.STATIC) {
                        Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("entity", packet.uid);
                        if (optEnt.isPresent()) {
                            Entity npcEntity = optEnt.get();
                            if ((int)npcEntity.getX() == (int)packet.x && (int)npcEntity.getX() == (int)packet.y) {
                                npcEntity.getComponent(AnimatedMovementComponent.class).setState(Data.MovementState.STANDING);
                                System.out.println("npc is standing still");
                            } else {

                                npcEntity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);


                                npcEntity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);

                                double distanceX = Math.abs(packet.x - npcEntity.getX());
                                double distanceY = Math.abs(packet.y - npcEntity.getY());

                                if (distanceX < snappingDistance && distanceY < snappingDistance) {
//                            entity.setX(packet.x);
//                            entity.setY(packet.y);
                                    npcEntity.getComponent(AnimatedMovementComponent.class).setState(Data.MovementState.STANDING);

                                } else {




                                }
                                double blend = 1f - Math.pow(1f - interpolationConstant, dtf * 60); // we should be at 60fps
                                npcEntity.setPosition(interpolateCharacter(packet.x, packet.y, npcEntity.getPosition(), dtf));
                            }

                        }
                    }

                }


                if (entity instanceof GameObject) {
                    GameObject object = (GameObject) entity;
                    if (object.isProjectile()) {
                        Optional<Entity> optEnt = FXGL.getApp().getGameWorld().getEntityByID("entity", object.getUniqueGameId());
                        if (optEnt.isPresent()) {
                            Entity objectEntity = optEnt.get();

                            if (objectEntity.hasComponent(ProjectileComponent.class)) {
                                ProjectileComponent component = objectEntity.getComponent(ProjectileComponent.class);
                                objectEntity.setPosition(FXGLMath.lerp(component.getProjectilePosition().getX(), component.getProjectilePosition().getY(), object.getX(), object.getY(), 0.01));
                                //entity.setX(object.getX());
                                //entity.setY(object.getY());
                            }
                        }
                    }
                }


            }



        }


        messagesToAdd.clear();
//
//        GameState current = new GameState();
//        current.setPlayers(playersHere);
//        current.setNpcs(npcsHere);
//
//        gameStates.put(tick, current);
//
//
//        for (Long state : gameStates.keySet()) {
//            if (state.longValue() < tick-100) { // delete all the old gamestates
//                gameStates.remove(state);
//            }
//        }

    }



}

