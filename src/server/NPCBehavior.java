package server;

import shared.EntityType;
import shared.Network;

public class NPCBehavior {

    private Network.NPCPacket data;

    private int state = 0;

    private Network.UpdateNPC updateNPC;



    public NPCBehavior(EntityType entityType, int x, int y) {
        data = new Network.NPCPacket();
        data.type = entityType;
        data.x = x;
        data.y = y;

        updateNPC = new Network.UpdateNPC();

    }


    protected static double getTime() {
        return (double) System.nanoTime() / (double) 1000000000L;
    }

    public void update() {
        float speed = 0.06f;
        switch (state) {
            case 1:
                data.y += speed;
                break;
            case 2:
                data.x += speed;
                break;
            case 3:
                data.x -= speed;
                break;
            case 4:
                data.y -= speed;
                break;
        }
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

    public void onCollisionWithPlayer() {
    }

    public Network.UpdateNPC formUpdate() {
        updateNPC.x = getData().x;
        updateNPC.y = getData().y;
        updateNPC.id = getData().id;

        return updateNPC;
    }
}
