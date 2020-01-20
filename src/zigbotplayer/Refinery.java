package zigbotplayer;

import battlecode.common.*;

import java.util.Arrays;

public class Refinery extends RobotPlayer {
    int built = 0;

    void RefineryTurn() throws GameActionException {
    	// The refinery seems to refine soup PASSIVELY
    	
    	// It refines min(RobotType.REFINERY.maxSoupProduced, rc.getSoupCarrying()) soup per turn.
    	// That is, it refines up to 20 soup per turn. (The discord said RobotType.REFINERY.maxSoupProduced = 20)
    	
    }

    public void runUnit() throws GameActionException {
        while (true) {
            RefineryTurn();
            endTurn();
            System.out.println(Arrays.toString(rc.getBlock(rc.getRoundNum() - 1)));
        }
    }

}
