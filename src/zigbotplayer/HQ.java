package zigbotplayer;

import battlecode.common.*;
import javafx.util.Builder;

import java.util.Arrays;

public class HQ extends RobotPlayer {

    void HQTurn() throws GameActionException {
        BuildUnits.considerBuild(this, RobotType.MINER);
    }

    public void runUnit() throws GameActionException {
        while (true) {
            HQTurn();
            endTurn();
            // This line prints the contents of the most recent block on the blockchain
            System.out.println("Last block = " + Arrays.toString(rc.getBlock(rc.getRoundNum() - 1)));
        }
    }

}
