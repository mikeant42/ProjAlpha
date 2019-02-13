package client;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.view.EntityView;
import com.almasb.fxgl.scene.GameScene;
import com.almasb.fxgl.scene.Viewport;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;

import static client.Screen.TILESIZE;

public class OverlayTextComponent extends Component {

    private String text;
    private int offsetX = 70;
    private int offsetY = 30;
    private Text textPixels;
    private Point2D pos;
    private Viewport viewport;

    public OverlayTextComponent(String text) {
        this.text = text;
    }

    @Override
    public void onAdded() {
        textPixels = new Text(text);
        FXGL.getApp().getGameScene().addUINode(textPixels);

        viewport = new Viewport(FXGL.getAppWidth(), FXGL.getAppHeight());
        viewport.bindToEntity(getEntity(), FXGL.getAppWidth()/2 - TILESIZE/2, FXGL.getAppHeight()/2 - TILESIZE/2);
        viewport.setZoom(1.2);


        pos = getTextPos();

        textPixels.setTranslateX(pos.getX());
        textPixels.setTranslateY(pos.getY());


        //Bindings.createIntegerBinding(() -> textPixels.setTranslateX(pos.getX()), )

    }

    @Override
    public void onRemoved() {
        FXGL.getApp().getGameScene().removeUINode(textPixels);
    }

    @Override
    public void onUpdate(double dtf) {
        viewport.onUpdate(dtf);

        pos = getTextPos();

        textPixels.setTranslateX(pos.getX());
        textPixels.setTranslateY(pos.getY());

        System.out.println(FXGL.getApp().getGameScene().getViewport().getOrigin());
        System.out.println(FXGL.getApp().getGameScene().getViewport().getX() +" " + FXGL.getApp().getGameScene().getViewport().getY());

    }

    private Point2D getTextPos() {
        double x = getEntity().getX();
        double y = getEntity().getY();

        return new Point2D(x,y).subtract(FXGL.getApp().getGameScene().getViewport().getOrigin())
            .multiply(FXGL.getAppWidth()/FXGL.getAppHeight()).add(new Point2D(offsetX, offsetY));
    }
}
