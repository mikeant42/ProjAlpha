package client;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import server.LoginListener;
import shared.CharacterPacket;
import shared.Data;
import shared.Network;
import shared.Network.*;
import shared.Data.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/*
This class needs to handle all incoming and outcoming data from the server, and assign different listeners to
different types of packets.
 */


public class ClientHandler {
    private Client client;

    private String host = "localhost";

    public static boolean LOGIN_STATUS = false;

    private HashSet<CharacterPacket> otherPlayers = new HashSet<>();

    private List<Network.UpdateCharacter2> updatePlayerList = new ArrayList<>();

    private Screen screen;

    private int id; // This is the id the server assigned to us

    private LoginResponseListener loginResponseListener;
    private CharacterResponseListener characterResponseListener;
    private WorldResponseListener worldResponseListener;

    public ClientHandler(Screen screen) {
        client = new Client();
        client.start();

        loginResponseListener = new LoginResponseListener(this);
        characterResponseListener = new CharacterResponseListener(this);
        worldResponseListener = new WorldResponseListener(this);

        this.screen = screen;

        Network.register(client);

        // ThreadedListener runs the listener methods on a different thread.

        client.addListener(new Listener.ThreadedListener(loginResponseListener));
        addMainListeners();

        client.addListener(new Listener.ThreadedListener(new Listener() {
            public void connected(Connection connection) {
                System.out.println("Connected");
            }


            public void disconnected (Connection connection) {
                System.exit(0);
            }
        }));


    }

    public void updatePlayerLocal(Data.Input input, double x, double y, int id) {
        List<Entity> ents = screen.getGameWorld().getEntitiesByComponent(NetworkedComponent.class);
        for (Entity entity : ents) {
            if (id == entity.getComponent(NetworkedComponent.class).getId()) {
                // We found the dude we need to update

                entity.getComponent(AnimatedMovementComponent.class).setInput(input);
//                if (input.UP) {
//                    entity.getComponent(AnimatedMovementComponent.class).animUp();
//                } else if (input.DOWN) {
//                    entity.getComponent(AnimatedMovementComponent.class).animDown();
//                } else if (input.LEFT) {
//                    System.out.println("legt");
//                    entity.getComponent(AnimatedMovementComponent.class).animLeft();
//                } else if (input.RIGHT) {
//                    entity.getComponent(AnimatedMovementComponent.class).animRight();
                //}
                entity.getComponent(NetworkedComponent.class).getEntity().setX(x);
                entity.getComponent(NetworkedComponent.class).getEntity().setY(y);

//                entity.getComponent(PhysicsComponent.class).setVelocityX(velX);
//                entity.getComponent(PhysicsComponent.class).setVelocityY(velY);

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

    public List<UpdateCharacter2> getUpdatePlayerList() {
        return updatePlayerList;
    }

    public void setMap(int id) {
        screen.setMap(id);
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

    public void requestMap() {
        Network.WorldQuery query = new Network.WorldQuery();
        query.cid = id;
        client.sendTCP(query);
    }

    public void sendMovement(Input input, double x, double y, int id) {
        Network.UpdateCharacter update = new Network.UpdateCharacter();
        update.input = input;
        update.id = id;
        update.x = x;
        update.y = y;
        //System.out.println("id " + id);
        client.sendTCP(update); // I'd like movement to be udp
    }

    public void sendClientMoveUpdate(double posX, double posY, double velX, double velY, Data.Input input) {
        Network.UpdateCharacter2 updateCharacter2 = new Network.UpdateCharacter2();
        updateCharacter2.id = id;
        updateCharacter2.velX = velX;
        updateCharacter2.velY = velY;
        updateCharacter2.input = input;
        client.sendTCP(updateCharacter2);

    }

    public HashSet<CharacterPacket> getOtherPlayers() {
        return otherPlayers;
    }

    public void onLoggedIn(int id) {
        this.id = id;
        screen.initGamee();
        //client.removeListener(loginResponseListener);
    }

    public void quit(int id) {
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

    public Client getClient() {
        return client;
    }

    // This gets called when the player is logged, and we know he's ready to recieve it.
    private void addMainListeners() {
        client.addListener(characterResponseListener);
        client.addListener(worldResponseListener);
    }
}
