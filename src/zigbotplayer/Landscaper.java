package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import battlecode.common.MapLocation;

import java.util.ArrayList;
import java.util.List;

public class Landscaper extends RobotPlayer {

    Goal currentGoal = Goal.None; // initialization

    // goalLocation and goalElevation are part of a system that I'm phasing out; use digSite and depositSite instead
    MapLocation goalLocation = new MapLocation(38, 28); // Choice of (38,28) is completely arbitrary
    int goalElevation = 75; // Choice of 75 is completely arbirary

    // These coordinates are completely arbitrary
    // Ultimately, we will select these coordinates intelligently so our robot builds a wall
    List<MapLocation> depositSites = new ArrayList<MapLocation>();
    List<MapLocation> digSites = new ArrayList<MapLocation>();

    void updateHQLoc() {
        System.out.println("UPDATED YAY");
        for (Direction d : Movement.directions) {
            depositSites.add(HQLocation.add(d));
            digSites.add(HQLocation.add(d).add(d));
        }
    }

    /*
     * The current goal of this unit
     *
     * None = no goal
     * Refill = go somewhere and dig more dirt
     * Deposit = go somewhere and deposit dirt
     */
    enum Goal {None, Refill, Deposit}

    ;


    /**
     * Execute a landscaper turn.
     * <p>
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
        MapLocation digSite = digSites.get(0);
        int minDist = box(digSites.get(0), rc.getLocation());
        for (int i = 0; i < digSites.size(); i++) {
            int d= box(digSites.get(i), rc.getLocation());
            if (d< minDist && 0 < d) {
                digSite = digSites.get(i);
            }
        }
        if (minDist==1) { // If adjacent to digSite, dig from it!
            tryDigDirt(rc.getLocation().directionTo(digSite));
        } else { // If not adjacent to digSite, move toward it
            Movement movement = new Movement(this, digSite); // set the destination to move toward
            movement.step();
            System.out.println("I am a landscaper and I just tried to take a step");
        }
        return true;

    }

    /*
     * Try to dump all my dirt on location depositSite
     */
    boolean executeDepositMode() throws GameActionException {
        System.out.println("I am executing deposit mode");
        MapLocation depositSite = depositSites.get(0);
        int minDist = box(depositSites.get(0), rc.getLocation());
        for (int i = 0; i < depositSites.size(); i++) {
            int d= box(depositSites.get(i), rc.getLocation());
            if (d< minDist && 0 < d) {
                depositSite = depositSites.get(i);
            }
        }
        if (minDist == 1) { // If adjacent to digSite, dig from it!
            // TODO
            tryDepositDirt(rc.getLocation().directionTo(depositSite));
        } else { // If not adjacent to digSite, move toward it
            Movement movement = new Movement(this, depositSite); // set the destination to move in
            movement.step();
        }
        return true;
    }

    public void runUnit() throws GameActionException {
        while (true) {
            endTurn();
            landscaperTurn();
        }
    }

    /**
     * Deposit one unit of dirt in the given direction IFF it is possible
     *
     * @param Direction dir = the direction in which you want to deposit dirt
     * @return true if the dirt is deposited, false otherwise
     * @author chris
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
     * @param Direction dir = direction from which to dig
     * @return true if dirt was dug, false otherwise
     * @author chris
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
