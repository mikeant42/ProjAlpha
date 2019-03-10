package shared;


public class Inventory {
    public static final int INVENT_SIZE = 16;


    public GameObject[] objects;

    private int gold = 10;

    public Inventory() {
        objects = new GameObject[INVENT_SIZE];
    }

    public boolean addObject(GameObject object) {
        // Find first slot not filled and fill it
        for (int i = 0; i < INVENT_SIZE; i++) {
            if (objects[i] == null) {
                addObject(i, object);
                return true;
            }
        }

        return false;
    }

    public void addObject(int i, GameObject object) {
        objects[i] = object;
    }

    public GameObject getObjectSlot(int slotNumber) {
        return objects[slotNumber];
    }

    public void removeObjectFromUID(int uid) {
        for (int i = 0; i < INVENT_SIZE; i++) {
            if (objects[i] != null) {
                if (objects[i].getUniqueGameId() == uid) {
                    objects[i] = null; // NOT TESTED!!!!!!!!!!!!!!!!
                    return;
                }
            }
        }
    }

    public void removeObjectFromSlot(int slotNumber) {
        objects[slotNumber] = null;
    }

    public void fillSlot(int i, GameObject object) {
        objects[i] = object;
    }

    public int getGold() {
        return gold;
    }


}
