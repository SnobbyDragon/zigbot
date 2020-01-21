package zigbotplayer;

import battlecode.common.*;

public class DeliveryDrone extends RobotPlayer {

    Movement m;
    RobotInfo held = null;

    public void droneTurn() throws GameActionException {
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            for (RobotInfo robot : rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED)) {
                if (box(robot.location, HQLocation) == 1) {//just keep them away from our HQ plz
                    rc.pickUpUnit(robot.getID());
                    held = robot;
                }
            }
        } else {
            /*if (rc.senseFlooding(rc.getLocation())) {
                if (rc.canDropUnit(Direction.CENTER)) {
                    rc.dropUnit(Direction.CENTER);
                }
            }*/
            m = new Movement(this);
            for (int i = 0; i < 6; i++) {
                m.step();
            }
            while(!tryDropUnit(Direction.WEST)) {
                m.step();
            }
            held = null;
            m = new Movement(this, HQLocation, 3);
        }
        m.step();
    }

    public void runUnit() throws GameActionException {
        updateFromMessages();
        m= new Movement(this, HQLocation, 3);
        while (true) {
            endTurn();
            droneTurn();
        }
    }
    public boolean tryDropUnit(Direction d) throws GameActionException {
        if(rc.getCooldownTurns()>1){
            endTurn();
        }
        if(rc.canDropUnit(d)){
            rc.dropUnit(d);
            if(rc.getCooldownTurns()>1) {
                endTurn();
            }
            return true;
        }
        return false;
    }
}
