package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

import java.util.Arrays;

public class Landscaper extends RobotPlayer {

    public void landscaperTurn() throws GameActionException {
        // Dig dirt, if possible, from an arbitrary direction
        for (Direction dir : Movement.directions) {
            if (rc.isReady() && rc.canDigDirt(dir)) {
                System.out.println("Trying to dig in direction " + dir);
                rc.digDirt(dir);
            }

            // Deposit dirt, if possible, in the SOUTH direction
            if (rc.isReady() && rc.canDepositDirt(Direction.SOUTH)) {
                System.out.println("Trying to deposit dirt south");
                rc.depositDirt(Direction.SOUTH);
            }
        }
    }

    public void runUnit() throws GameActionException {
        while (true) {
            landscaperTurn();
            endTurn();
        }
    }
}
