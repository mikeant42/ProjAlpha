package client.render;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.ui.Position;
import com.almasb.fxgl.ui.ProgressBar;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import shared.Data;

public class OverlayHealthComponent extends Component {

    private DoubleProperty value;

    private DoubleProperty transX = new SimpleDoubleProperty(0);
    private DoubleProperty transY = new SimpleDoubleProperty(0);

    private int healthWidth = 40;
    private Color color;
    private Pane pane;
    private ProgressBar hpBar;

    public OverlayHealthComponent(Color col) {
        value = new SimpleDoubleProperty();
        color = col;
    }

    @Override
    public void onAdded() {
        hpBar = new ProgressBar();
        hpBar.setMinValue(0);
        hpBar.setMaxValue(Data.PlayerConstants.MAX_HEALTH);

        //value.bind(getEntity().getComponent(CombatComponent.class).getCombatObject().getHealth());

        hpBar.setCurrentValue(value.doubleValue());

        hpBar.setWidth(healthWidth);
        hpBar.setLabelVisible(false);
        hpBar.setLabelPosition(Position.BOTTOM);
        hpBar.setFill(color);

        Point2D pos = getPos();

        transX.set(pos.getX());
        transY.set(pos.getY());

        pane = new Pane();


        pane.translateXProperty().bind(transX);
        pane.translateYProperty().bind(transY);

        pane.getChildren().add(hpBar);
        getEntity().getView().addNode(pane);
        pane.setVisible(false);
    }

    @Override
    public void onUpdate(double dtf) {
        if (getEntity().getComponent(CombatComponent.class).getCombatObject().getHealth() < Data.PlayerConstants.MAX_HEALTH) {
            pane.setVisible(true);
            hpBar.setCurrentValue(getEntity().getComponent(CombatComponent.class).getCombatObject().getHealth());

        } else {
            pane.setVisible(false);
        }
    }

    private Point2D getPos() {
        double x = getEntity().getX();
        double y = getEntity().getY();

        return getEntity().getView().parentToLocal(x,y);
    }



}
