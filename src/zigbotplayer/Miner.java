package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class Miner extends RobotPlayer{

    static void runMiner() throws GameActionException {
        tryBlockchain();
        tryMove(randomDirection());
        for (Direction dir : directions) {
            while (tryMine(dir) && rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
                if (tryRefine(dir)) {
                    System.out.println("I refined soup! " + rc.getTeamSoup());
                }
            }
        }
        if (tryMove(randomDirection())) {
            System.out.println("I moved!");
        }
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dir : directions) {
            tryBuild(RobotType.FULFILLMENT_CENTER, dir);
        }
        for (Direction dir : RobotPlayer.directions)
            if (RobotPlayer.tryRefine(dir))
                System.out.println("I refined soup! " + RobotPlayer.rc.getTeamSoup());
    }
}
