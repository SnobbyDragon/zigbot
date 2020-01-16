package zigbotplayer;

import battlecode.common.*;

import java.util.Arrays;

public class HQ extends RobotPlayer {
    int built = 0;

    void HQTurn() throws GameActionException {
        if (built < rc.getTeamSoup() / 30) {// build mines when there is soup excess
            for (Direction dir : directions) {
                if (tryBuild(RobotType.MINER, dir)) {
                    built++;
                }
            }
        }
    }

    public void runUnit() throws GameActionException {
        while (true) {
            HQTurn();
            endTurn();
            System.out.println(Arrays.toString(rc.getBlock(rc.getRoundNum() - 1)));
        }
    }

}
