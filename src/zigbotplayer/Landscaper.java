package zigbotplayer;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

import java.util.Arrays;

public class Landscaper extends RobotPlayer {

    public void landScaperTurn() throws GameActionException {
        moveAnywhere();
        
        // System.out.println("I'M A LANDSCAPER AND I EXIST!");
    }

    public void runUnit() throws GameActionException {
        while (true) {
            landScaperTurn();
            endTurn();
        }
    }
}
