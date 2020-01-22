package zigbotplayer;

import battlecode.common.*;

// Miner class. A miner will set it's goal to be any soup in sight, then
// once it's mined, set it's goal to be any refinery in sight or the refinery it came from.
// it will try to move towards it's goal and achieve it.
// It it can't move in the general direction it wants to go, it will wander in a random direction
// for a while.
public class Miner extends RobotPlayer {
    private boolean mined() {
        return rc.getSoupCarrying() == RobotType.MINER.soupLimit;
    }

    /**
     * Goals of the miner
     * None = no goal
     * Refine = refine the soup it has
     * Mine = go mine soup
     * Explore = go find soup
     */
    enum Goal {None, Refine, Mine, Explore}

    ;
    Movement movement = new Movement(this);
    Goal g = Goal.None;

    /**
     * Location of the refinery to deliver soup.
     */
    MapLocation refinery;

    /**
     * Location of the soup to mine.
     */
    MapLocation soup;

    public void runUnit() throws GameActionException {
        refinery = nearestRefinery();
        while (true) {
            minerTurn();
            endTurn();
        }
    }

    /**
     * Moves in a direction depending on goal.
     *
     * @throws GameActionException
     */
    void chooseMove() throws GameActionException {
        if (g == Goal.None) {
            updateGoal();
            movement.step();
        }
        Movement.StepResult sr = movement.step();
        wardOffFoes();
        if (sr != Movement.StepResult.DONE) {
            if (sr == Movement.StepResult.STUCK) {
                System.out.println("New movement");
                g = Goal.None;
                updateGoal();
                movement = new Movement(this);
            }
        }
    }

    /**
     * Finds the nearest refinery
     *
     * @return the location of the nearest refinery
     */
    MapLocation nearestRefinery() throws GameActionException {
        MapLocation robot = rc.getLocation();
        MapLocation nearRef = null;
        int minD = 100000;
        for (RobotInfo ri : rc.senseNearbyRobots(-1, rc.getTeam())) {
            // we stop using the HQ as a refinery eventually
            if ((((ri.type == RobotType.HQ && rc.getRoundNum() < HQ_WALL_PHASE) || ri.type == RobotType.REFINERY))) {
                int d = box(ri.location, robot);
                if (d < minD) {
                    nearRef = ri.location;
                    minD = d;
                }
            }
        }
        if (nearRef != null) {
            refinery = nearRef;
        } else {
            System.out.println(soup + " " + rc.getLocation());
            if (soup == null) {
                soup = nearestSoup();
            }
            if (soup != null && box(soup, rc.getLocation()) < 2) {
                System.out.println("Wanna build refinery...");
                MapLocation ml = BuildUnits.considerBuild(this, RobotType.REFINERY);
                if (ml != null) {
                    refinery = ml;
                }
            }
        }
        return refinery;
    }

    MapLocation nearestSoup() throws GameActionException {
        for (MapLocation ml : rc.senseNearbySoup()) {
            if (rc.senseRobotAtLocation(ml) == null && (rc.getRoundNum() < HQ_WALL_PHASE || box(HQLocation, ml) >= 3)) {
                soup = ml;
                return soup;
            }
        }
        return null;
    }

    /**
     * @throws GameActionException
     */
    void updateGoal() throws GameActionException {
        MapLocation goal = mined() ? nearestRefinery() : nearestSoup();
        if (g == Goal.None && goal != null) {
            movement = new Movement(this, goal, 1);
            if (mined()) {
                g = Goal.Refine;
            } else {
                g = Goal.Mine;
            }
        }
        printGoal();

    }

    void printGoal() {
        if (g == Goal.Refine) {
            System.out.println("Goal: " + g + ", at: " + refinery);
        } else if (g == Goal.Mine) {
            System.out.println("Goal: " + g + ", at: " + soup);
        }
    }

    void wardOffFoes() {
        int foeDroneCount = 0;
        int netGunCount = 0;
        for (RobotInfo ri : rc.senseNearbyRobots()) {
            if (ri.type == RobotType.DELIVERY_DRONE && ri.team == rc.getTeam().opponent()) {
                foeDroneCount++;
            } else if (ri.type == RobotType.NET_GUN && ri.team == rc.getTeam()) {
                netGunCount++;
            }
        }
        if (netGunCount * 3 - foeDroneCount < 0) {
            BuildUnits.considerBuild(this, RobotType.NET_GUN);
        }
    }

    private void thinkAboutBuildingStuff() {
        BuildUnits.considerBuild(this, RobotType.DESIGN_SCHOOL);
        BuildUnits.considerBuild(this, RobotType.FULFILLMENT_CENTER);
    }

    void minerTurn() throws GameActionException {
        if (rc.getRoundNum() > HQ_WALL_PHASE && box(HQLocation, rc.getLocation()) < 3) {
            if (refinery == HQLocation) {
                refinery = null;
            }
            //stay away from HQ and let landscapers do their thing
            System.out.println("Leaving HQ");
            Movement m = new Movement(this, HQLocation.directionTo(rc.getLocation()));
            while (box(HQLocation, rc.getLocation()) < 3) {
                m.step();
            }
        }
        thinkAboutBuildingStuff();
        chooseMove();
        if (!mined() && soup != null) {
            while (tryMine(rc.getLocation().directionTo(soup))) ;
        } else if (refinery != null) {
            while (tryRefine(rc.getLocation().directionTo(refinery))) ;
        }
        //reset goals if the location we were supposed to go to is gone
        if (refinery != null && rc.canSenseLocation(refinery) &&
                rc.senseRobotAtLocation(refinery).type != RobotType.REFINERY) {
            refinery = null;
        } else if (soup != null && (rc.canSenseLocation(soup) && rc.senseFlooding(soup) || rc.senseSoup(soup) == 0 ||
                (rc.getRoundNum() < HQ_WALL_PHASE || box(HQLocation, rc.getLocation()) < 3))) {
            soup = null;
        }
        g = Goal.None;
        updateGoal();
    }


    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            if (rc.getCooldownTurns() >= 1) {
                endTurn();
            }
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            if (rc.getCooldownTurns() >= 1) {
                endTurn();
            }
            return true;
        } else return false;
    }
}
