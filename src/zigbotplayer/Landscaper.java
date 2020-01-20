package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.MapLocation;

import java.util.Arrays;

public class Landscaper extends RobotPlayer {
	/**
	 * Execute a landscaper turn.
	 * 
	 * The landscaper tries to raise goalLocation to an elevation of goalElevation.
	 * This is sort of a proof of concept RN.
	 * 
	 * @throws GameActionException
	 */
    public void landscaperTurn() throws GameActionException {
    	// STEP ONE: Decide what the goal should be (raising a particular tile to a particular elevation)
    	
    	MapLocation goalLocation = new MapLocation(38,28); // Choice of (38,28) is completely arbitrary
    	int goalElevation = 75; // Choice of 75 is completely arbirary
    	
    	// STEP TWO: Try to realize the goal of raising a particular tile to a particular elevation
    	
    	// Determine whether goalLocation is close enough to deposit dirt on
    	boolean goalAdjacent = rc.getLocation().isAdjacentTo(goalLocation);
		Direction dirToGoal = rc.getLocation().directionTo(goalLocation);
    	
    	if (goalAdjacent) { // Determine whether goalLocation is at the appropriate elevation yet
    		int elevationRightNow = rc.senseElevation(goalLocation);
    		
    		if (goalElevation == elevationRightNow) {
        		// If it's at the right elevation, hooray! Goal achieved.
    		} else if (elevationRightNow < goalElevation) { 
    			// If it's too low, try to add dirt
    			tryDepositDirt(dirToGoal);
    		} else if (elevationRightNow > goalElevation) {
    			// If it's too high, dig dirt from it
    			tryDigDirt(dirToGoal);
    		}
    		
    	} else if (!goalAdjacent) { // Move toward the goalLocation
    		// THIS COULD BE IMPROVED BY USING A SMARTER MOVEMENT STRATEGY
    		tryMove(dirToGoal);
    	}
    	
    	// Dig dirt, if possible, from an arbitrary direction
    	for (Direction dir : directions) {
        	if (rc.isReady() && rc.canDigDirt(dir)) {
        		System.out.println("Trying to dig in direction " + dir);
        		rc.digDirt(dir);
        	}
        	
        // Deposit dirt, if possible, in the SOUTH direction
        if (rc.isReady() && rc.canDepositDirt(Direction.SOUTH)) {
        	System.out.println("Trying to deposit dirt south");
        	rc.depositDirt(Direction.SOUTH);
        }
    	} 
    }
   
    public void runUnit() throws GameActionException {
        while (true) {
            landscaperTurn();
            endTurn();
        }
    }
    
    /**
     * Deposit one unit of dirt in the given direction IFF it is possible
     * 
     * @author chris
     * @param Direction dir = the direction in which you want to deposit dirt
     * @return true if the dirt is deposited, false otherwise 
     */
    boolean tryDepositDirt(Direction dir) throws GameActionException {
    	if (rc.canDepositDirt(dir)) {
    		rc.depositDirt(dir);
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Dig one unit dirt from given direction IFF it is possible
     * 
     * @author chris
     * @param Direction dir = direction from which to dig
     * @return true if dirt was dug, false otherwise
     */
    boolean tryDigDirt(Direction dir) throws GameActionException {
    	if (rc.canDigDirt(dir)) {
    		rc.digDirt(dir);
    		return true;
    	} else {
    		return false;
    	}
    	
    }
}
