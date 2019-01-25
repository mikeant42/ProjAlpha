package client;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.Network;


// this class is responsible for launching the client into the game world
public class LoginResponseListener extends Listener {

    private ClientHandler handler;

    public LoginResponseListener(ClientHandler h) {
        handler = h;
    }

    public void received (Connection connection, Object object) {
        if (object instanceof Network.LoginSuccess) {
            Network.LoginSuccess s = (Network.LoginSuccess)object;
            ClientHandler.LOGIN_STATUS = s.success;
            if (s.success == true) {
                handler.onLoggedIn(s.id);
                System.out.println("Login success");
            }
        }
    }

}
