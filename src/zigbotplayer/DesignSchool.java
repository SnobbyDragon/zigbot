package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

import java.util.Arrays;

public class DesignSchool extends RobotPlayer{

    public void designSchoolTurn() throws GameActionException {
        for (Direction dir : directions) {
            if (landscapers < rc.getTeamSoup() / 200) {
                System.out.println("BUILDING LANDSCAPER");
                if(tryBuild(RobotType.LANDSCAPER, dir)) {
                    landscapers++;
                }
                endTurn();
            }
        }
    }

    public void runUnit() throws GameActionException {
        while (true) {
            designSchoolTurn();
            endTurn();
        }
    }
}
