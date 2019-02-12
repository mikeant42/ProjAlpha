package client;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.scene.Viewport;
import javafx.geometry.Point2D;


import static client.Screen.TILESIZE;

public class AlphaUtil {

    // We need this for multi, getting the position of the camera from the position of the entity. So we can
    // know the other player's camera pos
    public static Point2D getCameraPosition(Entity entity) {
        Viewport viewport = new Viewport(entity.getX(), entity.getY());
        viewport.bindToEntity(entity, FXGL.getAppWidth()/2 - TILESIZE/2, FXGL.getAppHeight()/2 - TILESIZE/2);
        viewport.setZoom(1.2);
        return viewport.getOrigin();
    }
}
