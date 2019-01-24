package client;

import com.almasb.fxgl.entity.component.Component;

public class MovementComponent extends Component {
    // note that this component is injected automatically

    private double speed;
    private int moveFactor = 1;
    private boolean moving = false;

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * 60;
        moving = false;
    }

    public void up() {
        getEntity().setY(getEntity().getY()-moveFactor);
        moving = true;
    }

    public void down() {
        getEntity().setY(getEntity().getY()+moveFactor);
        moving = true;
    }

    public void left() {
        getEntity().setX(getEntity().getX()-moveFactor);
        moving = true;
    }

    public void right() {
        getEntity().setX(getEntity().getX()+moveFactor);
        moving = true;
    }

}
