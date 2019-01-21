package client;

import client.menu.LoginController;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.ui.UI;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Screen extends GameApplication {

    private ClientHandler clientHandler;
    private UI loginScreen;

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
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                getGameScene().removeUI(loginScreen);

            }

        });

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
