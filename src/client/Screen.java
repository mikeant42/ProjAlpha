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
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import shared.CharacterPacket;

import java.util.ArrayList;
import java.util.List;


public class Screen extends GameApplication {

    private ClientHandler clientHandler;
    private UI loginScreen;
    private LoginController loginController;
    private List<CharacterPacket> playersHere = new ArrayList<>();
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
                    player.getComponent(AnimatedMovementComponent.class).setMove(INVALID_MOVE.DOWN);
                } else if (!(hut.getBoundingBoxComponent().getMaxYWorld() > playerCollision.getBoundingBoxComponent().getMaxYWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).setMove(INVALID_MOVE.UP);
                } else if (!(hut.getBoundingBoxComponent().getMaxXWorld() > playerCollision.getBoundingBoxComponent().getMaxXWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).setMove(INVALID_MOVE.LEFT);
                } else if (!(hut.getBoundingBoxComponent().getMinXWorld() < playerCollision.getBoundingBoxComponent().getMinXWorld())) {
                    player.getComponent(AnimatedMovementComponent.class).setMove(INVALID_MOVE.RIGHT);
                } else {
                    player.getComponent(AnimatedMovementComponent.class).setMove(INVALID_MOVE.NONE);
                }
            }


            @Override
            public void onCollisionEnd(Entity player, Entity hut) {
                player.getComponent(AnimatedMovementComponent.class).setMove(INVALID_MOVE.NONE);
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


    @Override
    protected void onUpdate(double dtf) {
        if (loggedIn && hasMap) {
            // 1. Have the server send us the world.
            // 2. Draw and update all visible players
            //    - All of the networked npcs/players should update within their own loop


            if (!clientHandler.getOtherPlayers().isEmpty()) {
                for (CharacterPacket packet : clientHandler.getOtherPlayers()) {
                    if (!playersHere.contains(packet)) {
                        SpawnData data = new SpawnData(packet.x, packet.y);
                        data.put("ID", packet.id);
                        getGameWorld().spawn("player", data);
                        playersHere.add(packet);
                    }


                }

            }


        }


    }


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1024);
        settings.setHeight(860);
        settings.setTitle("Basic Game App");
    }

    public static void main(String[] aargs) {
        launch(aargs);

    }
}
