package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import battlecode.common.MapLocation;

import static zigbotplayer.RobotPlayer.rc;

/*
 * Build and prioritize unit creation.
 */
public final class BuildUnits {
    /*
     * Given a request to build a robot type, determine if it's worth it to act on the request.
     * Returns a location: null if nothing was built, or
     * the location of the new building.
     */
    public static MapLocation considerBuild(RobotPlayer builder, RobotType toBuild) {
        int soup = rc.getTeamSoup();
        switch (toBuild) {
            case DESIGN_SCHOOL:
                if (soup < 700 * builder.designSchools) {
                    return null; // there is not a lot of soup and a lot of schools already
                }
                break;
            case MINER:
                if (soup < builder.miners * 50) {// build miners when there is soup excess
                    return null;
                }
                break;
            case LANDSCAPER:
                if (soup < builder.landscapers * 200) {
                    return null;
                }
                break;
            case DELIVERY_DRONE:
                if(soup < builder.drones * 300){
                    return null;
                }
            default:
                break;
        }
        try {
            return build(builder, toBuild);
        } catch (GameActionException e) {
            System.out.println("EXCEPTION! Failed to build " + toBuild + e.toString());
            return null;
        }
    }

    public static MapLocation build(RobotPlayer builder, RobotType toBuild) throws GameActionException {
        for (Direction d : builder.directions) {
            if (builder.tryBuild(toBuild, d)) {
                switch (toBuild) {
                    case DESIGN_SCHOOL:
                        // note: designSchool increment will happen automatically when messages are read later.
                        builder.submitMessage(3, new int[]{1, 0, 0, 0, 0, 0});
                        break;
                    case LANDSCAPER:
                        builder.landscapers++;
                        break;
                    case MINER:
                        builder.miners++;
                        break;
                    case DELIVERY_DRONE:
                        builder.drones++;
                        break;
                }
                builder.endTurn();
                return rc.getLocation().add(d);
            }
        }
        return null;
    }
}
