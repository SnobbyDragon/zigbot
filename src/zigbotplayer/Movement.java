package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static zigbotplayer.RobotPlayer.MAP_HEIGHT;
import static zigbotplayer.RobotPlayer.MAP_WIDTH;

/*
 * A class for movement of all units.
 * If you want your robot to go to a certain location, create a new instance of this class:
 * m = new Movement(this, destination);
 *
 * Then to move towards your destination call m.step();
 */
public class Movement {
    // the robot that wants to go
    RobotPlayer rp;
    RobotController rc;
    // Where the robot wants to go
    public MapLocation destination;

    /*
     * Create new instance of movement class. If dest = null, assumes that movement
     * will be in some randomish direction.
     */
    public Movement(RobotPlayer you, MapLocation dest){
        rp = you;
        rc = RobotPlayer.rc;
        destination = dest;
    }

    /*
     * Update the location you wish to go to
     */
    public void updateDest(MapLocation newDest){
        destination = newDest;
    }

    public enum StepResult {MOVED, STUCK, DONE};

    /*
     * Direction a robot should move in to go to a destination.
     */
    public StepResult step() throws GameActionException {
        if(destination==null){
            return randomMove();
        }
        Direction dir = Movement.directionToLoc(RobotPlayer.rc.getLocation(), destination);
        if(dir == Direction.CENTER){
            return StepResult.DONE;
        }
        for (Direction d : Movement.generalDirectionOf(dir)) {
            if(tryMove(d)) {
                return StepResult.MOVED;
            }
        }
        return StepResult.STUCK;
    }

    /**
     * Chooses a direction to explore based on map edges, water, elevation, nearby units
     * TODO
     * @return a direction to explore
     */
    Direction pickExploreDirection() {
        String dir = "";
        MapLocation current = rc.getLocation();
        int sightRadiusSquared = rc.getCurrentSensorRadiusSquared();

        //first check if near edge of map, we don't want to move towards the edge when there's already nothing there
        if ((MAP_HEIGHT - current.y) * (MAP_HEIGHT - current.y) < sightRadiusSquared) { //can see the north edge of the map, must move south

        } else if (current.y < sightRadiusSquared) { //can see the south edge of the map, must move north

        }
        if (MAP_WIDTH - current.x < sightRadiusSquared) { //can see the east edge of the map, must move west
            dir += "WEST";
        } else if (current.x < sightRadiusSquared) { //can see the west edge of the map, must move east
            dir += "EAST";
        }
        if (!dir.equals("")) {
            return Direction.valueOf(dir);
        }
        //not near the edge of the map, so check for nearby robots

        //not near edge of map, no nearby robots
        return this.randomDirection();
    }
    Direction exploreDir;
    public StepResult randomMove() throws GameActionException {
        if(exploreDir == null){
            exploreDir = randomDirection();
        }
        for(Direction d : generalDirectionOf(exploreDir)){
            if(tryMove(d)){
                return StepResult.MOVED;
            }
        }
        return StepResult.STUCK;
    }
    /**
     * An array of all directions excluding CENTER in
     * clockwise order
     */
    static Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    /**
     * Finds the direction from a location to another
     *
     * @param a a location on the map
     * @param b another location on the map
     * @return the direction from a to b
     */
    private static Direction directionToLoc(MapLocation a, MapLocation b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        String dir = "";
        if (dy < 0) { //a is to the south of b
            dir += "NORTH";
        } else if (dy > 0) { //a is to the north of b
            dir += "SOUTH";
        }
        if (dx < 0) { //a is to the west of b
            dir += "EAST";
        } else if (dx > 0) { //a is to the east of b
            dir += "WEST";
        }
        if (dir.equals("")) { //a and b are the same
            dir = "CENTER";
        }
        return Direction.valueOf(dir);
    }


    /**
     * Attempts to move in a given direction. Fails if it is impossible or would lead to walking into water.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        while(!rc.isReady()){
            System.out.println("Was not ready to move, so waited first");
            rp.endTurn();
        }
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
            rc.move(dir);
            if (rc.getCooldownTurns() >= 1) {
                rp.endTurn();
            }
            return true;
        } else return false;
    }

    /**
     * Returns whether the given direction is not CENTER
     *
     * @param d the given direction
     * @return whether the given direction is not CENTER
     */
    static int indInDirList(Direction d) {
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == d) {
                return i;
            }
        }
        throw new RuntimeException("Direction Index not found in list");
    }

    /**
     * Returns directions adjacent to the given direction
     *
     * @param d the given direction
     * @return an array of 3 directions
     */
    static Direction[] generalDirectionOf(Direction d) {
        int ind = indInDirList(d);
        return new Direction[]{d, directions[(ind + 1) % 8], directions[(ind + 7) % 8]};
    }

    /**
     * returns whether the direction points north
     *
     * @param d the given direction
     * @return 1 if north, -1 if south, 0 otherwise
     */
    static int directionNorth(Direction d) {
        switch (d) {
            case NORTH:
            case NORTHEAST:
            case NORTHWEST:
                return 1;
            case SOUTH:
            case SOUTHEAST:
            case SOUTHWEST:
                return -1;
            default:
                return 0;
        }
    }

    /**
     * returns whether the direction points east
     *
     * @param d the given direction
     * @return 1 if east, -1 if west, 0 otherwise
     */
    static int directionEast(Direction d) {
        switch (d) {
            case EAST:
            case NORTHEAST:
            case SOUTHEAST:
                return 1;
            case WEST:
            case NORTHWEST:
            case SOUTHWEST:
                return -1;
            default:
                return 0;
        }
    }


    /**
     * Returns the opposite direction of the given direction
     *
     * @param d the given direction
     * @return the opposite direction
     * @throws RuntimeException
     */
    public static Direction oppositeDirection(Direction d) throws RuntimeException {
        return directions[(4 + indInDirList(d)) % 8];
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    Direction randomDirection() {
        return Movement.directions[(int) (Math.random() * Movement.directions.length)];
    }
}