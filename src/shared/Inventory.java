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
    }

    public GameObject getObject(int i) {
        return objects[i];
    }

    public void fillSlot(int i, GameObject object) {
        objects[i] = object;
    }
}
