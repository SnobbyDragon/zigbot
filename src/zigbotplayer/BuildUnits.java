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
    int toSave = 0;

    /*
     * Given a request to build a robot type, determine if it's worth it to act on the request.
     * Returns a location: null if nothing was built, or
     * the location of the new building.
     */
    public static MapLocation considerBuild(RobotPlayer builder, RobotType toBuild) {
        int soup = rc.getTeamSoup();
        switch (toBuild) {
            case DESIGN_SCHOOL:
                if (soup < 280 + 500 * builder.designSchools) {
                    return null; // there is not a lot of soup and a lot of schools already
                }
                break;
            case MINER:
                if (soup < 70 + builder.miners * 40) {// build miners when there is soup excess
                    return null;
                }
                break;
            case LANDSCAPER:
                if (soup < Math.min(300, builder.landscapers * 100) || builder.landscapers > 15) {
                    return null;
                }
                break;
            case DELIVERY_DRONE:
                if (soup < Math.max(builder.drones * 140,400)) {
                    return null;
                }
                break;
            case FULFILLMENT_CENTER:
                // only build drones if we are doing pretty well on resources.
                if (builder.landscapers < 5 + Math.random() * 5 || soup < 160 + builder.fullfillmentCenters * 600) {
                    return null;
                }
            default:
                break;
        }
        //save money for a defensive net gun deployment if needed.
        if (toBuild != RobotType.NET_GUN && toBuild !=RobotType.REFINERY
                && toBuild.cost + RobotType.NET_GUN.cost > soup && rc.getRoundNum() > 100) {
            return null;
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
                        builder.submitMessage(1, new int[]{4, 0, 0, 0, 0, 0});
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
