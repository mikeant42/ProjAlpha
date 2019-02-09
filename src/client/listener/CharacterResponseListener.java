package client.listener;

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
            if (msg.character.id != handler.getId()) { // lets make sure we dont dupelicate ourselves
                handler.getOtherPlayers().add(msg.character);
                System.out.println(handler.getOtherPlayers().size());
            }
        }

        // IF we get the command to remove from the server all we do is remove it from the game world
        if (object instanceof Network.RemoveCharacter) {
            Network.RemoveCharacter msg = (Network.RemoveCharacter) object;
            handler.getScreen().removeNetworkedEntity(msg.id);
            System.out.println("Removing client " + msg.id + " from the world");
            return;
        }

//        if (ClientHandler.LOGIN_STATUS) {
//            if (object instanceof Network.UpdateCharacter2) {
//                Network.UpdateCharacter2 update = (Network.UpdateCharacter2)object;
//                handler.updatePlayerLocal(update.x, update.y, update.input, update.velX,
//                        update.velY, update.id);
//            }
//        }


        if (ClientHandler.LOGIN_STATUS) {
            if (object instanceof Network.UpdateCharacter) {
                Network.UpdateCharacter update = (Network.UpdateCharacter) object;
                handler.updatePlayerLocal(update.input, update.x, update.y, update.id);
                //FXGL.getEventBus().fireEvent(new MoveEvent(MoveEvent.CHARACTER, update)); // This could need to be run in the main thread ;(
            }
        }

    }
}
