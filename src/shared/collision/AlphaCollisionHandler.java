package shared.collision;

import com.almasb.fxgl.parser.tiled.TiledObject;
import shared.CharacterPacket;
import shared.GameObject;
import shared.Network;

public abstract class AlphaCollisionHandler {
    public AlphaCollisionHandler() {}

    public abstract void handleCollision(TiledObject object, GameObject projectile);
    public abstract void handleCollision(GameObject object, CharacterPacket packet);
    public abstract void handleCollision(GameObject object, Network.NPCPacket packet);
}
