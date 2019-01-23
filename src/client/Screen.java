package client;

import client.menu.LoginController;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.ui.UI;
import com.almasb.fxgl.util.Optional;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import shared.CharacterPacket;

import java.util.ArrayList;
import java.util.List;


public class Screen extends GameApplication {

    private ClientHandler clientHandler;
    private UI loginScreen;
    private List<CharacterPacket> playersHere = new ArrayList<>();
    private boolean loggedIn = false;

    private Entity player;

    private Stage stage;

    public Screen() {
        clientHandler = new ClientHandler(this);
        clientHandler.connectServer();

    }

    @Override
    public void initUI() {
        LoginController controller = new LoginController(clientHandler);

        loginScreen = getAssetLoader().loadUI("login.fxml", controller);

        getGameScene().addUI(loginScreen);



    }

    /*
    In this method we init the world game for the player.
     */
    public void initGamee() {
        loggedIn = true;
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                getGameScene().removeUI(loginScreen);
                getGameWorld().addEntityFactory(new BaseFactory());

                SpawnData data = new SpawnData(0,0);
                data.put("ID", 100);

                player = getGameWorld().spawn("player", data);


                // Make sure we disconnect at the end of the game
                stage = (Stage) getGameScene().getRoot().getScene().getWindow();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent t) {
                        System.out.println("Bye!");
                        clientHandler.quit(player.getComponent(NetworkedComponent.class).getId());
                    }
                });

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
        getInput().addAction(new UserAction("Up 1") {
            @Override
            protected void onAction() {
                player.getComponent(MovementComponent.class).up();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down 1") {
            @Override
            protected void onAction() {
                player.getComponent(MovementComponent.class).down();
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Right 1") {
            @Override
            protected void onAction() {
                player.getComponent(MovementComponent.class).right();
            }
        }, KeyCode.D);

        getInput().addAction(new UserAction("Left 1") {
            @Override
            protected void onAction() {
                player.getComponent(MovementComponent.class).left();
            }
        }, KeyCode.A);
    }


    @Override
    protected void onUpdate(double dtf) {
        if (loggedIn) {
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
