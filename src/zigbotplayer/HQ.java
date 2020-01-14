package zigbotplayer;

import battlecode.common.*;
import battlecode.server.GameInfo;
import battlecode.world.GameStats;

import java.util.Queue;

public class HQ extends RobotPlayer {
    int built = 0;

    void HQTurn() throws GameActionException {
        if(built < 12) {//build 12 then build other things?
            for (Direction dir : directions) {
                if(tryBuild(RobotType.MINER, dir)){
                    built++;
                }
            }
        }
    }

    void runHQ() throws GameActionException {
        while (true) {
            HQTurn();
            Clock.yield();
        }
    }

}
