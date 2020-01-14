package zigbotplayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;

import java.util.Queue;

public class HQ extends RobotPlayer {
    int built = 0;

    void HQTurn() throws GameActionException {
        for (Direction dir : directions) {
            built++;
            tryBuild(RobotType.MINER, dir);
        }
    }

    void runHQ() throws GameActionException {
        while (true) {
            HQTurn();
            Clock.yield();
        }
    }

}
