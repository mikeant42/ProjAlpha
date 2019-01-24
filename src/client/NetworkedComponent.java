package client;

import com.almasb.fxgl.entity.component.Component;

public class NetworkedComponent extends Component {

    private int id;
    private ClientHandler handler;

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

    @Override
    public void onUpdate(double dtf) {
        handler.sendMovement((int)getEntity().getX(), (int)getEntity().getY(), this.id);
        System.out.println("working");
    }

}
