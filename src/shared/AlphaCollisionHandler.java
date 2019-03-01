package shared;

import com.almasb.fxgl.parser.tiled.TiledObject;

public abstract class AlphaCollisionHandler {
    public AlphaCollisionHandler() {}
    public abstract void handleStaticCollision(TiledObject object, GameObject projectile);
}
