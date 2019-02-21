package shared;

public abstract class AlphaCollisionHandler {
    public AlphaCollisionHandler() {

    }
    public abstract void collide(CharacterPacket packet, GameObject object);
}
