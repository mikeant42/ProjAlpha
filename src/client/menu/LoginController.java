package client.menu;

import client.ClientHandler;
import client.NetworkedComponent;
import com.almasb.fxgl.scene.GameScene;
import com.almasb.fxgl.ui.UIController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements UIController {
    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label loginMsg;

    private ClientHandler handler;
    private GameScene gameScene;
    private Stage stage;

    public LoginController(ClientHandler handler, GameScene world){
        this.handler = handler;
        this.gameScene = world;

    }

    @Override
    public void init() {

    }

    //public void loginError(String text) {
    //    loginMsg.setText(text);
    //}

    public Stage getStage() {
        return stage;
    }

    @FXML
    public void Login(ActionEvent event) {
        if (username.getText().equals("") || password.getText().equals("")) {
            loginMsg.setText("You have to fill everything out");
        } else {
            handler.login(username.getText(), password.getText());
        }

        /**
         * This is truly not a good way to do this. This is only a workaround for finding a better method
         * Need to wait for the variable to update
         */
        try
        {
            Thread.sleep(500);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

        loginMsg.setText("Your login status: " + ClientHandler.LOGIN_STATUS);

    }
}
