package client.ui;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.ui.UIController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import shared.GameObject;

import java.util.HashMap;
import java.util.HashSet;

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

    private int grid = 4; // 4x4, 3x3


    // There needs to be corresponding GameObjects
    private ImageView[] inventory = new ImageView[grid*grid];


    private int selected = 200; // this is out of range

    private int tick;

    private ColorAdjust adjust;

    private boolean canSelect = true;

    public MainPanelController() {

    }

    private void create() {
        for (int i = 0; i < inventory.length; i++) {
            ImageView view = inventory[i];

            view.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

                @Override
                public void handle(ContextMenuEvent event) {
                    if (view.getBoundsInLocal().contains(FXGL.getApp().getInput().getMouseXUI(), FXGL.getApp().getInput().getMouseYUI())) {
                        createContextMenu(tick).show(view, event.getScreenX(), event.getScreenY());
                    }
                }
            });
        }

    }

    private ContextMenu createContextMenu(int id) {

        ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = new MenuItem("Drop");
        item1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // First remove the image from the inventory grid
                ImageView piece = inventory[id];
                FXGL.getApp().getGameScene().removeUINode(piece);
            }
        });
        MenuItem item2 = new MenuItem("Use");
        item2.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

            }
        });

        // Add MenuItem to ContextMenu
        contextMenu.getItems().addAll(item1, item2);

        return contextMenu;

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
                ImageView defaul = new ImageView(FXGL.getAssetLoader().loadImage("ui/box.png"));
                defaul.setPreserveRatio(true);


                tick++;
                int internal = i+j;
                inventory[internal] = defaul;
                defaul.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (selected == internal) {
                            defaul.setEffect(new ColorAdjust());
                            canSelect = true;
                            selected = 200;
                        } else if (canSelect) {
                            defaul.setEffect(adjust);
                            selected = internal;
                            canSelect = false;
                        }
                    }
                });


                defaul.setFitHeight(invent.getPrefHeight()/grid);
                defaul.setFitWidth(invent.getPrefWidth()/grid);
                invent.add(defaul, i, j);

            }
        }


        //create();
    }


    public void dropItem() {
        if (selected <= grid*grid) {
            ImageView view = inventory[selected];
            if (view != null) {
                FXGL.getApp().getGameScene().removeUINode(view);
                System.out.println(selected + "removing");
            } else {
                System.out.println("selection is null");
            }
        }

    }

}
