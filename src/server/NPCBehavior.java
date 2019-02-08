package server;

import shared.CharacterPacket;
import shared.EntityType;
import shared.Network;

public class NPCBehavior {

    private Network.NPCPacket data;

    private int state = 0;

    private Network.UpdateNPC updateNPC;

    private int bounds; // We need this so the npc doesn't wander far from where he was spawned
    private int spawnX, spawnY;


    public NPCBehavior(EntityType entityType, int x, int y) {
        data = new Network.NPCPacket();
        data.type = entityType;
        data.x = x;
        data.y = y;

        spawnX = x;
        spawnY = y;

        updateNPC = new Network.UpdateNPC();

        bounds = 50;

    }


    protected static double getTime() {
        return (double) System.nanoTime() / (double) 1000000000L;
    }

    public void update() {
        float speed = 20f * 0.015f;

        switch (state) {
            case 1:
                moveUp(speed);
                break;
            case 2:
                moveRight(speed);
                break;
            case 3:
                moveLeft(speed);
                break;
            case 4:
                moveDown(speed);
                break;
        }

    }

    public void moveRight(float speed) {
        //int x = data.x + (int)speed;
        //if (Math.abs(x - spawnX) <= bounds)
            data.x -= -speed;
    }

    public void moveLeft(float speed) {
       // int x = data.x - (int)speed;
        //if (Math.abs(x - spawnX) <= bounds)
            data.x -= speed;
    }

    public void moveUp(float speed) {
        //nt y = data.y + (int)speed;
        //if (Math.abs(y - spawnY) <= bounds)
            data.y -= speed;
    }

    public void moveDown(float speed) {
       // int y = data.y - (int)speed;
       // if (Math.abs(y - spawnY) <= bounds)
            data.y -= speed;
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }


    public Network.NPCPacket getData() {
        return data;
    }

    public void onCollisionWithPlayer(CharacterPacket packet) {
    }

    public Network.UpdateNPC formUpdate() {
        updateNPC.x =  data.x;
        updateNPC.y =  data.y;
        updateNPC.id = data.id;

        return updateNPC;
    }

    public int getBounds() {
        return bounds;
    }

    public void setBounds(int bounds) {
        this.bounds = bounds;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }
}
