package server;

import server.npc.NPCBehavior;
import shared.Data;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RoamingBehavior extends NPCBehavior {
    // These are all for timing when he chooses to make a decision-
    private float elapsedTime = 0;
    private float currentTime = 0;
    private float lastTime;
    private float timeToDecide;
    private float directionTime;



    public RoamingBehavior(float x, float y) {
        super(x, y);


        lastTime = (float)getTime();
        timeToDecide = 3f;
        directionTime = 1.5f;

    }

    private int chooseRandomDirection() {
        Random r = ThreadLocalRandom.current();

        int[] randDir = new int[5];
        randDir[0] = Data.MovementState.STANDING;
        randDir[1] = Data.MovementState.RUNNING_FORWARD;
        randDir[2] = Data.MovementState.RUNNING_BACK;
        randDir[3] = Data.MovementState.RUNNING_LEFT;
        randDir[4] = Data.MovementState.RUNNING_RIGHT;

        int choice = r.nextInt(5);

        return choice;
    }


    @Override
    public void update() {
        super.update();


        currentTime = (float)getTime();
        elapsedTime += currentTime - lastTime;

        if (elapsedTime >= timeToDecide) {

            int choice = chooseRandomDirection();


            setState(choice);
            elapsedTime = 0;
        } else if (elapsedTime >= directionTime) {
            setState(Data.MovementState.STANDING);
        }

        lastTime = currentTime;


    }



}

