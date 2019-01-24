package client;

import client.ClientHandler;
import com.almasb.fxgl.app.FXGL;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.event.Event;
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

        // IF we get the command to remove from the server all we do is remove it from the game world
        if (object instanceof Network.RemoveCharacter) {
            Network.RemoveCharacter msg = (Network.RemoveCharacter) object;
            handler.getScreen().removeNetworkedEntity(msg.id);
            System.out.println("Removing client " + msg.id + " from the world");
            return;
        }


        if (object instanceof Network.UpdateCharacter) {
            Network.UpdateCharacter update = (Network.UpdateCharacter)object;
            handler.updatePlayerLocal(update.x, update.y, update.id);
            //FXGL.getEventBus().fireEvent(new MoveEvent(MoveEvent.CHARACTER, update)); // This could need to be run in the main thread ;(
        }

    }
}

