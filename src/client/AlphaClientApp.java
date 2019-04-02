package client;

import client.render.AnimatedMovementComponent;
import client.ui.LoginController;
import client.ui.MainPanelController;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.*;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.ui.Position;
import com.almasb.fxgl.ui.ProgressBar;
import com.almasb.fxgl.ui.UI;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import shared.*;
import shared.collision.AlphaCollision;

import java.util.Map;


public class AlphaClientApp extends GameApplication {

    private ClientHandler clientHandler;

    private UI loginScreen;
    private UI mainPanel;

    private ProgressBar hpBar;
    private ProgressBar manaBar;

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

    private Entity mouseAttackLook;

    private Text playerStateText;

    public AlphaClientApp() {
        clientHandler = new ClientHandler(this);
        //clientHandler.connectServer();

        gameMap = new ClientGameMap();


    }

    @Override
    public void initUI() {
        loginController = new LoginController(clientHandler, getGameScene());
        loginScreen = getAssetLoader().loadUI("login.fxml", loginController);

         panelControl = new MainPanelController();
        mainPanel = getAssetLoader().loadUI("mainpanel.fxml", panelControl);


        mainPanel.getRoot().getStylesheets().add("assets/ui/modena_dark.css");

        getGameScene().addUI(loginScreen);


    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("playerState", Data.AlphaGameState.FIGHTING);
    }

