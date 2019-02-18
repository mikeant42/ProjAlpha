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

    private AlphaServer server;


    public ServerHandler() throws IOException {
        server = new AlphaServer() {
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

        // lets run the npc update method in its own thread
        Thread npcThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.NONE();
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                       server.getMap().updateAction();
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        npcThread.run();


        Log.NONE();

//    }
    }

    protected void updateClient(int id, double x, double y) {
        for (CharacterPacket packet : server.getLoggedIn()) {
            if (packet.id == id) {
                packet.x = x;
                packet.y = y;
            }
        }
    }


    private void sendAll(Object o) {
        server.sendToAllTCP(o);
    }

    protected AlphaServer getServer() {
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
        return server.getLoggedIn();
    }
}
