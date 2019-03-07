package client.render;

import client.MovementComponent;
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
    private int speed = 1000;
    //private int frameWidth = 48, frameHeight = 64; // These numbers are the x,y of the image / number of rows/columns
    private int framesPerRow = 3;
    private int frameWidth, frameHeight;
    private String file;

    private int idleStart, idleEnd, forwardStart, forwardEnd, backStart, backEnd, rightStart, rightEnd, leftStart, leftEnd;

    // TODO - make this more configurable for when we need to rotate objetcts for the animations, and when we don't

    public AnimatedMovementComponent(String file, int frameWidth, int frameHeight, int framesPerRow) {
        super();

        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.file = file;

    }

    @Override
    public void onAdded() {

        animIdle = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight, Duration.seconds(1), idleStart, idleEnd);
        animWalkUp = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight, Duration.seconds(1), forwardStart, forwardEnd);
        animRight = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight,Duration.seconds(1), rightStart, rightEnd);
        animWalkDown = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight,Duration.seconds(1), backStart, backEnd);
        animLeft = new AnimationChannel(file, framesPerRow, frameWidth, frameHeight,Duration.seconds(1), leftStart, leftEnd);

        texture = new AnimatedTexture(animIdle);
        System.out.println(texture.getHeight());


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

        if (getState() == Data.MovementState.STANDING) {
            if (texture.getAnimationChannel() != animIdle)
                texture.loopAnimationChannel(animIdle);
            animationSpeed = 0;


        }

        // update this before movementcomponent, or else will cause animation problems
        super.onUpdate(tpf);

    }

    public double getWidth() {
        return texture.getWidth();
    }

    public double getHeight() {
        return texture.getHeight();
    }

    public int getAnimationSpeed() {
        return speed;
    }

    public void setAnimationSpeed(int animationSpeed) {
        this.speed = animationSpeed;
    }

    public int getFramesPerRow() {
        return framesPerRow;
    }

    public void setFramesPerRow(int framesPerRow) {
        this.framesPerRow = framesPerRow;
    }

    public void setForward(int start, int end) {
        forwardStart = start;
        forwardEnd = end;
    }

    public void setIdle(int start, int end) {
        idleStart = start;
        idleEnd = end;
    }

    public void setBack(int start, int end) {
        backStart = start;
        backEnd = end;
    }

    public void setRight(int start, int end) {
        rightStart = start;
        rightEnd = end;
    }

    public void setLeft(int start, int end) {
        leftStart = start;
        leftEnd = end;
    }
}
