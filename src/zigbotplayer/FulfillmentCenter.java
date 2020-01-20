package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

public class FulfillmentCenter extends RobotPlayer{

    public void fCenterTurn() throws GameActionException {
        for (Direction dir : directions) {
            if (drones < rc.getTeamSoup() / 300) {
                drones++;
                tryBuild(RobotType.DELIVERY_DRONE, dir);
                endTurn();
            }
        }
    }

    public void runUnit() throws GameActionException {
        while (true) {
            fCenterTurn();
            endTurn();
        }
    }
}
