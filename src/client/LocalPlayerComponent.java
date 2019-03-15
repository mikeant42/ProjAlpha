package client;

import client.render.AnimatedMovementComponent;
import com.almasb.fxgl.entity.component.Component;
import shared.Data;
import shared.EntityType;

public class LocalPlayerComponent extends Component {

    private int id;
    private ClientHandler handler;
    private boolean moveFlag = true;
    private client.render.AnimatedMovementComponent movementComponent;
    private boolean syncMovement = true;

    public LocalPlayerComponent(int id, ClientHandler handler) {
        this.id = id;
        this.handler = handler;

    }

    @Override
    public void onAdded() {
        movementComponent = entity.getComponent(AnimatedMovementComponent.class);
    }

    public void update() {
        if (movementComponent.isMoving()) {
            handler.sendMovement(movementComponent.getState(), getEntity().getX(),
                    getEntity().getY(), this.id);
        } else {
            movementComponent.setState(Data.MovementState.STANDING);
        }

    }

}
