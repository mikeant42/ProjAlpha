package shared;

/**
 * @id - this uid is actually the uid of the client from the Connection class. It's a temp uid assigned per session
 */

public class CharacterPacket extends Network.CombatEntity {
    public String name;
    public String otherStuff;
    public int moveState;
    public Inventory inventory;
    public boolean isLoaded = false;
}