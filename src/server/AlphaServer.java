package server;


import com.esotericsoftware.kryonet.Server;
import server.message.Message;
import shared.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class AlphaServer extends Server {

    //private HashSet<CharacterPacket> loggedIn = new HashSet();
    private Map<Integer, CharacterPacket> players = new ConcurrentHashMap<>();
    private GameMap map; // List<GameMap>


    private BlockingQueue<Message> serverMessagePool = new ArrayBlockingQueue<>(10000);


    // this number is used to make sure that objects dont acquire player ids
    public static final int PLAYER_COUNT = 16;

    public AlphaServer() {
        map = new GameMap(this);
    }

    @Override
    public void update(int i) throws IOException {
        super.update(i);

        // we are assuming all objects in this pool are queued
        // this update method is NOT in the same thread and does NOT update with the map's broadcast update
        for (CharacterPacket packet : players.values()) {
            for (Message message : serverMessagePool) {
                if (message.getId() == packet.uid) {
                    if (packet.isLoaded) {
                        message.send(this);
                        serverMessagePool.remove(message);
                        System.out.println("message! " + message.getContent().getClass());
                    }
                } else if (message.isSendToAll()) {
                    if (packet.isLoaded) {
                        message.send(this);
                        serverMessagePool.remove(message);
                    }
                }
            }
        }

    }


    public void logIn (ServerHandler.CharacterConnection c, CharacterPacket character) {
// Add existing characters to new logged in connection.
        for (CharacterPacket other : players.values()) {
            if (other.uid != c.getID()) {
                Network.AddCharacter addCharacter = new Network.AddCharacter();
                addCharacter.character = other;

                map.queueMessage(new Message(c.getID(), addCharacter, true));
                System.out.println("Client " + other.uid + " added to client " + c.getID());
            }
        }



        Network.LoginSuccess success = new Network.LoginSuccess();
        success.success = true;
        //success.uid = c.getID();
        success.packet = character;
        map.queueMessage(new Message(c.getID(), success, false));
        addLoggedIn(character);

        Network.AddCharacter addC = new Network.AddCharacter();
        addC.character = character;
        //sendToAllReadyExcept(c.getID(), addC, true); // Don't add the client's own player to his "other player" stack
        Message message = new Message(addC, true);
        message.setExcludeID(c.getID());
        map.queueMessage(message);

//        for (CharacterPacket packet : getLoggedIn()) {
//            System.out.println("Client " + packet.uid);
//        }


        //map.onCharacterAdd(character);
        map.addUnloadedPlayer(character);
    }

    protected void updateClient(int id, double x, double y) {
//        for (CharacterPacket packet : loggedIn) {
//            if (packet.uid == id) {
//                packet.x = x;
//                packet.y = y;
//            }
//        }
        CharacterPacket packet = players.get(id);
        packet.x = x;
        packet.y = y;
    }

    public void setIsLoaded(int cid, boolean tf) {
//        for (CharacterPacket packet : loggedIn) {
//            if (packet.uid == cid) {
//                packet.isLoaded = tf;
//            }
//        }
        players.get(cid).isLoaded = tf;
    }

    public Map<Integer, CharacterPacket> getLoggedIn() {
        return players;
    }

    public void removeClient(int id) {
//        for (CharacterPacket packet : loggedIn) {
//            if (packet.uid == id) {
//                loggedIn.remove(packet);
//                return;
//            }
//        }
        players.remove(id);
        map.removePlayer(id);
    }

    public void addLoggedIn(CharacterPacket packet) {
        players.put(packet.uid, packet);
    }


    public GameMap getMap() {
        return map;
    }


    public void sendToAll(Object object) {
        for (CharacterPacket packet : players.values()) {
            if (packet.isLoaded) {
                sendToTCP(packet.uid, object);
            }
        }
    }

    public void sendToAllExcept(Object o, int id) {
        for(CharacterPacket packet : players.values()) {
            if (id != packet.uid && packet.isLoaded) {
                sendToTCP(packet.uid, o);
            }
        }
    }

    public void addMessageToQueue(Message message) {
        try {
            serverMessagePool.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}