package zigbotplayer;

import battlecode.common.*;

// Miner class. A miner will set it's goal to be any soup in sight, then
// once it's mined, set it's goal to be any refinery in sight or the refinery it came from.
// it will try to move towards it's goal and achieve it.
// It it can't move in the general direction it wants to go, it will wander in a random direction
// for a while.
public class Miner extends RobotPlayer {
    boolean mined = false;

    /**
     * Goals of the miner
     * None = no goal
     * Refine = refine the soup it has
     * Mine = go mine soup
     * Explore = go find soup
     */
    enum Goal {None, Refine, Mine, Explore}

    ;
    Movement movement = new Movement(this, null);
    Goal g = Goal.None;
    int exploreRounds = 0;

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
     * Tries to move in the given general direction
     *
     * @param d the given direction
     * @return whether the miner can move towards the given direction
     * @throws GameActionException
     */
    boolean generalMove(Direction d) throws GameActionException {
        Direction[] genDirs = Movement.generalDirectionOf(d);
        int rand = (int) (Math.random() * genDirs.length);
        for (int i = 0; i < genDirs.length; i++) {
            if (new Movement(this, null).tryMove(genDirs[(i + rand) % genDirs.length])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves in a direction depending on goal.
     *
     * @throws GameActionException
     */
    void chooseMove() throws GameActionException {
        if (movement == null || g == Goal.None || (g == Goal.Explore && exploreRounds <= 0)) {
            updateGoal();
            printGoal();
        }
        if (g == Goal.None || g == Goal.Explore) {
            exploreRounds--;
            movement.step();
        }
        Movement.StepResult sr = movement.step();
        if (sr != Movement.StepResult.DONE) {
            if (sr == Movement.StepResult.STUCK) {
                movement = new Movement(this, null);
                g = Goal.Explore;
                exploreRounds = 1 + (int) (Math.random() * 4);
                System.out.println("EXPLORE FOR  " + exploreRounds);
            }
        }
    }

    /**
     * Finds the nearest refinery
     *
     * @return the location of the nearest refinery
     */
    MapLocation nearestRefinery() {
        MapLocation robot = rc.getLocation();
        MapLocation nearRef = null;
        int minD = 100000;
        for (RobotInfo ri : rc.senseNearbyRobots(-1, rc.getTeam())) {
            if (((ri.type == RobotType.HQ || ri.type == RobotType.REFINERY))) {
                int d = box(ri.location, robot);
                if (d < minD) {
                    nearRef = ri.location;
                    minD = d;
                }
            }
        }
        if (nearRef != null) {
            refinery = nearRef;
        }
        return refinery;
    }

    /**
     * @throws GameActionException
     */
    void updateGoal() throws GameActionException {
        MapLocation goal = soup;
        if (mined) {
            goal = nearestRefinery();
        } else {
            for (MapLocation ml : inSight()) {
                if (rc.senseSoup(ml) > 0) {
                    goal = ml;
                    soup = goal;
                    break;
                }
            }
        }
        if (goal != null) {
            if (mined) {
                g = Goal.Refine;
            } else {
                g = Goal.Mine;
            }
        }
        System.out.println("Set goal to " + goal);
        movement = new Movement(this, goal);
    }

    void printGoal() {
        if (g == Goal.Refine) {
            System.out.println("Goal: " + g + ", at: " + refinery + " Robot at " + movement.destination);
        } else if (g == Goal.Mine) {
            System.out.println("Goal: " + g + ", at: " + soup + " Robot at " + movement.destination);
        }
    }

    void minerTurn() throws GameActionException {
        BuildUnits.considerBuild(this, RobotType.DESIGN_SCHOOL);
        if (refinery != null && box(nearestRefinery(), rc.getLocation()) >= 6 && box(soup, rc.getLocation()) < 2) {
            BuildUnits.considerBuild(this, RobotType.REFINERY);
        }
        chooseMove();
        if (!mined) {
            for (Direction dir : Movement.directions) {
                while (tryMine(dir)) {
                    return;
                }
            }
        } else {
            for (Direction dir : Movement.directions) {
                if (tryRefine(dir)) {
                    return;
                }
            }
        }
        //reset goals if the location we were supposed to go to is gone
        if (g == Goal.Refine && taxicab(rc.getLocation(), refinery) == 0) {
            refinery = HQLocation;
        } else if (g == Goal.Mine && taxicab(rc.getLocation(), soup) == 0) {
            soup = null;
        }
        mined = rc.getSoupCarrying() == RobotType.MINER.soupLimit;
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
