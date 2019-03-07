package server;

import java.io.IOException;
import java.util.HashSet;

import com.esotericsoftware.kryonet.Connection;


import com.esotericsoftware.kryonet.Listener;
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
    private boolean running = true;
    private long tick = 0;
    private long fakeFPS = 60;
    private long broadcastTick = 0;
    private int broadcastTickBuffer = 20; // every x internal ticks we send an update to the clients


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
        server.addListener(new Listener.ThreadedListener(new CharacterCommandListener(this)));
        server.addListener(new WorldListener(this));

        server.bind(Network.port);
        server.start();


        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.NONE();

                long now;
                long updateTime;
                long wait;

                final long OPTIMAL_TIME = 1000000000 / fakeFPS;

                while (running) {
                    now = System.nanoTime();

                    tick();
                    if (broadcastTick + broadcastTickBuffer == tick) {
                        broadcastTick = tick;
                        System.out.println("broadcasting");
                        server.getMap().updateExternal(broadcastTick);

                    }

                    server.getMap().updateAction(tick);

                    updateTime = System.nanoTime() - now;
                    wait = (OPTIMAL_TIME - updateTime) / 1000000;

                    try {
                        Thread.sleep(wait);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("thread sleep error");
                    }


                }

            }
        });
        updateThread.run();


//        // lets run the npc update method in its own thread
//        // the kryo server update thread cannot be blocked
//        Thread npcThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.NONE();
//
//                long now;
//                long updateTime;
//                long wait;
//
//                final long OPTIMAL_TIME = 1000000000 / fakeFPS;
//
//                while (running) {
//                    now = System.nanoTime();
//
//                    tick();
//                    server.getMap().updateAction(tick);
//
//                    updateTime = System.nanoTime() - now;
//                    wait = (OPTIMAL_TIME - updateTime) / 1000000;
//
//                    try {
//                        Thread.sleep(wait);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        System.err.println("thread sleep error");
//                    }
//
//
//                }
//
////
////                double ns = 1000000000.0 / fakeFPS;
////                double delta = 0;
////
////                long lastTime = System.nanoTime();
////                long timer = System.currentTimeMillis();
////
////                while (running) {
////                    long now = System.nanoTime();
////                    delta += (now - lastTime) / ns;
////                    lastTime = now;
////
////                    while (delta >= 1) {
////                        tick();
////                        server.getMap().updateAction(tick);
////                        delta--;
////                    }
////                }
//            }
//        });
//        npcThread.run();


        Log.TRACE();

//    }
    }

    private void tick() {
        tick++;
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
