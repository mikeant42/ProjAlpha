package server;

import shared.Data;
import shared.EntityType;

import java.util.Random;

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
        Random r = new Random();

//        int[] randDir = new int[5];
//        randDir[0] = NPCState.STANDING;
//        randDir[1] = NPCState.RUNNING_FORWARD;
//        randDir[2] = NPCState.RUNNING_BACK;
//        randDir[3] = NPCState.RUNNING_LEFT;
//        randDir[4] = NPCState.RUNNING_RIGHT;

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

