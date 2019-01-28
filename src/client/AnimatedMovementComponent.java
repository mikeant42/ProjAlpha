package client;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

/**
 * Class for moving characters who also have an animated texture
 *
 */
public class AnimatedMovementComponent extends MovementComponent {
    private AnimatedTexture texture;


    private AnimationChannel idle, walk; // We'll wind up adding more

    private int animationSpeed = 0;

    public AnimatedMovementComponent(String file) {
        super();

        idle = new AnimationChannel(file, 4, 32, 42, Duration.seconds(1), 1, 1);
        walk = new AnimationChannel(file, 4, 32, 42, Duration.seconds(1), 0, 3);

        texture = new AnimatedTexture(idle);
    }

    @Override
    public void onAdded() {
        entity.setView(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        super.onUpdate(tpf);


        if (animationSpeed != 0) {
            if (texture.getAnimationChannel() == idle) {
                texture.loopAnimationChannel(walk);
            }

            animationSpeed = (int)(animationSpeed * 0.9);

            if (FXGLMath.abs(animationSpeed) < 1) {
                animationSpeed = 0;
                texture.loopAnimationChannel(idle);
            }
        }
    }

    public void animRight() {
        animationSpeed = 150;
        getEntity().setScaleX(1);
    }

    public void animLeft() {
        animationSpeed = -150;
        getEntity().setScaleX(-1);
    }

    public double getWidth() {
        return texture.getWidth();
    }

    public double getHeight() {
        return texture.getHeight();
    }

}
