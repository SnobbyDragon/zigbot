package zigbotplayer;

import battlecode.common.*;

public class DeliveryDrone extends RobotPlayer {

    Movement m = new Movement(this);
    public void droneTurn() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            for(RobotInfo robot: rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy)) {
                // Pick up a first robot within range
                if (rc.canPickUpUnit(robot.getID())) {
                    rc.pickUpUnit(robot.getID());
                }
            }
        } else {
            if(rc.senseFlooding(rc.getLocation())){
                if(rc.canDropUnit(Direction.CENTER)){
                    rc.dropUnit(Direction.CENTER);
                }
            }
        }
        m.step();
    }

    public void runUnit() throws GameActionException {
        while (true) {
            droneTurn();
            endTurn();
        }
    }
}
