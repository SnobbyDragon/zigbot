package zigbotplayer;

import battlecode.common.*;

import java.util.Arrays;

public class HQ extends RobotPlayer {

    Team foeteam;
    void HQTurn() throws GameActionException {
        if(rc.getRoundNum() < HQ_WALL_PHASE) {
            BuildUnits.considerBuild(this, RobotType.MINER);
        }
        for(RobotInfo robo:rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), foeteam)){
            if(robo.type==RobotType.DELIVERY_DRONE){
                if(rc.canShootUnit(robo.getID())) {
                    rc.shootUnit(robo.getID());
                }
            }
        }
    }

    public void runUnit() throws GameActionException {
        foeteam = rc.getTeam().opponent();
        submitMessage(11, new int[]{2, rc.getLocation().x, rc.getLocation().y, 0, 0, 0});
        while (true) {
            HQTurn();
            endTurn();
            // This line prints the contents of the most recent block on the blockchain
            System.out.println("Last block = " + Arrays.toString(rc.getBlock(rc.getRoundNum() - 1)));
        }
    }

}
