package shared;

public class GameObject {

    public class Food {
        public static final int FISH = 5001;
        public static final int MEAT = 5002;
    }

    public class Weapon {

    }

    public class ItemLevel {
        public static final int COMMON = 1001;
        public static final int UNIQUE = 1002;
        public static final int EPIC   = 1003;
    }

    // this will be everything from weapons to food to potions, all objects in the game that can be picked up, used, seen, and removed
    // Attributes CONSUMABLE,

    private int id;
    private int itemLevel;
    private String name;
    private float x,y;

    public GameObject(int id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    private Network.GameObjectPacket getPacket() {
        Network.GameObjectPacket packet = new Network.GameObjectPacket();
        packet.x = x;
        packet.y = y;
        packet.id = id;
        return packet;
    }
}
