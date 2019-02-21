package client;

import client.listener.CharacterResponseListener;
import client.listener.LoginResponseListener;
import client.listener.NPCResponseListener;
import client.listener.WorldResponseListener;
import com.almasb.fxgl.entity.SpawnData;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Platform;
import shared.CharacterPacket;
import shared.Data;
import shared.GameObject;
import shared.Network;
import shared.Network.*;

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
    private List<GameObject> objects = new ArrayList<>();


    private Screen screen;

    private CharacterPacket characterPacket;

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

    public void updatePlayerLocal(int move, double x, double y, int idd) {
        for (CharacterPacket packet : otherPlayers) {
            if (idd == packet.id && idd != characterPacket.id) {
                packet.x = x;
                packet.y = y;
                packet.moveState = move;
            }
        }

    }

    public void updateNPC(float x, float y, int move, int id) {
        for (NPCPacket packet : npcs) {
            if (id == packet.id) {
                packet.x = x;
                packet.y = y;
                packet.moveState = move;
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

        //this.username = username;
    }

    public void requestMap() {
        Network.WorldQuery query = new Network.WorldQuery();
        query.cid = characterPacket.id;
        client.sendTCP(query);
    }

    public void sendMovement(int move, double x, double y, int id) {
        Network.UpdateCharacter update = new Network.UpdateCharacter();
        update.moveState = move;
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

    public void onLoggedIn() {
        //this.id = id;
        screen.startGame();
        //client.removeListener(loginResponseListener);
    }

    public void quit(int id) {
        Network.RemoveCharacter msg = new Network.RemoveCharacter();
        msg.id = id;
        client.sendTCP(msg);
    }

    public void addGameObject(GameObject obj) {
        objects.add(obj);
    }

    public void removeGameObject(int uid) {
        List<GameObject> remove = new ArrayList<>();
        for (GameObject object : objects) {
            if (object.getUniqueGameId() == uid) {
                remove.add(object);
            }
        }
        objects.removeAll(remove);
        screen.removeNetworkedEntity(uid);
    }

    public CharacterPacket getCharacterPacket() {
        return characterPacket;
    }

    public void setCharacterPacket(CharacterPacket characterPacket) {
        this.characterPacket = characterPacket;
    }

    public void addInventory(GameObject object) {

    }

    public Screen getScreen() {
        return screen;
    }


    public int getId() {
        return characterPacket.id;
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

    public String getUsername() {
        return characterPacket.name;
    }

    public List<GameObject> getObjects() {
        return objects;
    }
}
