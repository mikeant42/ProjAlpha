package server;


import com.esotericsoftware.kryonet.Server;
import shared.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AlphaServer extends Server {

    private HashSet<CharacterPacket> loggedIn = new HashSet();
    private GameMap map; // List<GameMap>

    private List<Tuple<Integer, Object>> messageQueue = new ArrayList<>();
    private List<Tuple<Integer, Object>> messagesToAddQueue = new ArrayList<>();
    private List<Tuple<Integer, Object>> messageToRemoveQueue = new ArrayList<>();


    // this number is used to make sure that objects dont acquire player ids
    public static final int PLAYER_COUNT = 16;

    public AlphaServer() {
        map = new GameMap(this);
    }

    @Override
    public void update(int i) throws IOException {
        super.update(i);

        messageQueue.addAll(messagesToAddQueue);
        messageQueue.removeAll(messageToRemoveQueue);

        messagesToAddQueue.clear();
        messageToRemoveQueue.clear();

        // we need to make sure that this doesnt get too large
        // limitation of this system = cant send to any "except" - maybe implement the message system
        for (CharacterPacket packet : loggedIn) {
            for (Tuple<Integer, Object> message : messageQueue) {
                if (message.x.intValue() == packet.id && packet.isLoaded) {
                    sendToTCP(packet.id, message.y);
                    removeMessageFromQueue(message);
                    System.out.println("message! " + message.y.getClass());
                }
            }
        }


    }

    public void logIn (ServerHandler.CharacterConnection c, CharacterPacket character) {
// Add existing characters to new logged in connection.
        for (CharacterPacket other : getLoggedIn()) {
            if (other.id != c.getID()) {
                Network.AddCharacter addCharacter = new Network.AddCharacter();
                addCharacter.character = other;

                map.queueMessage(new Message(c.getID(), addCharacter, true));
                System.out.println("Client " + other.id + " added to client " + c.getID());
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
        for (CharacterPacket packet : loggedIn) {
            if (packet.id == id) {
                packet.x = x;
                packet.y = y;
            }
        }
    }

    public void setIsLoaded(int cid, boolean tf) {
        for (CharacterPacket packet : loggedIn) {
            if (packet.id == cid) {
                packet.isLoaded = tf;
            }
        }
    }

    public HashSet<CharacterPacket> getLoggedIn() {
        return loggedIn;
    }

    public void removeClient(int id) {
        for (CharacterPacket packet : loggedIn) {
            if (packet.id == id) {
                loggedIn.remove(packet);
                return;
            }
        }
    }

    public void addLoggedIn(CharacterPacket packet) {
        loggedIn.add(packet);
    }


    public GameMap getMap() {
        return map;
    }

    public void sendToAllReady(Object o, boolean queue) {
        for (CharacterPacket packet : loggedIn) {
            if (packet.isLoaded) {
                sendToTCP(packet.id, o);
            } else if (queue) {
                sendWithQueue(packet.id, o , queue);
            }
        }
    }

    public void sendToAllReady(Object o) {
        sendToAllReady(o, false);
    }

    public void sendWithQueue(int id, Object o, boolean queue) {
//        for (CharacterPacket packet : loggedIn) {
//            if (packet.uid == uid) {
        if (queue) {
            messagesToAddQueue.add(new Tuple<>(id, o));
            System.out.println("Message added to queue");
        } else {
            sendToTCP(id, o);
        }
        //   }
        // }
    }

    public void sendWithQueueExcept(int id, Object o, boolean queue) {
        if (queue) {
            messagesToAddQueue.add(new Tuple<>(id, o));
            System.out.println("Message added to queue");
        } else {
            sendToTCP(id, o);
        }
    }

    public void sendToAllReadyExcept(int i, Object o) {
        sendToAllReadyExcept(i, o, false);
    }

    public void sendToAllReadyExcept(int i, Object o, boolean queue) {
        for (CharacterPacket packet : loggedIn) {
            if (packet.isLoaded && packet.id != i) {
                sendToTCP(packet.id, o);
            } else if (queue && packet.id != i) {
                messagesToAddQueue.add(new Tuple<>(packet.id, o));
                System.out.println("hello");
            }
        }
    }

    protected void removeMessageFromQueue(Tuple<Integer, Object> msg) {
        messageToRemoveQueue.add(msg);
    }

    public List<Tuple<Integer, Object>> getMessageQueue() {
        return messageQueue;
    }
}