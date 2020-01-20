package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

import java.util.Arrays;

public class DesignSchool extends RobotPlayer{

    public void designSchoolTurn() throws GameActionException {
        BuildUnits.considerBuild(this,RobotType.LANDSCAPER);
    }

    public void runUnit() throws GameActionException {
        while (true) {
            designSchoolTurn();
            endTurn();
        }
    }
}