    /*
    In this method we init the world game for the player.
     */
    public void startGame() {
        getGameWorld().addEntityFactory(new BaseFactory(clientHandler));
//        this.addExitListener(new ExitListener() {
//            @Override
//            public void onExit() {
//                System.out.println("Bye!");
//                clientHandler.quit(clientHandler.getId());
//                clientHandler.getClient().close();
//                System.exit(0);
//            }
//        });


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

                EventHandler<PlayerEvent> deathHandler = event -> {
                    // do something with event
                    System.out.println("event handler is working");
                    panelControl.dropAll();
                    gameMap.getPlayer().setPosition(clientHandler.getCharacterPacket().x, clientHandler.getCharacterPacket().y);
                };

                getEventBus().addEventHandler(PlayerEvent.DEATH, deathHandler);

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

                manaBar = new ProgressBar();
                manaBar.setMinValue(0);
                manaBar.setMaxValue(Data.PlayerConstants.MAX_HEALTH);
                manaBar.setCurrentValue(clientHandler.getCharacterPacket().combat.getMana());
                manaBar.setWidth(healthWidth);
                manaBar.setLabelVisible(true);
                manaBar.setLabelPosition(Position.BOTTOM);
                manaBar.setFill(Color.BLUE);

                Pane manaPane = new Pane();
                manaPane.setTranslateX(FXGL.getAppWidth()-healthWidth-healthWidth);
                manaPane.getChildren().add(manaBar);
                getGameScene().addUINodes(manaPane);


                playerStateText = new Text("PlayerState " + Data.AlphaGameState.stateToString(getGameState().getInt("playerState")));
                playerStateText.setFill(Color.DARKSLATEGREY);
                playerStateText.setStrokeWidth(2.2);


                getGameScene().addUINodes(playerStateText);
                playerStateText.setX(10);
                playerStateText.setY(10);




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




        UserAction changeState = new UserAction("Change State") {
            @Override
            protected void onActionBegin() {
                if (getGameState().getInt("playerState") == Data.AlphaGameState.FIGHTING) {
                    getGameState().setValue("playerState", Data.AlphaGameState.RESTORING);
                    playerStateText.setText("PlayerState " + Data.AlphaGameState.stateToString(getGameState().getInt("playerState")));
                    //playerStateText.setText("PlayerState " + Data.AlphaGameState.stateToString(getGameState().getInt("playerState")));
                } else  if (getGameState().getInt("playerState") == Data.AlphaGameState.RESTORING) {
                    getGameState().setValue("playerState", Data.AlphaGameState.FIGHTING);
                    playerStateText.setText("PlayerState " + Data.AlphaGameState.stateToString(getGameState().getInt("playerState")));
                    //playerStateText.setText("PlayerState " + Data.AlphaGameState.stateToString(getGameState().getInt("playerState")));
                }

            }
        };

        getInput().addAction(changeState, KeyCode.C);

        UserAction useShield = new UserAction("Set Shield") {
            @Override
            protected void onActionBegin() {
                if (gameMap.isMapLoaded()) {
                    if (clientHandler.getCharacterPacket().combat.getShield() == Data.Shield.NONE) {
                        clientHandler.getCharacterPacket().combat.setShield(Data.Shield.GRAVITY); // pull from player deck - active shield
                    } else {
                        clientHandler.getCharacterPacket().combat.setShield(Data.Shield.NONE);
                    }
                    clientHandler.sendCombatObjectUpdate();
                }
            }
        };

        getInput().addAction(useShield, KeyCode.X);


        UserAction click = new UserAction("Click") {
            @Override
            protected void onActionBegin() {
                if (gameMap.isMapLoaded() && getGameState().getInt("playerState") == Data.AlphaGameState.FIGHTING) {
                    Input input = getInput();
//                    SpawnData data = new SpawnData(gameMap.getPlayer().getX(), gameMap.getPlayer().getY());
//                    data.put("mouseX", input.getMouseXWorld());
//                    data.put("mouseY", input.getMouseYWorld());
//                    System.out.println(input.getMouseXWorld( ));
//                    System.out.println(input.getMouseYWorld());
//                    data.put("name", Names.Spell.TORNADO);
                   // getGameWorld().spawn("projectile", data);
                    clientHandler.sendProjectile(gameMap.getPlayer().getX(), gameMap.getPlayer().getY(), input.getMouseXWorld(), input.getMouseYWorld());

                   // gameMap.getPlayer().addComponent(new ProjectileComponent(input.getMouseXWorld(), input.getMouseYWorld(), 5));
                }
            }
        };

        getInput().addAction(click, MouseButton.PRIMARY);

        UserAction rightClick = new UserAction("Rightclick") {
            @Override
            protected void onActionBegin() {

            }
        };

        getInput().addAction(rightClick, MouseButton.SECONDARY);

        UserAction tab = new UserAction("Tab") {
            public void onActionBegin() {
                if (gameMap.isMapLoaded()) {
                    if (panelOpen) {
                        getGameScene().removeUI(mainPanel);
                        panelOpen = false;
                    } else {
                        getGameScene().addUI(mainPanel);
                        panelOpen = true;
                    }
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
        //getPhysicsWorld().addCollisionHandler(AlphaCollision.setDialogueCollision(EntityType.LOCAL_PLAYER, EntityType.INTERACTABLE_NPC));
        //getPhysicsWorld().addCollisionHandler(AlphaCollision.setImpactCollision(EntityType.ENEMY, EntityType.PROJECTILE));
        //getPhysicsWorld().addCollisionHandler(AlphaCollision.setImpactCollision(EntityType.LOCAL_PLAYER, EntityType.PROJECTILE));
        //getPhysicsWorld().addCollisionHandler(AlphaCollision.setProjectileCollisionNPC(EntityType.NPC, EntityType.PROJECTILE));
    }


    public ClientGameMap getActiveMap() {
        return gameMap;
    }

//
    public void addChatMsg(Network.UserChat chat) {

//        if (chat.cid == clientHandler.getId()) {
//            gameMap.addChatToWorld(chat);
//            username = clientHandler.getUsername();
//        } else {
//            username = gameMap.addChatToWorld(chat);
//        }
//
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String username = "";
                if (chat.cid == clientHandler.getId()) {
                    username = clientHandler.getUsername();
                } else {
                    try {
                        username = gameMap.getUsername(chat.cid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                gameMap.addChatToWorld(chat);
                panelControl.addChat(username + ": " + chat.message);
            }
        });
    }


    @Override
    protected void onUpdate(double dtf) {

        if (gameMap.isMapLoaded()) {
            hpBar.setCurrentValue(clientHandler.getCharacterPacket().combat.getHealth());
            manaBar.setCurrentValue(clientHandler.getCharacterPacket().combat.getMana());



            //System.out.println(getTick());
            gameMap.update(dtf);

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
        settings.setProfilingEnabled(true);
    }

    public static void main(String[] aargs) {
        launch(aargs);

    }
}
