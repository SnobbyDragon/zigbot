package zigbotplayer;

import battlecode.common.*;

import java.util.Arrays;

public class HQ extends RobotPlayer {

    void HQTurn() throws GameActionException {
        BuildUnits.considerBuild(this, RobotType.MINER);
        // todo: shoot lazorz
    }

    public void runUnit() throws GameActionException {
        submitMessage(11, new int[]{2, rc.getLocation().x, rc.getLocation().y, 0, 0, 0});
        while (true) {
            HQTurn();
            endTurn();
            // This line prints the contents of the most recent block on the blockchain
            System.out.println("Last block = " + Arrays.toString(rc.getBlock(rc.getRoundNum() - 1)));
        }
    }

}
