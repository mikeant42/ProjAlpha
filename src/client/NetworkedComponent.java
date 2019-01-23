package client;

import com.almasb.fxgl.entity.component.Component;

public class NetworkedComponent extends Component {

    private int id;

    public NetworkedComponent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
