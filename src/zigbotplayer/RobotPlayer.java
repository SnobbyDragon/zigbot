package zigbotplayer;

import battlecode.common.*;

import java.util.*;

/* Chris Kimmel was here */
public strictfp class RobotPlayer {
    static RobotController rc;

    static int TEAM_HASH;

    int designSchools = 0;
    int messageReadFrom = 1;

    /**
     * An array of all directions excluding CENTER
     */
    Direction[] directions = {
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
     * Returns whether the given direction is not CENTER
     * @param d
     * 		the given direction
     * @return whether the given direction is not CENTER
     */
    int indInDirList(Direction d) {
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == d) {
                return i;
            }
        }
        throw new RuntimeException("Direction Index not found in list");
    }

    /**
     * Returns directions adjacent to the given direction
     * @param d
     * 		the given direction
     * @return an array of 3 directions
     */
    Direction[] generalDirectionOf(Direction d) {
        int ind = indInDirList(d);
        return new Direction[]{d, directions[(ind + 1) % 8], directions[(ind + 7) % 8]};
    }

    /**
     * returns whether the direction points north
     * @param d
     * 		the given direction
     * @return 1 if north, -1 if south, 0 otherwise
     */
    int directionNorth(Direction d) {
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
     * @param d
     * 		the given direction
     * @return 1 if east, -1 if west, 0 otherwise
     */
    int directionEast(Direction d) {
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
     * Finds the direction from a location to another
     * @param a
     * 		a location on the map
     * @param b
     * 		another location on the map
     * @return the direction from a to b
     */
    Direction directionToLoc(MapLocation a, MapLocation b) {
    	int dx = a.x - b.x;
    	int dy = a.y - b.y;
    	String dir = "";
    	if (dy < 0) { //a is to the south of b
    		dir += "SOUTH";
    	} else if (dy > 0) { //a is to the north of b
    		dir += "NORTH";
    	}
    	if (dx < 0) { //a is to the west of b
    		dir += "WEST";
    	} else if (dx > 0) { //a is to the east of b
    		dir += "EAST";
    	}
    	if (dir.equals("")) { //a and b are the same
    		dir = "CENTER";
    	}
    	return Direction.valueOf(dir);
    }
    
    /**
     * Taxicab distance between two locations
     * @param a
     * 		a location on the map
     * @param b
     * 		another location on the map
     * @return the taxicab distance between a and b
     */
    int taxicab(MapLocation a, MapLocation b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    
    /**
     * Box distance to find the minimum moves between two locations (since we can move diagonally. disregards obstacles)
     * @param a 
     * 		a location on the map
     * @param b
     * 		another location on the map
     * @return the minimum moves between a and b
     */
    int box(MapLocation a, MapLocation b) {
    	return Math.max(a.x - b.x, a.y - b.y);
    }

    RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    int turnCount;

    /**
     * Returns the opposite direction of the given direction
     * @param d
     * 		the given direction
     * @return the opposite direction
     * @throws RuntimeException
     */
    public Direction oppositeDirection(Direction d) throws RuntimeException {
        return directions[(4 + indInDirList(d)) % 8];
    }

    
    public Set<MapLocation> inSight() {
        Set<MapLocation> seen = new HashSet<>();
        MapLocation st = rc.getLocation();
        Queue<MapLocation> toVisit = new LinkedList<>();
        toVisit.add(st);
        for (int i = 0; i < toVisit.size(); i++) {
            MapLocation top = toVisit.poll();
            for (Direction d : directions) {
                MapLocation nxt = top.add(d);
                if (!seen.contains(nxt) && rc.canSenseLocation(nxt)) {
                    toVisit.add(nxt);
                    seen.add(nxt);
                }
            }
        }
        return seen;
    }

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer me = new RobotPlayer();
        RobotPlayer.rc = rc;
        TEAM_HASH = 387428419 + (rc.getTeam() == Team.A ? 0 : 1);
        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
        while (true) {
            try {
                switch (rc.getType()) {
                    case HQ:
                        me = new HQ();
                        me.runUnit();
                        break;
                    case MINER:
                        me = new Miner();
                        me.runUnit();
                        break;
                    case REFINERY:
                        me.runRefinery();
                        break;
                    case VAPORATOR:
                        me.runVaporator();
                        break;
                    case DESIGN_SCHOOL:
                        me.runDesignSchool();
                        break;
                    case FULFILLMENT_CENTER:
                        me.runFulfillmentCenter();
                        break;
                    case LANDSCAPER:
                        me.runLandscaper();
                        break;
                    case DELIVERY_DRONE:
                        me.runDeliveryDrone();
                        break;
                    case NET_GUN:
                        me.runNetGun();
                        break;
                }
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    public void runUnit() throws GameActionException {
        try {
            throw new RuntimeException("Running robot type not defined");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    void runVaporator() throws GameActionException {

    }

    void runDesignSchool() throws GameActionException {

    }

    void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions) {
            if (designSchools < rc.getTeamSoup() / 200) {
                tryBuild(RobotType.DELIVERY_DRONE, dir);
                endTurn();
            }
        }
    }

    void runLandscaper() throws GameActionException {
        moveAnywhere();
    }

    void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            moveAnywhere();
        }
    }

    void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    boolean moveAnywhere() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            if (rc.getCooldownTurns() >= 1) {
                endTurn();
            }
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir  The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            if (rc.getCooldownTurns() >= 1) {
                endTurn();
            }
            return true;
        } else return false;
    }



    /*
     * Read messages to update map info... does nothing rn.
     */
    void updateFromMessages() throws GameActionException {
        for (; messageReadFrom < rc.getRoundNum(); messageReadFrom++) {
            rc.getBlock(messageReadFrom);
        }
    }

    static class Message implements Comparable<Message>{
        int price;
        int[] msg;
        Message(int pr, int[] ms){
            price = pr;
            msg = ms;
        }
        @Override
        public int compareTo(Message o) {
            return price - o.price;
        }
    }
    PriorityQueue<Message> messageQueue = new PriorityQueue<>();
    void endTurn() throws GameActionException {
        Clock.yield();
        updateFromMessages();
        if(!messageQueue.isEmpty()){
            if(submitMessage(messageQueue.peek())){
                messageQueue.poll();
            }
        }
    }
    /*
     * keep trying to submit a message until it works with starting price and increment.
     * Message is an int array of size 6.
     */
    void submitMessage(int price, int[] msg) throws GameActionException {
        messageQueue.add(new Message(price, msg));
    }

    /*
     * Submit a message!
     */
    private boolean submitMessage(Message m) throws GameActionException {
        int[] message = new int[7];
        message[0] = TEAM_HASH;
        for(int i = 1; i < 7; i++){
            message[i] = m.msg[i-1];
        }
        if (rc.canSubmitTransaction(message, m.price)) {
            rc.submitTransaction(message, m.price);
            return true;
        }
        return false;
    }
}
