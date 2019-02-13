package client;

import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;
import shared.Data;

/**
 * Class for moving characters who also have an animated texture
 *
 */
public class AnimatedMovementComponent extends MovementComponent {
    private AnimatedTexture texture;


    private AnimationChannel animIdle, animWalkUp, animWalkDown, animRight, animLeft;

    private int animationSpeed = 0;
    private int speed = 700;
    //private int frameWidth = 48, frameHeight = 64; // These numbers are the x,y of the image / number of rows/columns
    private int framesPerRow = 3;

    // TODO - make this more configurable for when we need to rotate objetcts for the animations, and when we don't

    public AnimatedMovementComponent(String file, int frameWidth, int frameHeight, int framesPerRow) {
        super();

        animIdle = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight, Duration.seconds(1), 8, 9);
        animWalkUp = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight, Duration.seconds(1), 0, 2);
        animRight = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight,Duration.seconds(1), 3, 5);
        animWalkDown = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight,Duration.seconds(1), 6, 8);
        animLeft = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight,Duration.seconds(1), 9, 11);

        texture = new AnimatedTexture(animIdle);
    }

//    public AnimatedMovementComponent(String file, int frameWidth, int frameHeight, int framesPerRow) {
//        super();
//        this.frameWidth = frameWidth;
//        this.frameHeight = frameHeight;
//        this.framesPerRow = framesPerRow;
//
//        animIdle = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight, Duration.seconds(1), 0, 2);
//        animWalkUp = new AnimationChannel(file, 4, 32, 42, Duration.seconds(1), 0, 3);
//
//        texture = new AnimatedTexture(animIdle);
//
//
//
////        animatedTexture.setOnCycleFinished(() -> {
////            if (animatedTexture.getAnimationChannel() == animJump) {
////
////                setState(FALL);
////            }
////        });
//    }

    @Override
    public void onAdded() {
        entity.setView(texture);
    }


    @Override
    public void onUpdate(double tpf) {
        if (getState() == Data.MovementState.RUNNING_FORWARD && texture.getAnimationChannel() != animWalkUp) {
            texture.loopAnimationChannel(animWalkUp);
        } else if (getState() == Data.MovementState.RUNNING_BACK && texture.getAnimationChannel() != animWalkDown) {
            texture.loopAnimationChannel(animWalkDown);
        } else if (getState() == Data.MovementState.RUNNING_LEFT && texture.getAnimationChannel() != animLeft) {
            texture.loopAnimationChannel(animLeft);
        } else if (getState() == Data.MovementState.RUNNING_RIGHT && texture.getAnimationChannel() != animRight) {
            texture.loopAnimationChannel(animRight);
        }

        // If no inputs are registering, its safe to move idle
        if (!isMoving()) {
            animIdle();
            animationSpeed = 0;
        }


        super.onUpdate(tpf);

    }

    public void animRight() {
        setState(Data.MovementState.RUNNING_RIGHT);
    }

    public void animLeft() {
        setState(Data.MovementState.RUNNING_LEFT);
    }

    public void animUp() {
        setState(Data.MovementState.RUNNING_FORWARD);
    }

    public void animDown() {
        if (texture.getAnimationChannel() != animWalkDown)
            setState(Data.MovementState.RUNNING_BACK);
    }

    public void animIdle() {
        if (texture.getAnimationChannel() != animIdle)
            texture.loopAnimationChannel(animIdle);
    }

    public double getWidth() {
        return texture.getWidth();
    }

    public double getHeight() {
        return texture.getHeight();
    }

    public AnimatedTexture getActiveAnimation() {
        return texture;
    }


}
