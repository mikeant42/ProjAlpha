package server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import server.message.Message;
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
            handler.getServer().removeClient(c.getID());

            // Broadcast to all to remove the character
            Network.RemoveCharacter msg = new Network.RemoveCharacter();
            msg.id = c.getID();
            System.out.println("og uid " + c.getID());
            System.out.println("Server recieved, client " + msg.id + " is quitting.");


            Message data = new Message(msg, false);
            data.setExcludeID(c.getID());
            handler.getServer().addMessageToQueue(data);
        }


        /*
        Create a bounds for character updates
        We only want to update visible characters

        Check against the previous player position to make sure its valid
         */
        if (object instanceof Network.UpdateCharacter) {
            Network.UpdateCharacter updateCharacter = (Network.UpdateCharacter)object;

            handler.getServer().updateClient(updateCharacter.id, updateCharacter.x, updateCharacter.y);
            //handler.getServer().sendToAllReadyExcept(updateCharacter.id, updateCharacter);
            Message message = new Message(updateCharacter, false);
            message.setExcludeID(updateCharacter.id);
            handler.getServer().getMap().queueMessage(message);

            //Message message = new Message(0, updateCharacter, false, true);
           // message.setExcludeID(updateCharacter.id);
           // handler.getServer().getMap().queueMessage(message);

        }

        if (object instanceof Network.UpdatePlayerInventory) {
            Network.UpdatePlayerInventory updateCharacter = (Network.UpdatePlayerInventory)object;


            handler.getServer().getMap().updatePlayerInventory(updateCharacter.cid, ((Network.UpdatePlayerInventory) object).object);


        }

        if (object instanceof Network.UserChat) {
            Network.UserChat chat = (Network.UserChat)object;
            //handler.getServer().getMap().queueMessage(new Message(0, chat, false, true));
            //handler.getServer().sendToAllReady(chat);
            handler.getServer().getMap().queueMessage(new Message(chat, false));
        }


        // In this case we are given an input MovementState and must produce the output of x,y
        // Or we could just send the inputs to the clients, and let them simulate. This would be simpler but it would make client cheating easy
//        if (object instanceof Network.UpdateCharacter2) {
//            Network.UpdateCharacter2 attempt = (Network.UpdateCharacter2)object;
//            System.out.println("Char " + attempt.uid + " moved " + attempt.input.UP);
//
//            handler.updateClient(attempt.uid, attempt.x, attempt.y);
//            handler.getServer().sendToAllExceptTCP(c.getID(), attempt);
//
//        }
    }
}
