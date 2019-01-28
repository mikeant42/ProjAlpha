package client;

import com.almasb.fxgl.entity.component.Component;
import shared.Data;

enum INVALID_MOVE {
    RIGHT, LEFT, UP, DOWN, NONE
}

public class MovementComponent extends Component {
    // note that this component is injected automatically

    private double speed;
    private int moveFactor = 245;
    private INVALID_MOVE move = INVALID_MOVE.NONE;

    private Data.Input input;

    public MovementComponent() {
        this.moveFactor = moveFactor;
        input = new Data.Input();

    }

    //private PhysicsComponent physics;

    @Override
    public void onUpdate(double tpf) {
        speed = (int)(tpf * moveFactor);


        input.UP = false;
        input.DOWN = false;
        input.RIGHT = false;
        input.LEFT = false;


    }

    public void up() {
        //physics.setVelocityY(-moveFactor);
        if (move != INVALID_MOVE.UP) {
            getEntity().setY(getEntity().getY() - speed);
            input.UP = true;
        }

    }

    public void down() {
        if (move != INVALID_MOVE.DOWN) {
            getEntity().setY(getEntity().getY() + speed);
            input.DOWN = true;
        }

    }

    public void left() {
        if (move != INVALID_MOVE.LEFT) {
            getEntity().setX(getEntity().getX() - speed);
            input.LEFT = true;

        }
    }

    public void right() {
        if (move != INVALID_MOVE.RIGHT) {
            getEntity().setX(getEntity().getX() + speed);
            input.RIGHT = true;
        }

    }

    public INVALID_MOVE getMove() {
        return move;
    }

    public void setMove(INVALID_MOVE move) {
        this.move = move;
    }

    public Data.Input getInput() {
        return input;
    }

}
