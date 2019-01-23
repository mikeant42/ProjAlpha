package client;

import client.ClientHandler;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import shared.CharacterPacket;
import shared.Network;

public class CharacterResponseListener extends Listener {
    private ClientHandler handler;

    public CharacterResponseListener(ClientHandler h) {
        handler = h;
    }


    @Override
    public void received (Connection c, Object object) {
        if (object instanceof Network.AddCharacter) {
            Network.AddCharacter msg = (Network.AddCharacter)object;
            handler.getOtherPlayers().add(msg.character);
            System.out.println(handler.getOtherPlayers().toString());
        }

//        if (object instanceof Network.UpdateCharacter) {
//            ui.updateCharacter((Network.UpdateCharacter)object);
//            return;
//        }
//

        // IF we get the command to remove from the server all we do is remove it from the game world
        if (object instanceof Network.RemoveCharacter) {
            Network.RemoveCharacter msg = (Network.RemoveCharacter) object;
            handler.getScreen().removeNetworkedEntity(msg.id);
            System.out.println("Removing client " + msg.id + " from the world");
            return;
        }

        }
}

