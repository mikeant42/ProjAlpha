package client.render;

import client.OverlayTextComponent;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class ProjectileComponent extends Component {

    private Point2D destination;
    private AnimatedTexture texture;
    private int timed;

    public ProjectileComponent(double x, double y, int timed) {
        destination = new Point2D(x,y);

        this.timed = timed;
    }

    public void setTexture(AnimatedTexture texture) {
        this.texture = texture;
    }

    @Override
    public void onAdded() {
        FXGL.getApp().getMasterTimer().runOnceAfter(() -> {
            getEntity().removeFromWorld();
            }, Duration.seconds(timed));
    }

    @Override
    public void onUpdate(double dtf) {
        float timeSinceStarted = 0f;

        timeSinceStarted += dtf;
        getEntity().getPositionComponent().setValue(FXGLMath.lerp(getEntity().getPosition().getX(), getEntity().getPosition().getY(),
                destination.getX(), destination.getY(), 0.12));
        //FXGLMath.ler

        // If the object has arrived, stop the coroutine
        if (getEntity().getPosition().getX() == (destination.getX()) && getEntity().getY() == destination.getY()) {
            onHit();
            //System.out.println("hit");
        }

        // Otherwise, continue next frame


        //getEntity().setPosition(getEntity().getPosition().add(5,5));
    }

    private void onHit() {
        getEntity().removeFromWorld();
        System.out.println("endd");

        //getEntity().removeComponent(ProjectileComponent.class);
    }
}
