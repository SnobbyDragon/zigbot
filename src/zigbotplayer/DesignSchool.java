package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

import java.util.Arrays;

public class DesignSchool extends RobotPlayer{

    public void designSchoolTurn() throws GameActionException {
        for (Direction dir : directions) {
            if (designSchools < rc.getTeamSoup() / 200) {
                designSchools++;
                tryBuild(RobotType.LANDSCAPER, dir);
                endTurn();
            }
        }
    }

    public void runUnit() throws GameActionException {
        while (true) {
            designSchoolTurn();
            endTurn();
            System.out.println(Arrays.toString(rc.getBlock(rc.getRoundNum() - 1)));
        }
    }
}
