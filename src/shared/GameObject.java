package shared;

public class GameObject {
    // this will be everything from weapons to food to potions, all objects in the game that can be picked up, used, seen, and removed
    // Attributes CONSUMABLE,


    private int id;
    private int itemLevel;
    private String name;
    private String desc;
    private float x,y;
    private int uniqueGameId;


    private int minX = 50;
    private int maxX = 85;

    private int minY = 50;
    private int maxY = 90;

    public GameObject(int id) {
        this.id = id;
    }

    public GameObject() {}

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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getUniqueGameId() {
        return uniqueGameId;
    }

    public void setUniqueGameId(int uniqueGameId) {
        this.uniqueGameId = uniqueGameId;
    }

    public int getId() {
        return id;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }
}
