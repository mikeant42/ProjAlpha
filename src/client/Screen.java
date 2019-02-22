package client;

import client.ui.LoginController;
import client.ui.MainPanelController;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.listener.ExitListener;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.*;
import com.almasb.fxgl.parser.tiled.*;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.ui.UI;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import shared.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Screen extends GameApplication {

    private ClientHandler clientHandler;

    private UI loginScreen;
    private UI mainPanel;

    private LoginController loginController;
    private List<CharacterPacket> playersHere = new ArrayList<>();
    private List<Network.NPCPacket> npcsHere = new ArrayList<>();
    private List<GameObject> objectsHere = new ArrayList<>();
    private boolean loggedIn = false;

    private Entity player;

    private boolean hasMap = false;

    public static int TILESIZE = 256;


    private boolean panelOpen = false;

    MainPanelController panelControl;

    public Screen() {
        clientHandler = new ClientHandler(this);
        clientHandler.connectServer();

    }

    @Override
    public void initUI() {
        loginController = new LoginController(clientHandler, getGameScene());
        loginScreen = getAssetLoader().loadUI("login.fxml", loginController);

         panelControl = new MainPanelController();
        mainPanel = getAssetLoader().loadUI("mainpanel.fxml", panelControl);

        getGameScene().addUI(loginScreen);


    }

    /*
    In this method we init the world game for the player.
     */
    public void startGame() {
        this.addExitListener(new ExitListener() {
            @Override
            public void onExit() {
                System.out.println("Bye!");
                clientHandler.quit(clientHandler.getId());
                clientHandler.getClient().close();
                System.exit(0);
            }
        });

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                // Make sure we disconnect at the end of the game
                iniInput();
                loggedIn = true;
                ClientHandler.LOGIN_STATUS = true;

                getGameScene().removeUI(loginScreen);
                getGameWorld().addEntityFactory(new BaseFactory(clientHandler));

                clientHandler.requestMap();

                mainPanel.getRoot().toFront();

                panelControl.create(clientHandler);




//                SpawnData data = new SpawnData(0,0);
//                data.put("ID", clientHandler.getId());
//                player = getGameWorld().spawn("player", data);



            }

        });


    }

    private void iniInput() {


    }

    @Override
    public void initInput() {

        getInput().addAction(new UserAction("Up 1") {
            @Override
            protected void onAction() {
                player.getComponent(AnimatedMovementComponent.class).up();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down 1") {
            @Override
            protected void onAction() {
                player.getComponent(AnimatedMovementComponent.class).down();
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Right 1") {
            @Override
            protected void onAction() {
                player.getComponent(AnimatedMovementComponent.class).right();
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Left 1") {
            @Override
            protected void onAction() {
                player.getComponent(AnimatedMovementComponent.class).left();
            }
        }, KeyCode.A);





        UserAction tab = new UserAction("Tab") {
            public void onActionBegin() {
                if (panelOpen) {
                    getGameScene().removeUI(mainPanel);
                    panelOpen = false;
                } else {
                    getGameScene().addUI(mainPanel);
                    panelOpen = true;
                }
            }
        };
        getInput().addAction(tab, KeyCode.TAB);
    }



    /**
     * This physics handler only handles local data, like handling map collisions such as trees, water, etc
     * The server will handle the more sensisitive collisions
     */
    @Override
    protected void initPhysics() {
        //getPhysicsWorld().setGravity(1,1);
        getPhysicsWorld().addCollisionHandler(AlphaCollision.setClientCollision(EntityType.LOCAL_PLAYER, EntityType.COLLIDE));
    }


    public void setMap(int id) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (id == 1) {
                    try {
                        parseWorld("src/assets/json/ult.xml");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }

                hasMap = true;


                SpawnData data = new SpawnData(50, 50);
                data.put("ID", clientHandler.getId());
                player = getGameWorld().spawn("localplayer", data);

                getGameScene().getViewport().bindToEntity(player, FXGL.getAppWidth()/2 - TILESIZE/2, FXGL.getAppHeight()/2 - TILESIZE/2);
                getGameScene().getViewport().setZoom(1.2);

            }
        });
    }

    private void parseWorld(String file) throws FileNotFoundException{
        File initialFile = new File(file);
        InputStream targetStream = new FileInputStream(initialFile);


        TMXParser parser = new TMXParser();
        TiledMap map = parser.parse(targetStream);
//        map.getLayerByName("Treetop").setDraworder("topdown");

        getGameWorld().setLevelFromMap(map);
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

    private boolean isObjectHere(int uid) {
        for (GameObject object : objectsHere) {
            if (object.getUniqueGameId() == uid) {
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

            player.getComponent(NetworkedComponent.class).update();


            if (!clientHandler.getOtherPlayers().isEmpty()) {



                for (CharacterPacket packet : clientHandler.getOtherPlayers()) {
                    if (!isPlayerHere(packet.id) && packet.id != clientHandler.getId()) {
                        System.out.println("Adding player " + packet.id);
                        SpawnData data = new SpawnData(packet.x, packet.y);
                        data.put("ID", packet.id);
                        data.put("user", packet.name);
                        getGameWorld().spawn("player", data);
                        playersHere.add(packet);
                    }


                    // Update the other players
                    List<Entity> entities = getGameWorld().getEntitiesByType(EntityType.PLAYER);
                    for (Entity entity : entities) {

                        if (packet.id == entity.getComponent(NetworkedComponent.class).getId()) {
                            // We found the dude we need to update

                            entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);
                            int moveState = packet.moveState;

//                            // idea -- Move this into AnimMovemementComponent
//                            if (moveState == Data.MovementState.RUNNING_FORWARD) {
//                                entity.getComponent(AnimatedMovementComponent.class).up();
//                            } else if (moveState == Data.MovementState.RUNNING_BACK) {
//                                entity.getComponent(AnimatedMovementComponent.class).down();
//                            } else if (moveState == Data.MovementState.RUNNING_LEFT) {
//                                entity.getComponent(AnimatedMovementComponent.class).left();
//                            } else if (moveState == Data.MovementState.RUNNING_RIGHT) {
//                                entity.getComponent(AnimatedMovementComponent.class).right();
//                            }
//                           //     entity.getComponent(AnimatedMovementComponent.class).animIdle();
//                            }
                            entity.getComponent(NetworkedComponent.class).getEntity().setX(packet.x);
                            entity.getComponent(NetworkedComponent.class).getEntity().setY(packet.y);


                        }

//                entity.getComponent(PhysicsComponent.class).setVelocityX(velX);
//                entity.getComponent(PhysicsComponent.class).setVelocityY(velY);

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

            List<Entity> entities = getGameWorld().getEntitiesByType(EntityType.NPC);
            for (Entity entity : entities) {
                if (packet.id == entity.getInt("ID")) {
                    entity.getComponent(AnimatedMovementComponent.class).setState(packet.moveState);
                    entity.setX(packet.x);
                    entity.setY(packet.y);
                }
            }


        }

        for (GameObject object : clientHandler.getObjects()) {
            if (!isObjectHere(object.getUniqueGameId())) {
                SpawnData data = new SpawnData(object.getX(), object.getY());
                data.put("ID", object.getId());
                data.put("uid", object.getUniqueGameId());
                data.put("name", object.getName());

                getGameWorld().spawn("Gameobject", data);

                objectsHere.add(object);
            }
        }


        }
    }

    public MainPanelController getPanelControl() {
        return panelControl;
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(860);
        settings.setHeight(600);
        settings.setTitle("Alpha");
    }

    public static void main(String[] aargs) {
        launch(aargs);

    }
}
