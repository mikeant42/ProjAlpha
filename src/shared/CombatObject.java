package shared;

public class CombatObject {
    private int health;
    private int mana;

    public CombatObject() {

    }

    public CombatObject(int health, int mana) {
        this();
        this.health = health;
        this.mana = mana;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }
}
