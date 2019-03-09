package client.render;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.scene.Viewport;
import javafx.geometry.Point2D;

public class Camera {
    private int offset = 128;
    private Viewport viewport;
    private int bounds = 200;
    private Entity fakePlayer;

    public Camera() {
        viewport = FXGL.getApp().getGameScene().getViewport();
        viewport.setZoom(1.2);

        fakePlayer = new Entity();
    }

    public void bind(Entity entity) {

//        int minX = -(int)(entity.getX() - (viewport.getVisibleArea().getMinX()+bounds));
//        int minY = -(int)(entity.getY() - (viewport.getVisibleArea().getMinY()+bounds));
//
//        int maxX = (int)(entity.getX() - (viewport.getVisibleArea().getMaxX()+bounds));
//        int maxY = (int)(entity.getY() - (viewport.getVisibleArea().getMaxY()+bounds));
//
//
//        double posX = FXGLMath.lerp(viewport.getX(), entity.getX(), 0.45);
//        double posY = FXGLMath.lerp(viewport.getY(), entity.getY(), 0.45);
////
        viewport.focusOn(entity);


    }

    public void updateBounds(Entity entity) {
//
        Point2D newOrigin = entity.getCenter().subtract(FXGL.getAppWidth() / 2.0 - offset, FXGL.getAppHeight() / 2.0 - offset);


        //viewport.focusOn(entity);
        double posX = FXGLMath.lerp(viewport.getX(), newOrigin.getX(), 0.025);
        double posY = FXGLMath.lerp(viewport.getY(), newOrigin.getY(), 0.025);
        viewport.setX(posX);
        viewport.setY(posY);
//
//        viewport.setX(posX);
//        viewport.setY(posY);



        //FXGL.getApp().getGameScene().getViewport().setBounds((int)entity.getX()-300,(int)entity.getY()-300,
       // /        (int)entity.getX()+300,(int)entity.getY()+300);
    }
}
