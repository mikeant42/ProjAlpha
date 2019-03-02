package shared.collision;

import com.almasb.fxgl.parser.tiled.TiledObject;
import shared.GameObject;

public abstract class AlphaCollisionHandler {
    public AlphaCollisionHandler() {}
    public abstract void handleStaticCollision(TiledObject object, GameObject projectile);
}
