package client;

import com.almasb.fxgl.entity.component.Component;
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
        if (syncMovement && getEntity().getType().equals(EntityType.LOCAL_PLAYER)) {

            // This value is null if there is no physics component

            // This is null unless we have a movement component
            //if (getEntity().getComponent(AnimatedMovementComponent.class).isMoving()) {
                movementComponent = entity.getComponent(AnimatedMovementComponent.class);
                handler.sendMovement(getEntity().getComponent(AnimatedMovementComponent.class).getInput(), getEntity().getX(),
                        getEntity().getY(), this.id);
            //}

        }

    }

}
