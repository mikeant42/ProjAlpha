package client;

import com.almasb.fxgl.entity.component.Component;
import shared.Data;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;

public class MovementComponent extends Component {

    public enum INVALID_MOVE {
        RIGHT, LEFT, UP, DOWN, NONE
    }

    private double speed;
    private int moveFactor = 75;
    private List<INVALID_MOVE> invalidMoves = new ArrayList<>();

    private int state = Data.MovementState.STANDING;
    private Point2D previousPos = new Point2D(0,0);

    public MovementComponent() {
        this.moveFactor = moveFactor;

    }

    //private PhysicsComponent physics;

    @Override
    public void onUpdate(double tpf) {
        if (getEntity().getPosition().equals(previousPos)) { // If we haven't moved, we must be standing
            state = Data.MovementState.STANDING;
        }
        previousPos = getEntity().getPosition();

        speed = (int)(tpf * moveFactor);
    }

    public void up() {
        //physics.setVelocityY(-moveFactor);
        if (!invalidMoves.contains(INVALID_MOVE.UP)) {
            getEntity().setY(getEntity().getY() - speed);
            state = Data.MovementState.RUNNING_FORWARD;
        }

    }

    public void down() {
        if (!invalidMoves.contains(INVALID_MOVE.DOWN)) {
            getEntity().setY(getEntity().getY() + speed);
            state = Data.MovementState.RUNNING_BACK;
        }

    }

    public void left() {
        if (!invalidMoves.contains(INVALID_MOVE.LEFT)) {
            getEntity().setX(getEntity().getX() - speed);
            state = Data.MovementState.RUNNING_LEFT;

        }
    }

    public void right() {
        if (!invalidMoves.contains(INVALID_MOVE.RIGHT)) {
            getEntity().setX(getEntity().getX() + speed);
            state = Data.MovementState.RUNNING_RIGHT;
        }

    }


    public void addMove(INVALID_MOVE move) {
        invalidMoves.add(move);
    }

    public void resetMoves() {
        invalidMoves.removeAll(invalidMoves);
    }

    public int getState() {
        return state;
    }

    public void setState(int input) {
        this.state = input;
    }

    public boolean isMoving() {
        return !(state == Data.MovementState.STANDING);
    }

}
