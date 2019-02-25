package client.render;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.Entity;

public class Camera {
    private int offset = 256;

    public Camera() {
        FXGL.getApp().getGameScene().getViewport().setZoom(1.2);
    }

    public void bind(Entity entity) {
        FXGL.getApp().getGameScene().getViewport().bindToEntity(entity, FXGL.getAppWidth()/2 - offset/2, FXGL.getAppHeight()/2 - offset/2);
    }
}
