package client.render;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import shared.CombatObject;



public class CombatComponent extends Component {
    private CombatObject combatObject;
    private boolean shieldOn;
    private ImageView shield;
    private int offset = 24;

    public CombatComponent(CombatObject object) {
        combatObject = object;
    }

    @Override
    public void onAdded() {

    }

    public void updateShield() {
        switch (combatObject.getShield()) {
            case -1:
                if (shieldOn) {
                    getEntity().getView().removeNode(shield);
                }
                shieldOn = false;
                return;
            case 0:
                if (!shieldOn) {
                    shield = new ImageView(FXGL.getAssetLoader().loadImage("spell/gravityshield.png"));
                    getEntity().getView().addNode(shield);
                    shield.setScaleX(0.7);
                    shield.setScaleY(0.7);
                    shield.setLayoutX(-shield.getImage().getWidth() / 2 + offset);
                    shield.setLayoutY(-shield.getImage().getHeight() / 2 + offset + 8);
                    shieldOn = true;
                }
                break;
        }

//        Pane pane = new Pane();
//        pane.getChildren().add(new ImageView(image));

    }

    public CombatObject getCombatObject() {
        return combatObject;
    }

    public void setCombatObject(CombatObject combatObject) {
        this.combatObject = combatObject;
    }
}
