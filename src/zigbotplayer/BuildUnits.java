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
                if (soup < 100 + 500 * builder.designSchools) {
                    return null; // there is not a lot of soup and a lot of schools already
                }
                break;
            case MINER:
                if (soup < 40 + builder.miners * 40) {// build miners when there is soup excess
                    return null;
                }
                break;
            case LANDSCAPER:
                if (soup < Math.min(300, builder.landscapers * 50)) {
                    return null;
                }
                break;
            case DELIVERY_DRONE:
                if (soup < builder.drones * 100) {
                    return null;
                }
                break;
            case FULFILLMENT_CENTER:
                return null;//fullfillment centers and drones suck
                /*
                if (soup < 160 + builder.fullfillmentCenters * 600) {
                    return null;
                }*/
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
        for (Direction d : Movement.directions) {
            int hqDist;
            if (RobotPlayer.HQLocation == null) {
                hqDist = 0;
            } else {
                hqDist = builder.box(rc.getLocation().add(d), RobotPlayer.HQLocation);
            }
            //things that don't move should go away from HQ
            if ((toBuild == RobotType.DESIGN_SCHOOL || toBuild == RobotType.REFINERY ||
                    toBuild == RobotType.NET_GUN || toBuild == RobotType.FULFILLMENT_CENTER) && hqDist < 3) {
                continue;
            }
            if (tryBuild(builder, toBuild, d)) {
                switch (toBuild) {
                    case DESIGN_SCHOOL:
                        // note: designSchool increment will happen automatically when messages are read later.
                        builder.submitMessage(8, new int[]{1, 0, 0, 0, 0, 0});
                        break;
                    case FULFILLMENT_CENTER:
                        builder.submitMessage(8, new int[]{3, 0, 0, 0, 0, 0});
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

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir  The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotPlayer rp, RobotType type, Direction dir) throws GameActionException {
        while (rc.getCooldownTurns() >= 1) {
            rp.endTurn();
        }
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            while (rc.getCooldownTurns() >= 1) {
                rp.endTurn();
            }
            return true;
        } else return false;
    }
}
