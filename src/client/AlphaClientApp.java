package client;

import client.render.ProjectileComponent;
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
import com.almasb.fxgl.ui.Position;
import com.almasb.fxgl.ui.ProgressBar;
import com.almasb.fxgl.ui.UI;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import shared.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class AlphaClientApp extends GameApplication {

    private ClientHandler clientHandler;

    private UI loginScreen;
    private UI mainPanel;

    private ProgressBar hpBar;

    private LoginController loginController;

//    private List<CharacterPacket> playersHere = new ArrayList<>();
//    private List<Network.NPCPacket> npcsHere = new ArrayList<>();
//    private List<GameObject> objectsHere = new ArrayList<>();
//
//
//    private List<Network.UserChat> messagesToAdd = new ArrayList<>();

    private ClientGameMap gameMap;

    private boolean loggedIn = false;

//    private Entity player;

    private boolean hasMap = false;


    private boolean panelOpen = false;

    private MainPanelController panelControl;

    public AlphaClientApp() {
        clientHandler = new ClientHandler(this);
        clientHandler.connectServer();

        gameMap = new ClientGameMap();


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
        getGameWorld().addEntityFactory(new BaseFactory(clientHandler));
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

                clientHandler.requestMap();

                mainPanel.getRoot().toFront();

                panelControl.create(clientHandler);

                gameMap.init(clientHandler);

                int healthWidth = 200;

                hpBar = new ProgressBar();
                hpBar.setMinValue(0);
                hpBar.setMaxValue(Data.PlayerConstants.MAX_HEALTH);
                hpBar.setCurrentValue(clientHandler.getCharacterPacket().combat.getHealth());
                hpBar.setWidth(healthWidth);
                hpBar.setLabelVisible(true);
                hpBar.setLabelPosition(Position.BOTTOM);
                hpBar.setFill(Color.GREEN);

                Pane pane = new Pane();
                pane.setTranslateX(FXGL.getAppWidth()-healthWidth);
                pane.getChildren().add(hpBar);
                getGameScene().addUINodes(pane);





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
                if (gameMap.getPlayer().hasComponent(AnimatedMovementComponent.class))
                    gameMap.getPlayer().getComponent(AnimatedMovementComponent.class).up();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down 1") {
            @Override
            protected void onAction() {
                if (gameMap.getPlayer().hasComponent(AnimatedMovementComponent.class))
                    gameMap.getPlayer().getComponent(AnimatedMovementComponent.class).down();
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Right 1") {
            @Override
            protected void onAction() {
                if (gameMap.getPlayer().hasComponent(AnimatedMovementComponent.class))
                    gameMap.getPlayer().getComponent(AnimatedMovementComponent.class).right();
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Left 1") {
            @Override
            protected void onAction() {
                if (gameMap.getPlayer().hasComponent(AnimatedMovementComponent.class))
                    gameMap.getPlayer().getComponent(AnimatedMovementComponent.class).left();
            }
        }, KeyCode.A);




        UserAction click = new UserAction("Click") {
            @Override
            protected void onActionBegin() {
                if (gameMap.isMapLoaded()) {
                    Input input = getInput();
                    SpawnData data = new SpawnData(gameMap.getPlayer().getX(), gameMap.getPlayer().getY());
                    data.put("mouseX", input.getMouseXWorld());
                    data.put("mouseY", input.getMouseYWorld());
                    System.out.println(input.getMouseXWorld( ));
                    System.out.println(input.getMouseYWorld());
                    data.put("name", Names.Spell.TORNADO);
                   // getGameWorld().spawn("projectile", data);
                    clientHandler.sendProjectile(gameMap.getPlayer().getX(), gameMap.getPlayer().getY(), input.getMouseXWorld(), input.getMouseYWorld());

                   // gameMap.getPlayer().addComponent(new ProjectileComponent(input.getMouseXWorld(), input.getMouseYWorld(), 5));
                }
            }
        };

        getInput().addAction(click, MouseButton.PRIMARY);

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
        //getPhysicsWorld().addCollisionHandler(AlphaCollision.setProjectileCollision(EntityType.PLAYER, EntityType.PROJECTILE));
       //dw getPhysicsWorld().addCollisionHandler(AlphaCollision.setProjectileCollisionNPC(EntityType.NPC, EntityType.PROJECTILE));
    }


    public ClientGameMap getActiveWorld() {
        return gameMap;
    }

//
    public void addChatMsg(Network.UserChat chat) {
        gameMap.addChatMsg(chat);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                panelControl.addChat(chat.message);
            }
        });
    }


    @Override
    protected void onUpdate(double dtf) {

        if (gameMap.isMapLoaded()) {
            hpBar.setCurrentValue(clientHandler.getCharacterPacket().combat.getHealth());

            gameMap.update();

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
