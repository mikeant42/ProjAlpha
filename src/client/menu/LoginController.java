package client.menu;

import client.ClientHandler;
import com.almasb.fxgl.ui.UIController;
import com.esotericsoftware.kryonet.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LoginController implements UIController {
    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Label loginMsg;

    ClientHandler handler;


    public LoginController(ClientHandler handler){
        this.handler = handler;
    }

    @Override
    public void init() {

    }

    public void loginError(String text) {
        loginMsg.setText(text);
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
            Thread.sleep(100);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

        loginMsg.setText("Your login status: " + ClientHandler.LOGIN_STATUS);

    }
}
