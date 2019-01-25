package server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.CharacterPacket;
import shared.Network;

public class CharacterCommandListener extends Listener {
    private ServerHandler handler;

    public CharacterCommandListener(ServerHandler h) {
        handler = h;
    }


    @Override
    public void received (Connection c, Object object) {
        // We know all connections for this server are actually CharacterConnections.
        ServerHandler.CharacterConnection connection = (ServerHandler.CharacterConnection)c;
        CharacterPacket character = connection.character;

        if (object instanceof Network.RemoveCharacter) {
            // Remove the character from the server's list
            handler.removeClient(c.getID());

            // Broadcast to all to remove the character
            Network.RemoveCharacter msg = new Network.RemoveCharacter();
            msg.id = c.getID();
            System.out.println("Server recieved, client " + msg.id + " is quitting.");

            handler.getServer().sendToAllExceptTCP(c.getID(), msg);
        }


        /*
        Create a bounds for character updates
        We only want to update visible characters
         */
        if (object instanceof Network.UpdateCharacter) {
            Network.UpdateCharacter updateCharacter = (Network.UpdateCharacter)object;
            updateCharacter.id = ((Network.UpdateCharacter) object).id;
            updateCharacter.x = ((Network.UpdateCharacter) object).x;
            updateCharacter.y = ((Network.UpdateCharacter) object).y;

            handler.updateClient(updateCharacter.id, updateCharacter.x, updateCharacter.y);
            handler.getServer().sendToAllExceptTCP(c.getID(), updateCharacter);
        }
    }
}
