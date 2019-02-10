package client;

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
        if (animationSpeed != 0) {
//            if (texture.getAnimationChannel() == animIdle) {
//                if (getInput().UP) {
//                    texture.loopAnimationChannel(animWalkUp);
//                    System.out.println("looping up");
//                } else if (getInput().DOWN) {
//                    texture.loopAnimationChannel(animWalkDown);
//                } else if (getInput().RIGHT) {
//                    texture.loopAnimationChannel(animRight);
//                } else if (getInput().LEFT) {
//                    texture.loopAnimationChannel(animLeft);
//                }
//            }

            // If no inputs are registering, its safe to move idle
            if (!isMoving()) {
                texture.loopAnimationChannel(animIdle);
                animationSpeed = 0;
            }

       }


        super.onUpdate(tpf);

    }

    public void animRight() {
        animationSpeed = speed;
        if (texture.getAnimationChannel() != animRight)
            texture.loopAnimationChannel(animRight);
        getInput().RIGHT = true;
        //getEntity().setScaleX(1);
    }

    public void animLeft() {
        animationSpeed = -speed;
        if (texture.getAnimationChannel() != animLeft)
            texture.loopAnimationChannel(animLeft);
        getInput().LEFT = true;
        //getEntity().setScaleX(-1);
    }

    public void animUp() {
        animationSpeed = speed;
        if (texture.getAnimationChannel() != animWalkUp)
            texture.loopAnimationChannel(animWalkUp);
        getInput().UP = true;
    }

    public void animDown() {
        animationSpeed = -speed;
        if (texture.getAnimationChannel() != animWalkDown)
            texture.loopAnimationChannel(animWalkDown);
        getInput().DOWN = true;
    }

    public double getWidth() {
        return texture.getWidth();
    }

    public double getHeight() {
        return texture.getHeight();
    }

}
