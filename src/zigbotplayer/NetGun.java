package zigbotplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class NetGun extends RobotPlayer{
    Team foeteam;
    void GunTurn() throws GameActionException {
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
        while (true) {
            GunTurn();
            endTurn();
        }
    }
}
