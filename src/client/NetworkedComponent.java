package client;

import com.almasb.fxgl.entity.component.Component;
import shared.Data;
import shared.EntityType;

public class NetworkedComponent extends Component {

    private int id;
    private ClientHandler handler;
    private boolean moveFlag = true;
    private AnimatedMovementComponent movementComponent;
    private boolean syncMovement = true;

    public NetworkedComponent(int id, ClientHandler handler) {
        this.id = id;
        this.handler = handler;

    }

    @Override
    public void onAdded() {
        if (getEntity().getType().equals(EntityType.LOCAL_PLAYER) || getEntity().getType().equals(EntityType.PLAYER))
            movementComponent = entity.getComponent(AnimatedMovementComponent.class);
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

    public void update() {
        if (ClientHandler.LOGIN_STATUS) {
//            if (getEntity().getType().equals(EntityType.PLAYER) && !getEntity().getComponent(AnimatedMovementComponent.class).isMoving()) {
//                movementComponent.setState(Data.MovementState.STANDING);
//                System.out.println("other player isnt moving");
//            }

            if (syncMovement && getEntity().getType().equals(EntityType.LOCAL_PLAYER)) {

                // This value is null if there is no physics component

                // This is null unless we have a movement component
                if (movementComponent.isMoving()) {
                    handler.sendMovement(movementComponent.getState(), getEntity().getX(),
                            getEntity().getY(), this.id);
                }

            }

        }
    }

}
