package zigbotplayer;

import battlecode.common.*;

public class DeliveryDrone extends RobotPlayer {

    Movement m;
    RobotInfo held = null;

    public boolean tryPickUp(RobotInfo victim) throws GameActionException {
       Movement m = new Movement(this, victim.location, 1);
       while(m.step() != Movement.StepResult.DONE) {
           if (rc.canPickUpUnit(victim.getID())) {
               rc.pickUpUnit(victim.getID());
               return true;
           }
       }
        if (rc.canPickUpUnit(victim.getID())) {
            rc.pickUpUnit(victim.getID());
            return true;
        }
        return false;
    }
    public void droneTurn() throws GameActionException {
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            for (RobotInfo robot : rc.senseNearbyRobots()) {
                if (box(robot.location, HQLocation) == 1 && robot.type.canBePickedUp()
                        && (robot.type != RobotType.LANDSCAPER || robot.team != rc.getTeam())) {//just keep them away from our HQ plz
                    tryPickUp(robot);
                    held = robot;
                }
                if (box(robot.location, HQLocation) >= 2 && robot.type == RobotType.LANDSCAPER && robot.team == rc.getTeam()) {
                    tryPickUp(robot);
                    held = robot;
                }
            }
        } else {
            if (held.type == RobotType.LANDSCAPER && held.team == rc.getTeam()) {
                m = new Movement(this, HQLocation, 0);
                while(true){
                    for(Direction d: Movement.directions){
                        if(box(rc.getLocation().add(d), HQLocation) == 1 && tryDropUnit(d)){
                            break;
                        }
                    }
                    m.step();
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
                while (!tryDropUnit(Direction.WEST)) {
                    m.step();
                }
                held = null;
                m = new Movement(this, HQLocation, 0);
            }
        }
        if (m.step() != Movement.StepResult.MOVED) {
            m = new Movement(this, HQLocation.translate((int)(Math.random())*4-2, (int)(Math.random()*4)-2), 0);
        }
    }

    public void runUnit() throws GameActionException {
        updateFromMessages();
        m = new Movement(this, HQLocation, 0);
        while (true) {
            endTurn();
            droneTurn();
        }
    }

    public boolean tryDropUnit(Direction d) throws GameActionException {
        if (rc.getCooldownTurns() > 1) {
            endTurn();
        }
        if (rc.canDropUnit(d)) {
            rc.dropUnit(d);
            if (rc.getCooldownTurns() > 1) {
                endTurn();
            }
            return true;
        }
        return false;
    }
}
