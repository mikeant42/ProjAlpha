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

        This shouldn't be used to respond!!!!
        Just update the server's characters internally, then in a main loop the server will update all of the clients
        at a fixed time.
         */
        if (object instanceof Network.UpdateCharacter) {
            Network.UpdateCharacter updateCharacter = (Network.UpdateCharacter)object;

            handler.updateClient(updateCharacter.id, updateCharacter.x, updateCharacter.y);
            handler.getServer().sendToAllExceptTCP(updateCharacter.id, updateCharacter);
        }


        // In this case we are given an input Input and must produce the output of x,y
        // Or we could just send the inputs to the clients, and let them simulate. This would be simpler but it would make client cheating easy
//        if (object instanceof Network.UpdateCharacter2) {
//            Network.UpdateCharacter2 attempt = (Network.UpdateCharacter2)object;
//            System.out.println("Char " + attempt.id + " moved " + attempt.input.UP);
//
//            handler.updateClient(attempt.id, attempt.x, attempt.y);
//            handler.getServer().sendToAllExceptTCP(c.getID(), attempt);
//
//        }
    }
}
