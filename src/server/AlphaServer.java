package server;

import com.esotericsoftware.kryonet.Server;
import shared.CharacterPacket;
import shared.EntityType;
import shared.Network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AlphaServer extends Server {

    private HashSet<CharacterPacket> loggedIn = new HashSet();
    private NPCHandler npcHandler;

    public AlphaServer() {
        RoamingBehavior behavior = new RoamingBehavior(500, 500);

        npcHandler = new NPCHandler();
        npcHandler.addNPC(behavior);


    }

    @Override
    public void update(int i) throws IOException {
        super.update(i);

        for (NPCBehavior behavior : npcHandler.getNPCs()) {
            sendToAllTCP(behavior.formUpdate());
        }

    }

    public void logIn (ServerHandler.CharacterConnection c, CharacterPacket character) {
// Add existing characters to new logged in connection.
        for (CharacterPacket other : getLoggedIn()) {
            if (other.id != c.getID()) {
                Network.AddCharacter addCharacter = new Network.AddCharacter();
                addCharacter.character = other;
                c.sendTCP(addCharacter);
                System.out.println("Client " + other.id + " added to client " + c.getID());
            }
        }

        Network.LoginSuccess success = new Network.LoginSuccess();
        success.success = true;
        success.id = c.getID();
        sendToTCP(c.getID(), success);
        addLoggedIn(character);

        Network.AddCharacter addC = new Network.AddCharacter();
        addC.character = character;
        sendToAllExceptTCP(c.getID(), addC); // Don't add the client's own player to his "other player" stack

        for (CharacterPacket packet : getLoggedIn()) {
            System.out.println("Client " + packet.id);
        }

        // We also have to spawn all the npcs in his level
        for (NPCBehavior npcBehavior : npcHandler.getNPCs()) {
            c.sendTCP(npcBehavior.getData());
        }
    }


    public HashSet<CharacterPacket> getLoggedIn() {
        return loggedIn;
    }

    public void removeClient(int id) {
        for (CharacterPacket packet : loggedIn) {
            if (packet.id == id) {
                loggedIn.remove(packet);
                return;
            }
        }
    }

    public void addLoggedIn(CharacterPacket packet) {
        loggedIn.add(packet);
    }

    public NPCHandler getNpcHandler() {
        return npcHandler;
    }
}
