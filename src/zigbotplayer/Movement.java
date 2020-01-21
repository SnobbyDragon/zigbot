package zigbotplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static zigbotplayer.RobotPlayer.MAP_HEIGHT;
import static zigbotplayer.RobotPlayer.MAP_WIDTH;

/*
 * A class for movement of all units.
 * If you want your robot to go to a certain location, create a new instance of this class:
 * m = new Movement(this, destination);
 *
 * Then to move towards your destination call m.step();
 * It is recommended to use just one movement object and call step on it repeatedly.
 * As implemented now it doesn't matter.
 */
public class Movement {
    // the robot that wants to go
    RobotPlayer rp;
    RobotController rc;
    // Where the robot wants to go
    public MapLocation destination;

    Direction exploreDir;
    int requiredDistance;

    /*
     * Create new instance of movement class with no real goal, just go somewhere far.
     */
    public Movement(RobotPlayer you) {
        rp = you;
        rc = RobotPlayer.rc;
    }

    /*
     * Create new instance of movement class, the robot will generally
     * go in the given direction.
     */
    public Movement(RobotPlayer you, Direction direction) {
        rp = you;
        rc = RobotPlayer.rc;
        exploreDir = direction;
        /*
        destination = you.rc.getLocation();
        for(int i = 0; i < 30; i++){
            destination.add(direction);
        }*/
    }

    /*
     * Create new instance of movement class with a destination. Also set dist: to the number of squares away
     * from your location you are ok with being (taxicab distance)
     */
    public Movement(RobotPlayer you, MapLocation dest, int dist) {
        rp = you;
        rc = RobotPlayer.rc;
        destination = dest;
        requiredDistance = dist;
    }

    /*
     * Update the location you wish to go to
     */
    public void updateDest(MapLocation newDest) {
        destination = newDest;
    }

    public enum StepResult {MOVED, STUCK, DONE}

    ;

    Set<MapLocation> seen = new HashSet<>();

    /*
     * Direction a robot should move in to go to a destination.
     */
    public StepResult step() throws GameActionException {
        seen.add(rc.getLocation());
        if (destination == null) {
            return randomMove();
        }
        Direction dir = rc.getLocation().directionTo(destination);
        if (rp.taxicab(rc.getLocation(), destination) <= requiredDistance) {
            return StepResult.DONE;
        }
        for (Direction d : Movement.directionsOf(dir)) {
            if (!seen.contains(rc.getLocation().add(d)) && tryMove(d)) {
                return StepResult.MOVED;
            }
        }
        for(Direction d: Movement.directionsOf(dir)){
            //maybe we got trapped in locations we've been to, but there is still a path
            if (tryMove(d)) {
                return StepResult.MOVED;
            }
        }
        // not good to reach here...
        return StepResult.STUCK;
    }

    /**
     * Chooses a direction to explore based on map edges, water, elevation, nearby units
     * TODO
     *
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

    public StepResult randomMove() throws GameActionException {
        if (exploreDir == null) {
            exploreDir = randomDirection();
        }
        for (Direction d : Movement.directionsOf(exploreDir)) {
            if (!seen.contains(rc.getLocation().add(d)) && tryMove(d)) {
                return StepResult.MOVED;
            }
        }
        for(Direction d: Movement.directionsOf(exploreDir)){
            //maybe we got trapped in locations we've been to, but there is still a path
            if (tryMove(d)) {
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

    /**
     * Attempts to move in a given direction. Fails if it is impossible or would lead to walking into water.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        while (!rc.isReady()) {
            rp.endTurn();
        }
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
     * Returns directions in order of how much they tend to be in the given
     * direction
     *
     * @param d the given direction
     * @return an array of 8 directions
     */
    static Direction[] directionsOf(Direction d) {
        int ind = indInDirList(d);
        return new Direction[]{d, directions[(ind + 1) % 8], directions[(ind + 7) % 8],
                directions[(ind + 2) % 8], directions[(ind + 6) % 8],
                directions[(ind + 3) % 8], directions[(ind + 5) % 8],
                directions[(ind + 4) % 8]};
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