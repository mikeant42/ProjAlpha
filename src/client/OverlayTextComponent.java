package client;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.view.EntityView;
import com.almasb.fxgl.scene.FXGLMenu;
import com.almasb.fxgl.scene.GameScene;
import com.almasb.fxgl.scene.Viewport;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static client.Screen.TILESIZE;
import static javafx.application.Application.getUserAgentStylesheet;

public class OverlayTextComponent extends Component {

    private String text;
    private int offsetX = 10;
    private int offsetY = 10;
    private Text textPixels;
    private Point2D pos;

    private int timed = 0;

    private DoubleProperty transX = new SimpleDoubleProperty(0);
    private DoubleProperty transY = new SimpleDoubleProperty(0);

    public OverlayTextComponent(String text) {
        this.text = text;
    }

    public OverlayTextComponent(String text, int timed) {
        this(text);
        this.timed = timed;
    }

    @Override
    public void onAdded() {
        textPixels = new Text(text);

        offsetX = text.length() / 2;
        offsetY = 0;

        getEntity().getView().addNode(textPixels);

        pos = getTextPos();
        transX.set(pos.getX());
        transY.set(pos.getY());

        textPixels.translateXProperty().bind(transX);
        textPixels.translateYProperty().bind(transY);

        if (timed != 0) {
            FXGL.getApp().getMasterTimer().runOnceAfter(() -> {
                getEntity().removeComponent(OverlayTextComponent.class);
            }, Duration.seconds(timed));
        }

    }

    @Override
    public void onRemoved() {
        getEntity().getView().removeNode(textPixels);
    }

    private Point2D getTextPos() {
        double x = getEntity().getX();
        double y = getEntity().getY();

        return getEntity().getView().parentToLocal(x,y).add(offsetX, offsetY);
    }
}
