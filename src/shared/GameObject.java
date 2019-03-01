package shared;

public class GameObject {
    // this will be everything from weapons to food to potions, all objects in the game that can be picked up, used, seen, and removed
    // Attributes CONSUMABLE,


    private int id;
    private int itemLevel;
    private String name;
    private String desc;
    private double x,y;
    private int uniqueGameId;

    private boolean isProjectile = false;


    // this is for bounding collision
    private int width  = 20;
    private int height = 20;

    public GameObject(int id) {
        this.id = id;
//        setOnUse(new AlphaCollisionHandler() {
//            @Override
//            public void onUse(CharacterPacket packet) {
//                System.out.println("hello");
//            }
//        });
    }

    public GameObject() {
//        setOnUse(new AlphaCollisionHandler() {
//            @Override
//            public void onUse(CharacterPacket packet) {
//                System.out.println("hello");
//            }
//        });
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public void setOnCollision() {

    }

    public void use(CharacterPacket packet) {
        System.out.println(packet.name);
    }

    public void effect(CombatObject combatObject) {}

    public boolean isProjectile() {
        return isProjectile;
    }

    public void setProjectile(boolean projectile) {
        isProjectile = projectile;
    }
}
