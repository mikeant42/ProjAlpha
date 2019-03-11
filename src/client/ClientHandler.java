package client;

import client.listener.CharacterResponseListener;
import client.listener.LoginResponseListener;
import client.listener.NPCResponseListener;
import client.listener.WorldResponseListener;
import client.render.CombatComponent;
import client.ui.MainPanelController;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.*;
import shared.Network.*;
import sun.nio.ch.Net;

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


    private AlphaClientApp alphaClientApp;

    // TODO - FIX THIS
    private int latestWorldID = 1;

    private CharacterPacket characterPacket;

    private LoginResponseListener loginResponseListener;
    private CharacterResponseListener characterResponseListener;
    private WorldResponseListener worldResponseListener;
    private NPCResponseListener npcResponseListener;


    public ClientHandler(AlphaClientApp alphaClientApp) {
        client = new Client();
        client.start();

        loginResponseListener = new LoginResponseListener(this);
        characterResponseListener = new CharacterResponseListener(this);
        worldResponseListener = new WorldResponseListener(this);
        npcResponseListener = new NPCResponseListener(this);

        this.alphaClientApp = alphaClientApp;

        Network.register(client);

        // ThreadedListener runs the listener methods on a different thread.

        client.addListener(loginResponseListener);
        client.addListener(npcResponseListener);

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

    static { System.out.println("static init");}


    public void updatePlayerLocal(int move, double x, double y, int idd) {
        if (getId() == idd) {
            alphaClientApp.getActiveWorld().getPlayer().setPosition(x,y);
            characterPacket.moveState = move;
        }
        for (CharacterPacket packet : otherPlayers) {
            if (idd == packet.uid && idd != characterPacket.uid) {
                packet.x = x;
                packet.y = y;
                packet.moveState = move;
            }
        }

    }

    public void updateOurPlayerInventory(Inventory inventory) {
        characterPacket.inventory = inventory;
    }

    public void updateNPC(double x, double y, int move, int id) {
        for (NPCPacket packet : npcs) {
            if (id == packet.uid) {
                packet.x = x;
                packet.y = y;
                packet.moveState = move;
            }
        }
    }

    public void sendReady(boolean ready) {
        Network.ReadyToRecieve readyToRecieve = new Network.ReadyToRecieve();
        readyToRecieve.cid = getId();
        readyToRecieve.ready = ready;
        client.sendTCP(readyToRecieve);
    }

    protected void removePlayerLocal(int id) {
        for (CharacterPacket packet : otherPlayers) {
            if (packet.uid == id) {
                otherPlayers.remove(packet);
            }
        }
    }

//    public void setMap(int uid) {
//        alphaClientApp.setMap(uid);
//    }

    public boolean hasNPC(int id) {
        for (NPCPacket packet : npcs) {
            if (packet.uid == id) {
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
        query.cid = getCharacterPacket().uid;
        client.sendTCP(query);
    }

    public void sendMovement(int move, double x, double y, int id) {
        Network.UpdateCharacter update = new Network.UpdateCharacter();
        update.moveState = move;
        update.id = id;
        update.x = x;
        update.y = y;
        //System.out.println("uid " + uid);
        client.sendTCP(update); // I'd like movement to be udp
    }

    public HashSet<CharacterPacket> getOtherPlayers() {
        return otherPlayers;
    }

    public boolean isPlayerHere(int id) {
        for (CharacterPacket packet : otherPlayers) {
            if (packet.uid  == id) {
                System.out.println("Player " + id + " is here");
                return true;
            }
        }

        return false;
    }

    public void onLoggedIn() {
        //this.uid = uid;
        alphaClientApp.startGame();
        //client.removeListener(loginResponseListener);
    }

    public void quit(int id) {
        Network.RemoveCharacter msg = new Network.RemoveCharacter();
        msg.id = id;
        client.sendTCP(msg);
    }

    public void addGameObject(GameObject obj) {
        objects.add(obj);

        alphaClientApp.getActiveWorld().addGameObject(obj);
    }

    public void addProjectile(Projectile projectile) {
        objects.add(projectile.object);

        alphaClientApp.getActiveWorld().addProjectile(projectile);
    }

    public void removeGameObject(int uid) {
        List<GameObject> remove = new ArrayList<>();
        for (GameObject object : objects) {
            if (object.getUniqueGameId() == uid) {
                remove.add(object);
                alphaClientApp.getActiveWorld().removeGameObject(object);
            }
        }
        objects.removeAll(remove);
    }

    public void updateObjectLocal(int uid, double x, double y) {
        for (GameObject object1 : objects) {
            if (object1.getUniqueGameId() == uid) {
                object1.setX(x);
                object1.setY(y);
                //sdSystem.out.println("updating pos");
            }
        }
    }

    public void sendChat(String message) {
        UserChat chat = new UserChat();
        chat.message = message;
        chat.cid = characterPacket.uid;
        client.sendTCP(chat);
    }

    public void updatePlayerHealth(int id, CombatObject object) {
        for (CharacterPacket packet : otherPlayers) {
            if (id == packet.uid) {
                packet.combat = object;
            }
        }
        if (id == getId()) {
            characterPacket.combat = object;
        }
        alphaClientApp.getActiveWorld().updatePlayerCombat(id, object);
    }

    public void updateNPCHealth(int uid, CombatObject object) {
        for (NPCPacket packet : npcs) {
            if (uid == packet.uid) {
                packet.combat = object;
            }
        }

        alphaClientApp.getActiveWorld().updateNPCCombat(uid, object);
    }

    public void sendHealthUpdate() {
        Network.UpdatePlayerCombat combat = new Network.UpdatePlayerCombat();
        combat.object = characterPacket.combat;
        combat.id = getId();
        client.sendTCP(combat);
    }

    public void sendProjectile(double oX, double oY, double x, double y) {
        AddProjectile projectile = new AddProjectile();
        projectile.originX = oX;
        projectile.originY = oY;
        projectile.destinationX = x;
        projectile.destinationY = y;
        projectile.sourceUser = getId();
        client.sendTCP(projectile);
    }

    public CharacterPacket getCharacterPacket() {
        return characterPacket;
    }

    public void setCharacterPacket(CharacterPacket characterPacket) {
        this.characterPacket = characterPacket;
    }

    public void addInventory(GameObject object) {
        characterPacket.inventory.addObject(object);
        alphaClientApp.getPanelControl().addItem(object);
        System.out.println("added object  " + object.getUniqueGameId());
    }

    public void removeInventory(int uid) {
        characterPacket.inventory.removeObjectFromUID(uid);
    }

    public void sendInventoryUpdate() {
        Network.UpdatePlayerInventory update = new Network.UpdatePlayerInventory();
        update.object = characterPacket.inventory;
        update.cid = getId();
        client.sendTCP(update);
    }

    public AlphaClientApp getAlphaClientApp() {
        return alphaClientApp;
    }


    public int getId() {
        return characterPacket.uid;
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

    public int getLatestWorldID() {
        return latestWorldID;
    }

    public void setLatestWorldID(int latestWorldID) {
        this.latestWorldID = latestWorldID;
    }
}
