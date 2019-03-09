package client.render;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class ProjectileComponent extends Component {

    private double velX, velY;
    private Point2D projectilePosition;

    public ProjectileComponent(double x, double y) {
        //destination = new Point2D(x,y);
        velX = x;
        velY = y;
    }



    @Override
    public void onUpdate(double dtf) {


//        projectilePosition = (FXGLMath.lerp(getEntity().getPosition().getX(), getEntity().getPosition().getY(),
//                destination.getX(), destination.getY(), 0.01));
        //FXGLMath.ler

        // Otherwise, continue next frame

        projectilePosition = new Point2D(getEntity().getX()+velX, getEntity().getY()+velY);


        //getEntity().setPosition(getEntity().getPosition().add(5,5));
    }

    public Point2D getProjectilePosition() {
        return projectilePosition;
    }
}
