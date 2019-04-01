package shared;

public class Projectile {
    public GameObject object;
    public Network.AddProjectile projectile;
    public int width = 15;
    public int height = 15;
    public long tickCreated;

    public double velX, velY;
    public float moveSpeed = 4;

    public int damageEffect;
}
