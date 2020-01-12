package zigbotplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

import java.util.Queue;

public class Miner extends RobotPlayer {

    static void runMiner() throws GameActionException {
        boolean mined = false;
        for (Direction dir : directions) {
            while (tryMine(dir)){
                mined = true;
                System.out.println("Done mining soup " + rc.getTeamSoup());
                Clock.yield();
                tryBuild(RobotType.REFINERY, oppositeDirection(dir));
            }
        }
        if (!mined) {
            tryMove(randomDirection());
        }
        for(Direction dir: directions) {
            tryRefine(dir);
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
