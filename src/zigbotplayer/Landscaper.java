package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import battlecode.common.MapLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Landscaper extends RobotPlayer {

    Goal currentGoal = Goal.None; // initialization

    // goalLocation and goalElevation are part of a system that I'm phasing out; use digSite and depositSite instead
    MapLocation goalLocation = new MapLocation(38, 28); // Choice of (38,28) is completely arbitrary
    int goalElevation = 75; // Choice of 75 is completely arbirary

    // These coordinates are completely arbitrary
    // Ultimately, we will select these coordinates intelligently so our robot builds a wall
    List<MapLocation> depositSites = new ArrayList<>();
    List<MapLocation> digSites = new ArrayList<>();
    List<MapLocation> standSites = new ArrayList<>();
    boolean onStandSite = false;


    void filterOutOfBounds(List<MapLocation> l) {
        for (int i = 0; i < l.size(); i++) {
            MapLocation ml = l.get(i);
            if (ml.x < 0 || ml.x >= rc.getMapWidth() ||
                    ml.y < 0 || ml.y >= rc.getMapHeight()) {
                l.remove(i--);
            }
        }
    }

    void updateHQLoc() {
        System.out.println("UPDATED YAY");
        for (Direction d : Movement.directions) {
            depositSites.add(HQLocation.add(d));
            // we want the stand sites to be more evenly spread
            standSites.add(HQLocation.add(d).add(d));
            standSites.add(HQLocation.subtract(d).subtract(d.rotateLeft()));
            digSites.add(HQLocation.add(d).add(d).add(d.rotateLeft()));
            digSites.add(HQLocation.add(d).add(d).add(d));
            digSites.add(HQLocation.add(d).add(d).add(d.rotateRight()));
        }

        filterOutOfBounds(depositSites);
        filterOutOfBounds(digSites);
        filterOutOfBounds(standSites);
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
        // STEP ZERO: Go to a standing site!
        if (!onStandSite) {
            System.out.println("GOING TO FIND A PLACE TO STAND");
            goToStandSite();
            System.out.println("FOUND IT");
        }
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
    }

    public void goToStandSite() throws GameActionException {
        Movement m = new Movement(this, HQLocation, 3);
        Movement.StepResult sr = m.step();
        while (sr != Movement.StepResult.DONE) {
            sr = m.step();
        }
        System.out.println("NEAR HQ.");
        while (true) {
            for (MapLocation ss : standSites) {
                m = new Movement(this, ss, 0);
                sr = m.step();
                int occupiedTime = 0;
                while (sr == Movement.StepResult.MOVED) {
                    if (rc.canSenseLocation(ss) && rc.senseRobotAtLocation(ss) != null) {
                        if(occupiedTime > 3){//robot's been standing here for a while
                            break;
                        }else{
                            occupiedTime++;
                        }
                    }
                    sr = m.step();
                }
                if (sr == Movement.StepResult.DONE) {
                    onStandSite = true;
                    return;
                }
            }
        }
    }

    /*
     * Try to fill the robot up with dirt from location digSite
     */
    boolean executeRefillMode() throws GameActionException {
        MapLocation digSite = null;
        for (MapLocation site : digSites) {
            int d = box(site, rc.getLocation());
            if (d == 1 && rc.senseRobotAtLocation(site)==null) {
                digSite = site;
                break;
            }
        }
        if (digSite != null) { // If adjacent to digSite, dig from it!
            tryDigDirt(rc.getLocation().directionTo(digSite));
            return true;
        } else { // If not adjacent to digSite, move toward it
            onStandSite = false;
            return false;
        }

    }

    /*
     * Try to dump all my dirt on location depositSite
     */
    boolean executeDepositMode() throws GameActionException {
        System.out.println("I am executing deposit mode");
        MapLocation depositSite = null;
        int minH = 99999;
        for (int i = 0; i < depositSites.size(); i++) {
            int d = box(depositSites.get(i), rc.getLocation());
            if (d == 1) {
                if (depositSite == null || rc.senseElevation(depositSites.get(i)) < minH) {
                    depositSite = depositSites.get(i);
                    minH = rc.senseElevation(depositSite);
                }
            }
        }
        if (depositSite != null) { // If adjacent to digSite, dig from it!
            tryDepositDirt(rc.getLocation().directionTo(depositSite));
            return true;
        } else { // If not adjacent to digSite, move toward it
            onStandSite = false;
            return false;
        }
    }

    public void runUnit() throws GameActionException {
        while (true) {
            endTurn();
            landscaperTurn();
        }
    }

    /**
     * Deposit one unit of dirt in the given direction IFF it is possible
     * <p>
     * Direction dir = the direction in which you want to deposit dirt
     *
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
     * <p>
     * Direction dir = direction from which to dig
     *
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
