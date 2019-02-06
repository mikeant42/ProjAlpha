package client;

import client.menu.LoginController;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.ui.UI;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import shared.CharacterPacket;
import shared.Data;
import shared.EntityType;
import shared.Network;

import java.util.ArrayList;
import java.util.List;


public class Screen extends GameApplication {

    private ClientHandler clientHandler;
    private UI loginScreen;
    private LoginController loginController;
    private List<CharacterPacket> playersHere = new ArrayList<>();
    private List<Network.NPCPacket> npcsHere = new ArrayList<>();
    private boolean loggedIn = false;

    private Entity player;

    private boolean hasMap = false;

    public Screen() {
        clientHandler = new ClientHandler(this);
        clientHandler.connectServer();

    }

    @Override
    public void initUI() {
        loginController = new LoginController(clientHandler, getGameScene());

        loginScreen = getAssetLoader().loadUI("login.fxml", loginController);
//        loginController.getStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent t) {
//                System.out.println("Bye!");
//                clientHandler.quit(clientHandler.getId());
//                clientHandler.getClient().close();
//                System.exit(0);
//            }
//        });

        getGameScene().addUI(loginScreen);


    }

    /*
    In this method we init the world game for the player.
     */
    public void initGamee() {
        loggedIn = true;
        ClientHandler.LOGIN_STATUS = true;

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                // Make sure we disconnect at the end of the game
                iniInput();

                getGameScene().removeUI(loginScreen);
                getGameWorld().addEntityFactory(new BaseFactory(clientHandler));
                clientHandler.requestMap();




//                SpawnData data = new SpawnData(0,0);
//                data.put("ID", clientHandler.getId());
//                player = getGameWorld().spawn("player", data);



            }

        });


    }

    private void iniInput() {
        getInput().addAction(new UserAction("Up 1") {
            @Override
            protected void onAction() {
                player.getComponent(AnimatedMovementComponent.class).up();
                player.getComponent(AnimatedMovementComponent.class).animUp();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down 1") {
            @Override
            protected void onAction() {
                player.getComponent(AnimatedMovementComponent.class).down();
                player.getComponent(AnimatedMovementComponent.class).animDown();
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Right 1") {
            @Override
            protected void onAction() {
                player.getComponent(AnimatedMovementComponent.class).right();
                player.getComponent(AnimatedMovementComponent.class).animRight();
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Left 1") {
            @Override
            protected void onAction() {
                player.getComponent(AnimatedMovementComponent.class).left();
                player.getComponent(AnimatedMovementComponent.class).animLeft();
            }
        }, KeyCode.A);
    }

    /**
     * This physics handler only handles local data, like handling map collisions such as trees, water, etc
     * The server will handle the more sensisitive collisions
     */
    @Override
    protected void initPhysics() {
        //getPhysicsWorld().setGravity(1,1);
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.LOCAL_PLAYER, EntityType.HUT) {

            /*
            This is sort of a home cooked collision system. There are issues with corners of aabbs, but since it's used for non essential map objects it should be fine
            for now. It definetely cannot stay this bugged but i need to move on. The bigger the hitbox the less noticible it is
            This collision system is only meant for map entities that are static on all clients.
             */
            @Override
            protected void onCollision(Entity playerCollision, Entity hut) {
                if (!(hut.getBoundingBoxComponent().getMinYWorld() < playerCollision.getBoundingBoxComponent().getMinYWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).addMove(INVALID_MOVE.DOWN);
                }
                if (!(hut.getBoundingBoxComponent().getMaxYWorld() > playerCollision.getBoundingBoxComponent().getMaxYWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).addMove(INVALID_MOVE.UP);
                }
                if (!(hut.getBoundingBoxComponent().getMaxXWorld() > playerCollision.getBoundingBoxComponent().getMaxXWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).addMove(INVALID_MOVE.LEFT);
                }
                if (!(hut.getBoundingBoxComponent().getMinXWorld() < playerCollision.getBoundingBoxComponent().getMinXWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).addMove(INVALID_MOVE.RIGHT);
                }
            }


            @Override
            public void onCollisionEnd(Entity player, Entity hut) {
                player.getComponent(AnimatedMovementComponent.class).resetMoves();
            }

        });

    }


    public void setMap(int id) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (id == 1) {
                    getGameWorld().setLevelFromMap("ult.json");

                }

                hasMap = true;


                SpawnData data = new SpawnData(50, 50);
                data.put("ID", clientHandler.getId());
                player = getGameWorld().spawn("localplayer", data);

                getGameScene().getViewport().bindToEntity(player, 350, 350);;
                getGameScene().getViewport().setZoom(1);

            }
        });
    }


    public void removeNetworkedEntity(int id) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                List<Entity> charr = getGameWorld().getEntitiesByComponent(NetworkedComponent.class);
                for (Entity c : charr) {
                    if (c.getComponent(NetworkedComponent.class).getId() == id) {
                        c.removeFromWorld();
                    }
                }
            }
        });

    }


    @Override
    protected void initInput() {

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
            if (packet.id == id) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onUpdate(double dtf) {
        if (loggedIn && hasMap) {
            // 1. Have the server send us the world.
            // 2. Draw and update all visible players
            //    - All of the networked npcs/players should update within their own loop


            if (!clientHandler.getOtherPlayers().isEmpty()) {
                for (CharacterPacket packet : clientHandler.getOtherPlayers()) {
                    if (!isPlayerHere(packet.id) && packet.id != clientHandler.getId()) {
                        System.out.println("Adding player " + packet.id);
                        SpawnData data = new SpawnData(packet.x, packet.y);
                        data.put("ID", packet.id);
                        getGameWorld().spawn("player", data);
                        playersHere.add(packet);
                    }


                    // Update the other players
                    List<Entity> entities = getGameWorld().getEntitiesByType(EntityType.PLAYER);
                    for (Entity entity : entities) {
                        if (packet.id == entity.getComponent(NetworkedComponent.class).getId()) {
                            // We found the dude we need to update

                            //entity.getComponent(AnimatedMovementComponent.class).setInput(input);
                            Data.Input input = packet.input;
                            if (input != null) {
                                if (input.UP) {
                                    entity.getComponent(AnimatedMovementComponent.class).animUp();
                                } else if (input.DOWN) {
                                    entity.getComponent(AnimatedMovementComponent.class).animDown();
                                } else if (input.LEFT) {
                                    entity.getComponent(AnimatedMovementComponent.class).animLeft();
                                } else if (input.RIGHT) {
                                    entity.getComponent(AnimatedMovementComponent.class).animRight();
                                }
                                entity.getComponent(NetworkedComponent.class).getEntity().setX(packet.x);
                                entity.getComponent(NetworkedComponent.class).getEntity().setY(packet.y);
                            }

//                entity.getComponent(PhysicsComponent.class).setVelocityX(velX);
//                entity.getComponent(PhysicsComponent.class).setVelocityY(velY);

                        }


                    }


                }


            }


            for (Network.NPCPacket packet : clientHandler.getNpcs()) {
                if (!isNPCHere(packet.id)) {
                    System.out.println("Spawning npc " + packet.id);
                    SpawnData data = new SpawnData(packet.x, packet.y);
                    data.put("ID", packet.id);
                    getGameWorld().spawn("Roaming NPC", data);
                    npcsHere.add(packet);
                }

                List<Entity> entities = getGameWorld().getEntitiesByType(EntityType.ROAMING_NPC);
                for (Entity entity : entities) {
                    if (packet.id == entity.getInt("ID")) {
                        entity.setX(packet.x);
                        entity.setY(packet.y);
                    }
                }


            }


        }
    }


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(860);
        settings.setHeight(600);
        settings.setTitle("Basic Game App");
    }

    public static void main(String[] aargs) {
        launch(aargs);

    }
}
