package zigbotplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class Miner extends RobotPlayer {

    boolean mined = false;

    // nearest location of refinery
    int dx;
    int dy;

    void runMiner() throws GameActionException {
        while(true) {
            minerTurn();
            Clock.yield();
        }
    }

    void minerTurn() throws GameActionException{
        if(!mined) {
            for (Direction dir : directions) {
                if (tryMine(dir)) {
                    mined = true;
                    Clock.yield();
                    System.out.println("Done mining soup " + rc.getTeamSoup());
                    if(Math.abs(dx) + Math.abs(dy) > 7) {
                        tryBuild(RobotType.REFINERY, oppositeDirection(dir));
                    }
                    return;
                }
            }
            tryMove(randomDirection());
        } else {
            for (Direction dir : directions) {
                if(tryRefine(dir)){
                    return;
                }
            }
            tryMove(randomDirection());
        }
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
