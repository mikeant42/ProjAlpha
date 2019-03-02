package server;

import shared.*;

public class NPCBehavior {

    //private Network.NPCPacket data;

    private int state = Data.MovementState.STANDING;

    private int bounds; // We need this so the npc doesn't wander far from where he was spawned
    private float x,y;
    private float lastX, lastY;
    private float spawnX, spawnY;

    float speed = 0.9f;

    private boolean allowedToMove = true;

    private boolean isStatic = false;

    public NPCBehavior(float spawnX, float spawnY) {
//        data = new Network.NPCPacket();
//        data.type = entityType;

        // data.x, data.y are our spawn coords
        this.spawnX = spawnX;
        this.spawnY = spawnY;

        lastX = spawnX;
        lastY = spawnY;

        this.x = spawnX;
        this.y = spawnY;


        bounds = 50;

    }


    protected static double getTime() {
        return (double) System.nanoTime() / (double) 1000000000L;
    }

    public void update() {

        if (allowedToMove) {

            lastX = x;
            lastY = y;

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

        // This is very similar to how things are done in movementcomponent maybe combine them somehow
        if (lastX == x && lastY == y) {
            state = Data.MovementState.STANDING;
        }

    }

    public void moveRight(float speed) {
        float xr = x + speed;
        if (Math.abs(xr - spawnX) <= bounds)
            x = xr;
    }

    public void moveLeft(float speed) {
        float xr = x - speed;
        if (Math.abs(xr - spawnY) <= bounds)
            x = xr;
    }

    public void moveUp(float speed) {
        float yr = y - speed;
        if (Math.abs(yr - spawnX) <= bounds)
            y = yr;
    }

    public void moveDown(float speed) {
        float yr = y + speed;
        if (Math.abs(yr - spawnY) <= bounds)
            y = yr;
    }

    public void stopMoving() {
        allowedToMove = false;
    }

    public void resumeMoving() {
        allowedToMove = true;
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getBounds() {
        return bounds;
    }

    public void setBounds(int bounds) {
        this.bounds = bounds;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
