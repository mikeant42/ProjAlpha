package client.ui;

import client.ClientHandler;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.ui.UIController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import shared.GameObject;
import shared.Inventory;
import shared.Network;

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

    @FXML
    private TextField chatBox;

    @FXML
    private Label goldLabel;

    @FXML
    private ListView<String> chatView;

    @FXML
    private ImageView inventoryBackground;

    private ObservableList<String> chatLog = FXCollections.observableArrayList();

    private int grid = (int)Math.sqrt(Inventory.INVENT_SIZE); // 4x4, 3x3


    // There needs to be corresponding GameObjects
    private ImageView[] inventory = new ImageView[grid*grid];


    private int selected = 200; // this is out of range

    private int tick;

    private ColorAdjust adjust;

    private boolean canSelect = true;

    private int internal = -1;

    private ClientHandler handler;

    private Image defaultImage = FXGL.getAssetLoader().loadImage("ui/default.png");

    public MainPanelController() {

    }

    public void create(ClientHandler handler) {
        this.handler = handler;
        Inventory userInvent = handler.getCharacterPacket().inventory;

        for (int i = 0; i < Inventory.INVENT_SIZE; i++) {
            GameObject object = userInvent.getObjectSlot(i);
            if (object != null) {
                addItem(userInvent.getObjectSlot(i));
            }
        }

        goldLabel.setText("" + userInvent.getGold());

        inventoryBackground.setImage(FXGL.getAssetLoader().loadImage("ui/invent.png"));
        //inventoryBackground.setRotate(90);

    }

    public void addItem(GameObject object) {
        for (int i = 0; i < Inventory.INVENT_SIZE; i++) {
            if (!slotTaken(i)) {
                addItem(object, i);
                return;
            }
        }
    }

    private void addItem(GameObject object, int i) {
        inventory[i].setImage(FXGL.getAssetLoader().loadImage("objects/" + object.getName() + ".png"));
        handler.getCharacterPacket().inventory.addObject(i, object);
    }

    public void dropAll() {

        // loop through both at once ;)
        for (int i = 0; i < Inventory.INVENT_SIZE; i++) {
            GameObject object = handler.getCharacterPacket().inventory.objects[i];
            if (slotTaken(i)) {
                drop(i);
            }
        }

    }


    @Override
    public void init() {
        adjust = new ColorAdjust();
        adjust.setBrightness(0.7);
        adjust.setSaturation(0.5);

        chatView.setOrientation(Orientation.VERTICAL);

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
            if (slotTaken(selected) && handler.getCharacterPacket().inventory != null) {
                drop(selected);
            } else {
                System.err.println("selection is null");
            }
        }

        handler.sendInventoryUpdate();

    }

    private void drop(int selected) {
        handler.getCharacterPacket().inventory.removeObjectFromSlot(selected);
        //System.out.println(userInvent.getObjectSlot(selected).getName());
        System.out.println(handler.getCharacterPacket().inventory.getObjectSlot(selected) == null);
        inventory[selected].setImage(defaultImage);
        System.out.println(selected + " removing from inventory");
    }

    public void useItem() {
        if (selected <= grid*grid) { // if its a valid selection
            if (slotTaken(selected) && handler.getCharacterPacket().inventory != null) {
                handler.getCharacterPacket().inventory.getObjectSlot(selected).use(handler.getCharacterPacket());
                handler.sendCombatObjectUpdate();
                System.out.println(handler.getCharacterPacket().inventory.getObjectSlot(selected).getUniqueGameId() + " is being used");
                // if it heals update the server on new character health

                drop(selected);
            } else {
                System.err.println("selection is null");
            }
        }

        handler.sendInventoryUpdate();
    }

    public void sendChat() {
        handler.sendChat(chatBox.getText().trim());
        chatBox.clear();
        chatBox.setFocusTraversable(false);
    }

    public void addChat(String message) {
        chatLog.add(message);
        chatView.setItems(chatLog);
    }

    public boolean slotTaken(int slot) {
        return !(inventory[slot].getImage() == defaultImage);
    }

}
