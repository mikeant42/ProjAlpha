package client;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;

public class NetworkedComponent extends Component {

    private int id;
    private ClientHandler handler;
    private boolean moveFlag = true;
    private MovementComponent movementComponent;
    private boolean syncMovement = true;

    public NetworkedComponent(int id, ClientHandler handler) {
        this.id = id;
        this.handler = handler;

    }

    public boolean isSyncMovement() {
        return syncMovement;
    }

    public void setSyncMovement(boolean usePhysics) {
        this.syncMovement = usePhysics;
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
        if (syncMovement) {

            // This value is null if there is no physics component

            // This is null unless we have a movement component
            movementComponent = entity.getComponent(MovementComponent.class);
            handler.sendMovement(getEntity().getX(), getEntity().getY(), this.id);

        }
//        handler.sendMovement((int) getEntity().getX(), (int) getEntity().getY(), this.id);
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
