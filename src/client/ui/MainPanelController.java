package client.ui;

import client.ClientHandler;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.ui.UIController;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import shared.GameObject;
import shared.Inventory;

public class MainPanelController implements UIController {

    @FXML
    private ImageView slot1;

    @FXML
    private ImageView slot2;

    @FXML
    private ImageView slot3;

    @FXML
    private GridPane invent;

    @FXML
    private Button drop;

    @FXML
    private Button use;

    private int grid = (int)Math.sqrt(Inventory.INVENT_SIZE); // 4x4, 3x3


    // There needs to be corresponding GameObjects
    private ImageView[] inventory = new ImageView[grid*grid];

    private Inventory userInvent;


    private int selected = 200; // this is out of range

    private int tick;

    private ColorAdjust adjust;

    private boolean canSelect = true;

    private int internal = -1;

    private ClientHandler handler;

    private Image defaultImage = FXGL.getAssetLoader().loadImage("ui/box.png");

    public MainPanelController() {

    }

    public void create(ClientHandler handler) {
        this.userInvent = handler.getCharacterPacket().inventory;
        this.handler = handler;

        for (int i = 0; i < Inventory.INVENT_SIZE; i++) {
            GameObject object = userInvent.getObjectSlot(i);
            if (object != null) {
                addItem(userInvent.getObjectSlot(i));
            }
        }
    }

    public void addItem(GameObject object) {
        for (int i = 0; i < Inventory.INVENT_SIZE; i++) {
            if (!slotTaken(i)) {
                inventory[i].setImage(FXGL.getAssetLoader().loadImage("objects/" + object.getName() + ".png"));
                userInvent.addObject(i, object);
                return;
            }
        }
    }


    @Override
    public void init() {
        adjust = new ColorAdjust();
        adjust.setBrightness(0.7);
        adjust.setSaturation(0.5);
//        slot1.setImage(FXGL.getAssetLoader().loadImage("ui/sword.png"));
//        slot2.setImage(FXGL.getAssetLoader().loadImage("ui/sword.png"));
//        slot3.setImage(FXGL.getAssetLoader().loadImage("ui/sword.png"));



        for (int i = 0; i < grid; i++) {
            for (int j = 0; j < grid; j++) {
                if (internal <= Inventory.INVENT_SIZE) {
                    ImageView defaul = new ImageView(defaultImage);
                    defaul.setPreserveRatio(true);

                    internal++;
                    defaul.setUserData(internal);
                    System.out.println(internal);
                    inventory[internal] = defaul;
                    defaul.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            if (selected == (int)defaul.getUserData()) {
                                defaul.setEffect(new ColorAdjust());
                                canSelect = true;
                                selected = 200;
                            } else if (canSelect) {
                                defaul.setEffect(adjust);
                                selected = (int)defaul.getUserData();
                                canSelect = false;
                                System.out.println("selected " + selected);
                            }
                        }
                    });


                    defaul.setFitHeight(invent.getPrefHeight() / grid);
                    defaul.setFitWidth(invent.getPrefWidth() / grid);
                    invent.add(defaul, i, j);

                }
            }
        }


        //create();
    }


    public void dropItem() {
        if (selected <= grid*grid) { // if its a valid selection
            if (slotTaken(selected) && userInvent != null) {
                userInvent.removeObjectFromSlot(selected);
                //System.out.println(userInvent.getObjectSlot(selected).getName());
                System.out.println(userInvent.getObjectSlot(selected) == null);
                inventory[selected].setImage(FXGL.getAssetLoader().loadImage("ui/box.png"));
                System.out.println(selected + "removing");
            } else {
                System.err.println("selection is null");
            }
        }

    }

    public void useItem() {
        if (selected <= grid*grid) { // if its a valid selection
            if (slotTaken(selected) && userInvent != null) {
                userInvent.getObjectSlot(selected).use(handler.getCharacterPacket());
                System.out.println(userInvent.getObjectSlot(selected).getUniqueGameId() + " is being used");
            } else {
                System.err.println("selection is null");
            }
        }
    }

    public boolean slotTaken(int slot) {
        return !(inventory[slot].getImage() == defaultImage);
    }

}
