package shared;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    public static final int INVENT_SIZE = 16;


    public GameObject[] objects;

    public Inventory() {
        objects = new GameObject[INVENT_SIZE];
    }

    public void addObject(GameObject object) {
        // Find first slot not filled and fill it
        for (int i = 0; i < INVENT_SIZE; i++) {
            if (objects[i] == null) {
                objects[i] = object;
            }
        }
    }

    public GameObject getObject(int i) {
        return objects[i];
    }

    public void removeObject(int uid) {
        for (int i = 0; i < INVENT_SIZE; i++) {
            if (objects[i].getUniqueGameId() == uid) {
                objects[i] = null; // NOT TESTED!!!!!!!!!!!!!!!!
            }
        }
    }

    public void fillSlot(int i, GameObject object) {
        objects[i] = object;
    }
}
