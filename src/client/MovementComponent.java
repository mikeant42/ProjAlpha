package client;

import com.almasb.fxgl.entity.component.Component;
import shared.Data;

import java.util.ArrayList;
import java.util.List;

enum INVALID_MOVE {
    RIGHT, LEFT, UP, DOWN, NONE
}

public class MovementComponent extends Component {
    // note that this component is injected automatically

    private double speed;
    private int moveFactor = 245;
    private List<INVALID_MOVE> invalidMoves = new ArrayList<>();

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
        if (!invalidMoves.contains(INVALID_MOVE.UP)) {
            getEntity().setY(getEntity().getY() - speed);
            input.UP = true;
        }

    }

    public void down() {
        if (!invalidMoves.contains(INVALID_MOVE.DOWN)) {
            getEntity().setY(getEntity().getY() + speed);
            input.DOWN = true;
        }

    }

    public void left() {
        if (!invalidMoves.contains(INVALID_MOVE.LEFT)) {
            getEntity().setX(getEntity().getX() - speed);
            input.LEFT = true;

        }
    }

    public void right() {
        if (!invalidMoves.contains(INVALID_MOVE.RIGHT)) {
            getEntity().setX(getEntity().getX() + speed);
            input.RIGHT = true;
        }

    }


    public void addMove(INVALID_MOVE move) {
        invalidMoves.add(move);
    }

    public void resetMoves() {
        invalidMoves.removeAll(invalidMoves);
    }

    public Data.Input getInput() {
        return input;
    }

    public void setInput(Data.Input input) {
        this.input = input;
    }

    public boolean isMoving() {
        return !(!getInput().LEFT && !getInput().RIGHT && !getInput().UP && !getInput().DOWN);
    }
}
