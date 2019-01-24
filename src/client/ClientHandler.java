package client;

import com.almasb.fxgl.entity.Entity;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.event.EventHandler;
import shared.CharacterPacket;
import shared.Network;
import shared.Network.*;

import java.io.IOException;
import java.util.HashSet;

/*
This class needs to handle all incoming and outcoming data from the server, and assign different listeners to
different types of packets.
 */


public class ClientHandler {
    Client client;

    private String host = "localhost";

    public static boolean LOGIN_STATUS = false;

    private HashSet<CharacterPacket> otherPlayers = new HashSet<>();

    private Screen screen;

    private int id; // This is the id the server assigned to us

    public ClientHandler(Screen screen) {
        client = new Client();
        client.start();

        this.screen = screen;

        Network.register(client);

        // ThreadedListener runs the listener methods on a different thread.

        client.addListener(new Listener.ThreadedListener(new LoginResponseListener(this)));
        client.addListener(new Listener.ThreadedListener(new CharacterResponseListener(this)));

        client.addListener(new Listener.ThreadedListener(new Listener() {
            public void connected(Connection connection) {
                System.out.println("Connected");
            }


            public void disconnected (Connection connection) {
                System.exit(0);
            }
        }));


    }

    public void updatePlayerLocal(int x, int y, int id) {
        for (Entity entity : screen.getGameWorld().getEntitiesByComponent(NetworkedComponent.class)) {
            if (id == entity.getComponent(NetworkedComponent.class).getId()) {
                // We found the dude we need to update
                entity.getComponent(NetworkedComponent.class).getEntity().setX(x);
                entity.getComponent(NetworkedComponent.class).getEntity().setY(y);
                System.out.println("Updated char " + id);
            }
        }
    }

    protected void removePlayerLocal(int id) {
        for (CharacterPacket packet : otherPlayers) {
            if (packet.id == id) {
                otherPlayers.remove(packet);
            }
        }
    }


    public void connectServer() {
        try {
            client.connect(5000, host, Network.port);
            // Server communication after connection can go here, or in Listener#connected().
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void login(String username, String password) {
        Login login = new Login();
        login.name = username;
        login.pass = password;
        client.sendTCP(login);
    }

    public void sendMovement(int x, int y, int id) {
        Network.UpdateCharacter update = new Network.UpdateCharacter();
        update.x = x;
        update.y = y;
        update.id = id;
        System.out.println("id " + id);
        client.sendTCP(update); // I'd like movement to be udp
    }

    public HashSet<CharacterPacket> getOtherPlayers() {
        return otherPlayers;
    }

    public void onLoggedIn(int id) {
        this.id = id;
        screen.initGamee();
    }

    protected void quit(int id) {
        Network.RemoveCharacter msg = new Network.RemoveCharacter();
        msg.id = id;
        client.sendTCP(msg);
    }

    protected Screen getScreen() {
        return screen;
    }


    public int getId() {
        return id;
    }
}
