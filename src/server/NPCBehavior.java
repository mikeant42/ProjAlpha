package server;

import shared.CharacterPacket;
import shared.EntityType;
import shared.Network;

public class NPCBehavior {

    private Network.NPCPacket data;

    private int state = 0;

    private Network.UpdateNPC updateNPC;

    private int bounds; // We need this so the npc doesn't wander far from where he was spawned
    private float x,y;


    public NPCBehavior(EntityType entityType, int x, int y) {
        data = new Network.NPCPacket();
        data.type = entityType;

        // data.x, data.y are our spawn coords
        data.x = x;
        data.y = y;

        this.x = x;
        this.y = y;

        updateNPC = new Network.UpdateNPC();

        bounds = 50;

    }


    protected static double getTime() {
        return (double) System.nanoTime() / (double) 1000000000L;
    }

    public void update() {
        float speed = 0.9f;


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
        float xr = x + speed;
        if (Math.abs(x - data.x) <= bounds)
            x = xr;
    }

    public void moveLeft(float speed) {
        float xr = x - speed;
        if (Math.abs(x - data.x) <= bounds)
            x = xr;
    }

    public void moveUp(float speed) {
        float yr = y - speed;
        if (Math.abs(y - data.y) <= bounds)
            y = yr;
    }

    public void moveDown(float speed) {
        float yr = y + speed;
        if (Math.abs(y - data.y) <= bounds)
            y = yr;
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
        updateNPC.x =  x;
        updateNPC.y =  y;
        updateNPC.id = data.id;

        return updateNPC;
    }

    public int getBounds() {
        return bounds;
    }

    public void setBounds(int bounds) {
        this.bounds = bounds;
    }

}