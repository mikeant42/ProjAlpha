package server;

import java.io.IOException;
import java.util.HashSet;

import com.esotericsoftware.kryonet.Connection;


import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import shared.CharacterPacket;
import shared.Network;
import shared.Network.*;

/**
 * Suggestion: make it more oop by creating a CharacterManager class that will eventually extend to NPCManager, etc.
 * Alternative to networking
 * - Don't have the client draw itself on the screen. Instead just treat the client like another player, and have the server send back
 *   the movement information.
 */

public class ServerHandler {

    private Server server;
    private HashSet<CharacterPacket> loggedIn = new HashSet();

    public ServerHandler() throws IOException {
        server = new Server() {
            protected Connection newConnection() {
                // By providing our own connection implementation, we can store per
                // connection state without a connection ID to state look up.
                return new CharacterConnection();
            }
        };


        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Network.register(server);

        server.addListener(new LoginListener(this));
        server.addListener(new CharacterCommandListener(this));
        server.addListener(new WorldListener(this));

        server.bind(Network.port);
        server.start();
//    }
    }

    protected void removeClient(int id) {
        for (CharacterPacket packet : loggedIn) {
            if (packet.id == id) {
                loggedIn.remove(packet);
                return;
            }
        }
    }

    protected void updateClient(int id, double x, double y) {
        for (CharacterPacket packet : loggedIn) {
            if (packet.id == id) {
                packet.x = x;
                packet.y = y;
            }
        }
    }


    protected void logIn (CharacterConnection c, CharacterPacket character) {
        // Add existing characters to new logged in connection.
        for (CharacterPacket other : loggedIn) {
            AddCharacter addCharacter = new AddCharacter();
            addCharacter.character = other;
            c.sendTCP(addCharacter);
        }

        LoginSuccess success = new LoginSuccess();
        success.success = true;
        success.id = c.getID();
        c.sendTCP(success);
        loggedIn.add(character);

        Network.AddCharacter addC = new Network.AddCharacter();
        addC.character = character;
        server.sendToAllExceptTCP(c.getID(), addC); // Don't add the client's own player to his "other player" stack

        for (CharacterPacket packet : loggedIn) {
            System.out.println("Client " + packet.id);
        }
    }


    /*
    For each type of object there is an id prefix. Probably should put in a globals class in shared
     */
//    public int assignID(Object o,  Connection c) {
//        int id = 0;
//        if (o.getClass().equals(Network.Login.class)) {
//            id = 00 + c.getID();
//            System.out.println(id);
//        }
//
//        return id;
//    }


    private void sendAll(Object o) {
        server.sendToAllTCP(o);
    }

    protected Server getServer() {
        return server;
    }


    // This holds per connection state.
    static class CharacterConnection extends Connection {
        public CharacterPacket character;
    }


    public static void main (String[] args) throws IOException {
        Log.set(Log.LEVEL_DEBUG);

        new ServerHandler();
    }

    public HashSet<CharacterPacket> getLoggedIn() {
        return loggedIn;
    }
}
