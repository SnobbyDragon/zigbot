package zigbotplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class FulfillmentCenter extends RobotPlayer {

    public void fCenterTurn() {
        BuildUnits.considerBuild(this, RobotType.DELIVERY_DRONE);
    }

    public void runUnit() throws GameActionException {
        while (true) {
            fCenterTurn();
            endTurn();
        }
    }
}
