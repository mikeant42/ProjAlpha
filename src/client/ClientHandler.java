package client;

import client.listener.CharacterResponseListener;
import client.listener.LoginResponseListener;
import client.listener.NPCResponseListener;
import client.listener.WorldResponseListener;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
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
    private List<NPCPacket> npcs = new ArrayList<>();


    private Screen screen;

    private int id; // This is the id the server assigned to us

    private LoginResponseListener loginResponseListener;
    private CharacterResponseListener characterResponseListener;
    private WorldResponseListener worldResponseListener;
    private NPCResponseListener npcResponseListener;

    public ClientHandler(Screen screen) {
        client = new Client();
        client.start();

        loginResponseListener = new LoginResponseListener(this);
        characterResponseListener = new CharacterResponseListener(this);
        worldResponseListener = new WorldResponseListener(this);
        npcResponseListener = new NPCResponseListener(this);

        this.screen = screen;

        Network.register(client);

        // ThreadedListener runs the listener methods on a different thread.

        client.addListener(loginResponseListener);
        client.addListener(new Listener.ThreadedListener(npcResponseListener));

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

    public void updatePlayerLocal(Data.Input input, double x, double y, int idd) {
        for (CharacterPacket packet : otherPlayers) {
            if (idd == packet.id && idd != id) {
                packet.x = x;
                packet.y = y;
                packet.input = input;
            }
        }

    }

    public void updateNPC(float x, float y, int id) {
        for (NPCPacket packet : npcs) {
            if (id == packet.id) {
                packet.x = x;
                packet.y = y;
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

    public void setMap(int id) {
        screen.setMap(id);
    }

    public boolean hasNPC(int id) {
        for (NPCPacket packet : npcs) {
            if (packet.id == id) {
                return true;
            }
        }
        return false;
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

    public HashSet<CharacterPacket> getOtherPlayers() {
        return otherPlayers;
    }

    public boolean isPlayerHere(int id) {
        for (CharacterPacket packet : otherPlayers) {
            if (packet.id  == id) {
                System.out.println("Player " + id + " is here");
                return true;
            }
        }

        return false;
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

    public Screen getScreen() {
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

    public List<NPCPacket> getNpcs() {
        return npcs;
    }
}
