package server;

import java.io.IOException;
import java.util.HashSet;

import com.esotericsoftware.kryonet.Connection;


import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import shared.CharacterPacket;
import shared.Network;
import shared.Network.*;

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

//            private boolean isValid (String value) {
//                if (value == null) return false;
//                value = value.trim();
//                if (value.length() == 0) return false;
//                return true;
//            }
//
//            public void disconnected (Connection c) {
//                CharacterConnection connection = (CharacterConnection)c;
//                if (connection.character != null) {
//                    loggedIn.remove(connection.character);
//                    System.out.println("Removed char");
//
//                    RemoveCharacter removeCharacter = new RemoveCharacter();
//                    removeCharacter.id = connection.character.id;
//                    server.sendToAllTCP(removeCharacter);
//                }
//            }
//        });
        server.bind(Network.port);
        server.start();
//    }
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
        c.sendTCP(success);
        loggedIn.add(character);

        Network.AddCharacter addC = new Network.AddCharacter();
        addC.character = character;
        server.sendToAllTCP(character);

        System.out.println(loggedIn.toString());
    }

    private void sendAll(Object o) {
        server.sendToAllTCP(o);
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
