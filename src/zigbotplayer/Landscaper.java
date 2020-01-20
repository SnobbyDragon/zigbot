package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.MapLocation;

import java.util.Arrays;

public class Landscaper extends RobotPlayer {
	
	Goal currentGoal = Goal.None; // initialization
	
	// goalLocation and goalElevation are part of a system that I'm phasing out; use digSite and depositSite instead
	MapLocation goalLocation = new MapLocation(38,28); // Choice of (38,28) is completely arbitrary
	int goalElevation = 75; // Choice of 75 is completely arbirary
	
	// These coordinates are completely arbitrary
	// Ultimately, we will select these coordinates intelligently so our robot builds a wall
	MapLocation depositSite = new MapLocation(26,35);
	MapLocation digSite = new MapLocation(35,37);

	/*
	 * The current goal of this unit
	 * 
	 * None = no goal
	 * Refill = go somewhere and dig more dirt
	 * Deposit = go somewhere and deposit dirt
	 */
	enum Goal {None, Refill, Deposit};
	
	
	/**
	 * Execute a landscaper turn.
	 * 
	 * The landscaper tries to raise goalLocation to an elevation of goalElevation.
	 * This is sort of a proof of concept RN.
	 * 
	 * @throws GameActionException
	 */
    public void landscaperTurn() throws GameActionException {
    	// STEP ONE: Decide what the goal should be
    	if (rc.getDirtCarrying() == RobotType.LANDSCAPER.dirtLimit) { // if full
    		currentGoal = Goal.Deposit;
    		System.out.println("I switched to deposit mode");
    	} else if (rc.getDirtCarrying() == 0) { // if empty
    		currentGoal = Goal.Refill;
    		System.out.println("I switched to refill mode");
    	}
    	
    	// STEP TWO: Follow code specific for that goal    	
    	if (currentGoal == Goal.None) {
    		System.out.println("I am an idle landscaper");
    	} else if (currentGoal == Goal.Refill) {
    		executeRefillMode();
    	} else if (currentGoal == Goal.Deposit) {
    		executeDepositMode();
    	}
    	System.out.println("I reached the end of the landscaperTurn subroutine");
    }
    
    /*
     * Try to fill the robot up with dirt from location digSite
     */
    boolean executeRefillMode() throws GameActionException {

    	if (rc.getLocation().isAdjacentTo(digSite)) { // If adjacent to digSite, dig from it!
    		// TODO
    		tryDigDirt(rc.getLocation().directionTo(digSite));
    		return true;
    	} else if (!rc.getLocation().isAdjacentTo(digSite)) { // If not adjacent to digSite, move toward it
    		Movement movement = new Movement(this, digSite); // set the destination to move toward
    		movement.step();
        	System.out.println("I am a landscaper and I just tried to take a step");
    		return true;
    	} else {
    		return false;
    	}
    	
    }
    
    /*
     * Try to dump all my dirt on location depositSite
     */
    boolean executeDepositMode() throws GameActionException {
    	System.out.println("I am executing deposit mode");
    	if (rc.getLocation().isAdjacentTo(depositSite)) { // If adjacent to digSite, dig from it!
    		// TODO
    		tryDepositDirt(rc.getLocation().directionTo(depositSite));
    		return true;
    	} else if (!rc.getLocation().isAdjacentTo(depositSite)) { // If not adjacent to digSite, move toward it
    		Movement movement = new Movement(this, depositSite); // set the destination to move in
    		System.out.println("I am entering the movement.step() subrouting");
    		movement.step();
    		System.out.println("I am out of with the movement.step() subroutine");
    		return true;
    	} else {
    		return false;
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
