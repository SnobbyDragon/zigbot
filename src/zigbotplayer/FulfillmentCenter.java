package zigbotplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class FulfillmentCenter extends RobotPlayer {

    public void fCenterTurn() {
        if(BuildUnits.considerBuild(this, RobotType.DELIVERY_DRONE)!=null){
            drones++;
        }
    }

    public void runUnit() throws GameActionException {
        while (true) {
            fCenterTurn();
            endTurn();
        }
    }
}
