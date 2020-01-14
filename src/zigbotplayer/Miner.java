package zigbotplayer;

import battlecode.common.*;

// Miner class. A miner will set it's goal to be any soup in sight, then
// once it's mined, set it's goal to be any refinery in sight or the refinery it came from.
// it will try to move towards it's goal and achieve it.
public class Miner extends RobotPlayer {
    boolean mined = false;

    enum Goal {None, Refine, Mine}

    ;

    Goal g = Goal.None;

    MapLocation refinery;
    MapLocation soup;

    void runMiner() throws GameActionException {
        while (true) {
            minerTurn();
            Clock.yield();
        }
    }

    boolean move(Direction d) throws GameActionException {
        boolean success = tryMove(d);
        if(success) {
            Clock.yield();
        }
        return success;
    }

    void chooseMove() throws GameActionException {
        if (g == Goal.None) {
            updateGoal();
        }
        if (g == Goal.None) {
            move(randomDirection());
        } else {
            MapLocation dest;
            if (g == Goal.Refine) {
                dest = refinery;
            } else {
                assert g == Goal.Mine;
                dest = soup;
            }
            printGoal();
            for (Direction d : directions) {
                if (taxicab(rc.getLocation().add(d), dest) < taxicab(rc.getLocation(), dest)) {
                    if (move(d)) {
                        Clock.yield();
                        return;
                    }
                }
            }
        }
    }

    int taxicab(MapLocation a, MapLocation b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    void updateGoal() throws GameActionException {
        MapLocation goal = null;
        MapLocation robot = rc.getLocation();
        int minD = 100000;
        if (mined) {
            for (RobotInfo ri : rc.senseNearbyRobots(-1, rc.getTeam())) {
                if (((ri.type == RobotType.HQ || ri.type == RobotType.REFINERY))) {
                    int d = taxicab(ri.location, robot);
                    if (d < minD) {
                        goal = ri.location;
                        minD = d;
                    }
                }
            }
        } else {
            for (MapLocation ml : inSight()) {
                if (rc.senseSoup(ml) > 0) {
                    System.out.println("SEE SOUP " + mined);
                    goal = ml;
                    break;
                }
            }
        }
        if (goal != null) {
            if (mined) {
                g = Goal.Refine;
                refinery = goal;
            } else {
                g = Goal.Mine;
                soup = goal;
            }
        }
    }
    void printGoal(){
        if (g == Goal.Refine) {
            System.out.println("Goal: " + g + ", at: " + refinery + " Robot at "+ rc.getLocation());
        } else if (g == Goal.Mine) {
            System.out.println("Goal: " + g + ", at: " + soup + " Robot at "+ rc.getLocation());
        }
    }

    void minerTurn() throws GameActionException {
        if (!mined) {
            chooseMove();
            for (Direction dir : directions) {
                if (tryMine(dir)) {
                    mined = true;
                    Clock.yield();
                    if (refinery != null && taxicab(refinery, rc.getLocation()) > 7) { //far from refinery...
                        tryBuild(RobotType.REFINERY, oppositeDirection(dir));
                    }
                    g = Goal.None;
                    System.out.println("Done mining soup " + rc.getSoupCarrying());
                    return;
                }
            }
        } else {
            chooseMove();
            for (Direction dir : directions) {
                if (tryRefine(dir)) {
                    mined = false;
                    g = Goal.None;
                    System.out.println("Refined soup");
                    return;
                }
            }
        }
        //reset goals if the location we were supposed to go to is gone
        /*if (g == Goal.Refine && taxicab(rc.getLocation(), refinery) == 0) {
            g = Goal.None;
        } else if (g == Goal.Mine && taxicab(rc.getLocation(), soup) == 0) {
            g = Goal.None;
        }*/
    }


    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
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
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }
}
