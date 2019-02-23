package shared;

public abstract class ObjectUseHandler {
    public ObjectUseHandler() {}
    public abstract void onUse(CharacterPacket packet);
}
