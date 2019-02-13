package shared;

/**
 * @id - this id is actually the id of the client from the Connection class. It's a temp id assigned per session
 */

public class CharacterPacket {
    public String name;
    public String otherStuff;
    public int id;
    public double x, y;
    public int moveState;
}