package client;

import client.render.OverlayTextComponent;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.util.Duration;

public class ExpireComponent extends Component {
    private double duration = 1;
    public ExpireComponent(double dur) {
        duration = dur;
    }

    @Override
    public void onAdded() {
        FXGL.getApp().getMasterTimer().runOnceAfter(() -> {
            if (entity != null && entity.getWorld() != null)
                entity.removeFromWorld();
        }, Duration.seconds(duration));
    }


}
