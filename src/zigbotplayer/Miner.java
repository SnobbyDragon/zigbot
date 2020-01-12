package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class Miner {

    static void runMiner() throws GameActionException {
        RobotPlayer.tryBlockchain();
        RobotPlayer.tryMove(RobotPlayer.randomDirection());
        for (Direction dir : RobotPlayer.directions)
            while (RobotPlayer.tryMine(dir) && RobotPlayer.rc.getSoupCarrying() == RobotType.MINER.soupLimit){
                if (RobotPlayer.tryRefine(dir)) {
                    System.out.println("I refined soup! " + RobotPlayer.rc.getTeamSoup());
                }
            }
        if (RobotPlayer.tryMove(RobotPlayer.randomDirection())) {
            System.out.println("I moved!");
        }
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dir : RobotPlayer.directions) {
            RobotPlayer.tryBuild(RobotType.FULFILLMENT_CENTER, dir);
        }
        for (Direction dir : RobotPlayer.directions)
            if (RobotPlayer.tryRefine(dir))
                System.out.println("I refined soup! " + RobotPlayer.rc.getTeamSoup());
    }
}
