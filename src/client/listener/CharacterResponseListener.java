package client.listener;

import client.ClientHandler;
import client.PlayerEvent;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
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
           // if (msg.character.uid != handler.getId()) { // lets make sure we dont dupelicate ourselves
                handler.getOtherPlayers().add(msg.character);
                handler.getAlphaClientApp().getActiveWorld().addPlayer(msg.character);
                System.out.println(handler.getOtherPlayers().size());
           // }
        }

        // IF we get the command to remove from the server all we do is remove it from the game world
        if (object instanceof Network.RemoveCharacter) {
            Network.RemoveCharacter msg = (Network.RemoveCharacter) object;
            handler.getAlphaClientApp().getActiveWorld().removeEntityLater(msg.id);
            System.out.println("Removing client " + msg.id + " from the world");
        }

        if (object instanceof Network.UpdateCharacterPacket) {
            Network.UpdateCharacterPacket update = (Network.UpdateCharacterPacket)object;
            if (update.player.uid == handler.getId()) {
                handler.setCharacterPacket(update.player); // this updates the inventory

                if (update.hasDied) {
                    // we need to alert everyone that we have died
                    handler.getAlphaClientApp().getEventBus().fireEvent(new PlayerEvent(PlayerEvent.DEATH));
                }
            }

            handler.updatePlayerLocal(update.player.moveState, update.player.x, update.player.y, update.player.uid);

            handler.updatePlayerHealth(update.player.uid, update.player.combat);
        }

//        if (ClientHandler.LOGIN_STATUS) {
//            if (object instanceof Network.UpdateCharacter2) {
//                Network.UpdateCharacter2 update = (Network.UpdateCharacter2)object;
//                handler.updatePlayerLocal(update.x, update.y, update.input, update.velX,
//                        update.velY, update.uid);
//            }
//        }


       // if (ClientHandler.LOGIN_STATUS) {
            if (object instanceof Network.UpdateCharacter) {
                Network.UpdateCharacter update = (Network.UpdateCharacter) object;
                handler.updatePlayerLocal(update.moveState, update.x, update.y, update.id);
                //FXGL.getEventBus().fireEvent(new PlayerEvent(PlayerEvent.CHARACTER, update)); // This could need to be run in the main thread ;(
            }


            if (object instanceof Network.AddInventoryItem) {
                Network.AddInventoryItem update = (Network.AddInventoryItem) object;
                handler.addInventory(update.object);
            }

            if (object instanceof Network.UserChat) {
                Network.UserChat chat = (Network.UserChat)object;
                handler.getAlphaClientApp().addChatMsg(chat);
            }

        if (object instanceof Network.UpdatePlayerCombat) {
            Network.UpdatePlayerCombat combat = (Network.UpdatePlayerCombat)object;
            handler.updatePlayerHealth(combat.id, combat.object);
        }

      //  }

    }
}

