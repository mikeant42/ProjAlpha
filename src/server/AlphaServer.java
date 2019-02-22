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
    private GameMap map; // List<GameMap>

    public AlphaServer() {
        map = new GameMap(this);
    }

    @Override
    public void update(int i) throws IOException {
        super.update(i);

        map.update();
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
        //success.id = c.getID();
        success.packet = character;
        sendToTCP(c.getID(), success);
        addLoggedIn(character);

        Network.AddCharacter addC = new Network.AddCharacter();
        addC.character = character;
        sendToAllExceptTCP(c.getID(), addC); // Don't add the client's own player to his "other player" stack

        for (CharacterPacket packet : getLoggedIn()) {
            System.out.println("Client " + packet.id);
        }


        map.onCharacterAdd(character);
    }

    protected void updateClient(int id, double x, double y) {
        for (CharacterPacket packet : loggedIn) {
            if (packet.id == id) {
                packet.x = x;
                packet.y = y;
            }
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


    public GameMap getMap() {
        return map;
    }
}
