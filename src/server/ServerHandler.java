package server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.esotericsoftware.kryonet.Connection;


import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import shared.CharacterPacket;
import shared.Network;
import shared.Network.*;


public class ServerHandler {

    private AlphaServer server;
    private boolean running = true;
    private long tick = 0;
    private long fakeFPS = 60;
    private long broadcastFPS = 5;
    private long broadcastTick = 0;
    private int broadcastTickBuffer = 11; // every x internal ticks we send an update to the clients


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

        //runBroadcastLoop();

        //ExecutorService service = Executors.newCachedThreadPool();


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
        updateThread.start();

        runBroadcastLoop();





        Log.TRACE();

//    }
    }

    public void runBroadcastLoop() {
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.NONE();

                long now;
                long updateTime;
                long wait;

                final long OPTIMAL_TIME = 1000000000 / broadcastFPS;

                while (running) {
                    now = System.nanoTime();

                    broadcastTick++;
                    server.getMap().updateExternal(broadcastTick);



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
        updateThread.start();
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

    public Map<Integer, CharacterPacket> getLoggedIn() {
        return server.getLoggedIn();
    }
}
