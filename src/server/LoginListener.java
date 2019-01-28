package server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.CharacterPacket;
import shared.Network;

public class LoginListener extends Listener {

    ServerHandler handler;

    public LoginListener(ServerHandler handler) {
        this.handler = handler;
    }

    @Override
    public void received (Connection c, Object object) {
        // We know all connections for this server are actually CharacterConnections.
        ServerHandler.CharacterConnection connection = (ServerHandler.CharacterConnection)c;
        CharacterPacket character = connection.character;


        if (object instanceof Network.Login) {
            // Ignore if already logged in.

            //if (character != null) return;

            // Reject if the name is invalid.
            String name = ((Network.Login)object).name;
            if (!isValid(name)) {
                c.close();
                return;
            }


            // This code should retrieve relevant info from the db and check the strings
            // This is where the db fills all of the information about the character
            character = new CharacterPacket();
            character.x = 50;
            character.y = 50;
            character.id = connection.getID();
            handler.logIn(connection, character);



        }
    }

    private boolean isValid (String value) {
        if (value == null) return false;
        value = value.trim();
        if (value.length() == 0) return false;
        return true;
    }
}
