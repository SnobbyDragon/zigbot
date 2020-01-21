package zigbotplayer;

import battlecode.common.*;

import java.util.*;

/* Chris Kimmel was here */
public strictfp class RobotPlayer {
    public static RobotController rc;

    // phase where miners go away from HQ and let landscapers build
    static final int HQ_WALL_PHASE = 260;

    static int TEAM_HASH;
    static int MAP_HEIGHT, MAP_WIDTH;

    static MapLocation HQLocation;
    /*
     * The turn number.
     */
    int turnCount;

    /**
     * The number of various types of robots built.
     */
    int miners = 0;
    int designSchools = 0;
    int drones = 0;
    int landscapers = 0;
    int fullfillmentCenters = 0;

    /**
     * The last round a message was read from
     */
    int messageReadFrom = 1;

    /**
     * Taxicab distance between two locations
     *
     * @param a a location on the map
     * @param b another location on the map
     * @return the taxicab distance between a and b
     */
    int taxicab(MapLocation a, MapLocation b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Box distance to find the minimum moves between two locations (since we can move diagonally. disregards obstacles)
     *
     * @param a a location on the map
     * @param b another location on the map
     * @return the minimum moves between a and b
     */
    int box(MapLocation a, MapLocation b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    /**
     * Gets all locations that are in sight of this robot.
     *
     * @return a set of locations that can be seen
     */
    public Set<MapLocation> inSight() {
        Set<MapLocation> seen = new HashSet<>();
        MapLocation st = rc.getLocation();
        Queue<MapLocation> toVisit = new LinkedList<>();
        toVisit.add(st);
        for (int i = 0; i < toVisit.size(); i++) {
            MapLocation top = toVisit.poll();
            for (Direction d : Movement.directions) {
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
    public static void run(RobotController rc) {
        RobotPlayer me = new RobotPlayer();
        try {
            // This is the RobotController object. You use it to perform actions from this robot,
            // and to get information on its current status.
            RobotPlayer.rc = rc;
            MAP_WIDTH = rc.getMapWidth();
            MAP_HEIGHT = rc.getMapHeight();
            TEAM_HASH = 387428419 + (rc.getTeam() == Team.A ? 0 : 1);
            System.out.println("I'm a " + rc.getType() + " and I just got created!");
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            switch (rc.getType()) {
                case HQ:
                    me = new HQ();
                    break;
                case MINER:
                    me = new Miner();
                    break;
                case REFINERY:
                    me = new Refinery();
                    break;
                case VAPORATOR:
                    break;
                case DESIGN_SCHOOL:
                    me = new DesignSchool();
                    break;
                case FULFILLMENT_CENTER:
                    me = new FulfillmentCenter();
                    break;
                case LANDSCAPER:
                    me = new Landscaper();
                    break;
                case DELIVERY_DRONE:
                    me = new DeliveryDrone();
                    break;
                case NET_GUN:
                    me = new NetGun();
                    break;
            }
        } catch (Exception e) {
            System.out.println(rc.getType() + " Has already broken, wow u suck");
            e.printStackTrace();
        }
        while (true) {
            try {
                me.runUnit();
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


    /*
     * Read messages to update map info.
     */
    void updateFromMessages() throws GameActionException {
        //List<Transaction> recentMessages = blockChainHandler.getMessages(messageReadFrom, rc.getRoundNum() - 1);
        for (; messageReadFrom < rc.getRoundNum(); messageReadFrom++) {
            for (Transaction t : rc.getBlock(messageReadFrom)) {//recentMessages) {
                int[] msg = t.getMessage();
                if(msg[0] == TEAM_HASH) {
                    System.out.println("THE MESSAGE WAS READ: " + Arrays.toString(msg));
                    if (msg[1] == 1) {//landscaper built
                        designSchools++;
                    } else if (msg[1] == 2) {
                        HQLocation = new MapLocation(msg[2], msg[3]);
                        if(this instanceof Landscaper){
                            ((Landscaper)(this)).updateHQLoc();
                        }
                    } else if (msg[1] == 3) {
                        fullfillmentCenters++;
                    }
                }
            }
        }
    }


    static class Message implements Comparable<Message> {
        int price;
        int[] msg;

        Message(int pr, int[] ms) {
            price = pr;
            msg = ms;
            assert ms.length == 6;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "price=" + price +
                    ", msg=" + Arrays.toString(msg) +
                    '}';
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
        if (!messageQueue.isEmpty()) {
            if (submitMessage(messageQueue.peek())) {
                System.out.println("THE MESSAGE WAS SUBMITTED: " + messageQueue.poll());
            }
        }
    }

    /*
     * keep trying to submit a message until it works with starting price and increment.
     * Message is an int array of size 6.
     */
    void submitMessage(int price, int[] msg) {
        messageQueue.add(new Message(price, msg));
    }

    /*
     * Submit a message!
     */
    private boolean submitMessage(Message m) throws GameActionException {
        int[] message = new int[7];
        message[0] = TEAM_HASH;
        System.arraycopy(m.msg, 0, message, 1, 6);
        if (rc.canSubmitTransaction(message, m.price)) {
            rc.submitTransaction(message, m.price);
            return true;
        }
        return false;
    }
}
