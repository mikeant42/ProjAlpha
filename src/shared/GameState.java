package shared;

import com.almasb.fxgl.scene.GameScene;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<CharacterPacket> players = new ArrayList<>();
    private List<Network.NPCPacket> npcs = new ArrayList<>();

    public GameState() {

    }

    public List<CharacterPacket> getPlayers() {
        return players;
    }

    public void setPlayers(List<CharacterPacket> players) {
        this.players = players;
    }

    public List<Network.NPCPacket> getNpcs() {
        return npcs;
    }

    public void setNpcs(List<Network.NPCPacket> npcsHere) {
        this.npcs = npcsHere;
    }
}
