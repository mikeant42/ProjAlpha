package client;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import shared.Data;

public class MovementComponent extends Component {
    // note that this component is injected automatically

    private double speed;
    private int moveFactor = 1;
    private boolean collidingX = false;
    private boolean collidingY = false;

    private Data.Input input;

    public MovementComponent(int moveFactor) {
        this.moveFactor = moveFactor;
        input = new Data.Input();

    }

    //private PhysicsComponent physics;

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * 60;


        input.UP = false;
        input.DOWN = false;
        input.RIGHT = false;
        input.LEFT = false;
    }

    public void up() {
        //physics.setVelocityY(-moveFactor);
        if (!collidingY) {
            getEntity().setY(getEntity().getY() - moveFactor);
        }
        input.UP = true;
    }

    public void down() {
        if (!collidingY) {
            getEntity().setY(getEntity().getY() + moveFactor);
        }
        input.DOWN = true;
    }

    public void left() {
        if (!collidingX) {
            getEntity().setX(getEntity().getX() - moveFactor);
        }
        input.LEFT = true;
    }

    public void right() {
        if (!collidingX) {
            getEntity().setX(getEntity().getX() + moveFactor);
        }
        input.RIGHT = true;
    }

    public void setCollidingX(boolean collsi) {
        collidingX = collsi;
    }

    public boolean isCollidingX() {
        return collidingX;
    }

    public boolean isCollidingY() {
        return collidingY;
    }

    public void setCollidingY(boolean collidingY) {
        this.collidingY = collidingY;
    }

    public Data.Input getInput() {
        return input;
    }

}
