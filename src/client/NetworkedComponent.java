package client;

import com.almasb.fxgl.entity.component.Component;

public class NetworkedComponent extends Component {

    private int id;
    private ClientHandler handler;
    private boolean moveFlag = true;

    public NetworkedComponent(int id, ClientHandler handler) {
        this.id = id;
        this.handler = handler;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isMoveFlag() {
        return moveFlag;
    }

    public void setMoveFlag(boolean moveFlag) {
        this.moveFlag = moveFlag;
    }

    @Override
    public void onUpdate(double dtf) {
        handler.sendMovement((int) getEntity().getX(), (int) getEntity().getY(), this.id);
//        if (getEntity().getType().equals(EntityType.PLAYER)) {
//            boolean moving = getEntity().getComponent(MovementComponent.class).isMoving();
//            if (moving) {
//                handler.sendMovement((int) getEntity().getX(), (int) getEntity().getY(), this.id);
//            }
//        } else {
//            if (moveFlag) {
//                handler.sendMovement((int) getEntity().getX(), (int) getEntity().getY(), this.id);
//            }
//        }
    }

}
