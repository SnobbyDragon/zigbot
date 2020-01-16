package zigbotplayer;

import battlecode.common.*;

// Miner class. A miner will set it's goal to be any soup in sight, then
// once it's mined, set it's goal to be any refinery in sight or the refinery it came from.
// it will try to move towards it's goal and achieve it.
// It it can't move in the general direction it wants to go, it will wander in a random direction
// for a while.
public class Miner extends RobotPlayer {
    boolean mined = false;

    enum Goal {None, Refine, Mine, Explore}

    ;

    Goal g = Goal.None;
    Direction exploreDir = randomDirection();
    int exploreRounds = 0;

    MapLocation refinery;
    MapLocation soup;

    public void runUnit() throws GameActionException {
        while (true) {
            minerTurn();
            endTurn();
        }
    }

    boolean generalMove(Direction d) throws GameActionException {
        Direction[] genDirs = generalDirectionOf(d);
        int rand = (int) (Math.random() * genDirs.length);
        for (int i = 0; i < genDirs.length; i++) {
            if (tryMove(genDirs[(i + rand) % genDirs.length])) {
                return true;
            }
        }
        return false;
    }

    void chooseMove() throws GameActionException {
        printGoal();
        if (g == Goal.None || (g == Goal.Explore && exploreRounds <= 0)) {
            updateGoal();
        }
        if (g == Goal.None || g == Goal.Explore) {
            exploreRounds--;
            if (!generalMove(exploreDir)) {
                exploreDir = randomDirection();
            }
        } else {
            MapLocation dest;
            if (g == Goal.Refine) {
                dest = refinery;
            } else {
                assert g == Goal.Mine;
                dest = soup;
            }
            for (Direction d : directions) {
                if (taxicab(rc.getLocation().add(d), dest) < taxicab(rc.getLocation(), dest)) {
                    if (tryMove(d)) {
                        System.out.println("MOVED " + d);
                        return;
                    }
                }
            }
            // failed to move
            exploreDir = randomDirection();
            g = Goal.Explore;
            exploreRounds = (int) (Math.random() * 10);
            System.out.println("EXPLORE FOR  " + exploreRounds);
        }
    }

    int taxicab(MapLocation a, MapLocation b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    MapLocation nearestRefinery() {
        MapLocation robot = rc.getLocation();
        MapLocation nearRef = null;
        int minD = 100000;
        for (RobotInfo ri : rc.senseNearbyRobots(-1, rc.getTeam())) {
            if (((ri.type == RobotType.HQ || ri.type == RobotType.REFINERY))) {
                int d = taxicab(ri.location, robot);
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

    void updateGoal() throws GameActionException {
        MapLocation goal = null;
        if (mined) {
            goal = nearestRefinery();
        } else {
            for (MapLocation ml : inSight()) {
                if (rc.senseSoup(ml) > 0) {
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

    void printGoal() {
        if (g == Goal.Refine) {
            System.out.println("Goal: " + g + ", at: " + refinery + " Robot at " + rc.getLocation());
        } else if (g == Goal.Mine) {
            System.out.println("Goal: " + g + ", at: " + soup + " Robot at " + rc.getLocation());
        }
    }

    void minerTurn() throws GameActionException {
        if (rc.getTeamSoup() > 300*designSchools) {
            for (Direction d : directions) {
                if (tryBuild(RobotType.DESIGN_SCHOOL, d)) {
                    submitMessage(6, new int[]{5, 0, 0 ,0 ,0 ,0 ,0});
                    break;
                }
            }
        }
        if (!mined) {
            chooseMove();
            for (Direction dir : directions) {
                if (tryMine(dir)) {
                    mined = true;
                    Clock.yield();
                    if (refinery != null && taxicab(nearestRefinery(), rc.getLocation()) >= 5) { //far from refinery...
                        for (Direction d : generalDirectionOf(dir)) {
                            if (tryBuild(RobotType.REFINERY, d)) {
                                break;
                            }
                        }
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
        if (g == Goal.Refine && taxicab(rc.getLocation(), refinery) == 0) {
            g = Goal.None;
        } else if (g == Goal.Mine && taxicab(rc.getLocation(), soup) == 0) {
            g = Goal.None;
        }
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
                Clock.yield();
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
                Clock.yield();
            }
            return true;
        } else return false;
    }
}
