package optimo.robots;

import battlecode.common.*;
import optimo.CoordinateSystem;

import java.util.Random;

/**
 * Created by home on 1/6/15.
 */
public abstract class BaseRobot {
    public static final int TOTAL_MINED = 100;
    public static final int TOTAL_KILLS = 101;
    public static final int BORDER_MAP = 200;
    public static final int COMMAND_COUNT = 1000 + BORDER_MAP + CoordinateSystem.MAP_WIDTH* CoordinateSystem.MAP_HEIGHT;
    public static final int COMMAND_LIST = COMMAND_COUNT+1;
    public static final int COMMAND_WIDTH = 2;

    public RobotController rc;
    protected final int myRange;
    protected final Team myTeam;
    protected final Team enemyTeam;
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    protected final Random rand;
    protected Direction facing;
    private RobotInfo[] nearby;
    private Boolean congested;
    protected Direction planDirection;
    private Double minedBefore;
    protected final CoordinateSystem coordinateSystem;
    protected MapLocation commandTarget;

    protected BaseRobot(RobotController _rc) {
        rc = _rc;
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        rand = new Random(rc.getID());
        facing = directions[rand.nextInt(8)];
        coordinateSystem = new CoordinateSystem(rc);
        commandTarget = null;
    }

    public final void run() {
        while(true) {
            try {
                if (minedBefore != null) {
                    double minedAfter = rc.senseOre(rc.getLocation());
                    double mined = minedBefore - minedAfter;
                    int globallyMined = rc.readBroadcast(TOTAL_MINED) + (int) (mined*1000);
                    rc.broadcast(TOTAL_MINED, globallyMined);
                    rc.addMatchObservation(this.getClass().getCanonicalName()+": Globally mined: "+globallyMined);

                    minedBefore = null;
                }

                nearby = null;
                congested = null;

                act();
                transferSupply();
                compute();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    protected void compute() throws GameActionException {

    }

    protected abstract void act() throws GameActionException;

    protected boolean attackSomething() throws GameActionException {
        RobotInfo target = null;
        double attackRatio = Double.MIN_VALUE;

        RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
        if (enemies.length > 0) {
            target = enemies[0];
        }

        for(RobotInfo info : enemies) {
            if (info.type == RobotType.TOWER) {
                target = info;
                break;
            }
            double ratio = -info.health;
            if (ratio > attackRatio || (ratio == attackRatio && rand.nextBoolean())) {
                target = info;
                attackRatio = ratio;
            }
        }

        if (target != null) {
            rc.attackLocation(target.location);
            if (rc.getType().attackPower >= target.health) {
                int totalKills = rc.readBroadcast(TOTAL_KILLS) + 1;
                rc.broadcast(TOTAL_KILLS, totalKills);
                rc.addMatchObservation(this.getClass().getCanonicalName()+": Total kills: "+totalKills);
            }
            return true;
        } else {
            return false;
        }
    }

    protected void tryMove(Direction d) throws GameActionException {
        int offsetIndex = 0;
        int[] offsets = {0,1,-1,2,-2};
        int dirint = directionToInt(d);
        boolean blocked = false;
        while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
            offsetIndex++;
        }
        if (offsetIndex < 5) {
            facing = directions[(dirint+offsets[offsetIndex]+8)%8];
            rc.move(facing);
        }
    }

    protected void trySpawn(Direction d, RobotType type) throws GameActionException {
        int offsetIndex = 0;
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        int dirint = directionToInt(d);
        boolean blocked = false;
        while (offsetIndex < 8 && !rc.canSpawn(directions[(dirint+offsets[offsetIndex]+8)%8], type)) {
            offsetIndex++;
        }
        if (offsetIndex < 8) {
            rc.spawn(directions[(dirint+offsets[offsetIndex]+8)%8], type);
        }
    }

    protected boolean tryBuild(Direction d, RobotType type) throws GameActionException {
        int offsetIndex = 0;
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        int dirint = directionToInt(d);
        boolean blocked = false;
        while (offsetIndex < 8 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
            offsetIndex++;
        }
        if (offsetIndex < 8) {
            rc.build(directions[(dirint+offsets[offsetIndex]+8)%8], type);
            rc.broadcast(type.ordinal(), rc.readBroadcast(type.ordinal()) + 1);
            return true;
        } else {
            return false;
        }
    }

    protected int directionToInt(Direction d) {
        switch(d) {
            case NORTH:
                return 0;
            case NORTH_EAST:
                return 1;
            case EAST:
                return 2;
            case SOUTH_EAST:
                return 3;
            case SOUTH:
                return 4;
            case SOUTH_WEST:
                return 5;
            case WEST:
                return 6;
            case NORTH_WEST:
                return 7;
            default:
                return -1;
        }
    }

    protected int countOf(RobotType type) throws GameActionException {
        return rc.readBroadcast(type.ordinal());
    }

    protected void mineOrMove() throws GameActionException {
        double oreHere = rc.senseOre(rc.getLocation());

        int mineThreshold;
        if (oreHere <= 2) {
            mineThreshold = 5;
        } else if (oreHere <= 8) {
            mineThreshold = 30;
        } else if (oreHere <= 15) {
            mineThreshold = 50;
        } else if (oreHere <= 30) {
            mineThreshold = 80;
        } else {
            mineThreshold = 90;
        }

        if (rand.nextInt(100) < mineThreshold) {
            rc.mine();
        } else {
            Direction bestDirection = facing;
            double bestOre = rc.senseOre(rc.getLocation().add(bestDirection));
            if (!rc.canMove(bestDirection)) bestOre = Double.MIN_VALUE;
            for(Direction dir : directions) {
                MapLocation square = rc.getLocation().add(dir);
                if (rc.canMove(dir)) {
                    double ore = rc.senseOre(square);
                    if (ore > bestOre) {
                        bestDirection = dir;
                        bestOre = ore;
                    }
                }
            }

            if (rc.canMove(bestDirection)) {
                rc.move(bestDirection);
            }
        }
    }

    protected void transferSupply() throws GameActionException {
        if (Clock.getBytecodesLeft() < 1000) return;
        double mySupply = rc.getSupplyLevel();
        if (mySupply > 250) {
            RobotInfo[] myRobots = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
            MapLocation target = null;

            for(RobotInfo info : myRobots) {
                if (Clock.getBytecodesLeft() <= 600) return;
                if (info.supplyLevel < mySupply/2) {
                    target = info.location;
                    break;
                }
            }

            if (target != null && Clock.getBytecodesLeft() > 600) {
                rc.transferSupplies((int)(mySupply/2), target);
            }
        }
    }

    protected boolean canSeeBuilding() throws GameActionException {
        RobotInfo[] myRobots = rc.senseNearbyRobots(12, myTeam);
        MapLocation target = null;

        for(RobotInfo info : myRobots) {
            if (info.type.isBuilding) {
                target = info.location;
                break;
            }
        }

        return target != null;
    }

    protected boolean inRangeToAttack() throws GameActionException {
        RobotInfo[] myRobots = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, enemyTeam);
        MapLocation target = null;

        for(RobotInfo info : myRobots) {
            target = info.location;
            break;
        }

        return target != null;
    }

    protected boolean isCongested() {
        if (congested != null) return congested;

        int clearCount = 0;
        for(Direction d : directions) {
            MapLocation target = rc.getLocation();
            boolean clear = true;
            for(int extent = 0; extent<3; extent++) {
                target = target.add(d);
                TerrainTile terrainTile = rc.senseTerrainTile(target);
                if (terrainTile != TerrainTile.NORMAL) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                clearCount++;
            }
        }

        congested = clearCount < 6;
        return congested;
    }

    protected void planMoveAway() {
        int sumX = 0;
        int sumY = 0;
        double count = 0;

        for(RobotInfo r : nearby()) {
            int multiplier = (isSupplyRelay(r.type) ? 1 : 10);
            sumX += multiplier*(r.location.x - rc.getLocation().x);
            sumY += multiplier*(r.location.y - rc.getLocation().y);
            count += multiplier;
        }

        double x = -(sumX / count);
        double y = -(sumY / count);

        if (count > 0) {
            planDirection = vectorToDirection(x,y);
        }
    }

    private Direction vectorToDirection(double x, double y) {
        if (x < 0) {
            if (y < 0) {
                return Direction.NORTH_WEST;
            } else if (y > 0) {
                return Direction.SOUTH_WEST;
            } else { // y == 0
                return Direction.WEST;
            }
        } else if (x > 0) {
            if (y < 0) {
                return Direction.NORTH_EAST;
            } else if (y > 0) {
                return Direction.SOUTH_EAST;
            } else { // y == 0
                return Direction.EAST;
            }
        } else { // x == 0
            if (y < 0) {
                return Direction.NORTH;
            } else if (y > 0) {
                return Direction.SOUTH;
            } else { // y == 0
                return planDirection;
            }
        }
    }

    protected boolean isSupplyRelay(RobotType type) {
        return type == RobotType.HQ
                || type == RobotType.TANKFACTORY
                || type == RobotType.MINERFACTORY
                || type == RobotType.BARRACKS
                || type == RobotType.TOWER
                || type == RobotType.SUPPLYDEPOT
                || type == RobotType.BEAVER
                || type == RobotType.TECHNOLOGYINSTITUTE
                || type == RobotType.COMPUTER;
    }


    protected boolean isRelayVisible() {
        boolean isRelayVisible = false;

        for(RobotInfo r : nearby()) {
            if (isSupplyRelay(r.type)) {
                isRelayVisible = true;
                break;
            }
        }
        return isRelayVisible;
    }

    protected int relayCount() {
        int count = 0;

        for(RobotInfo r : nearby()) {
            if (isSupplyRelay(r.type)) {
                count++;
            }
        }
        return count;
    }

    protected RobotInfo[] nearby() {
        if (nearby == null) {
            nearby = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);
        }
        return nearby;
    }

    protected boolean isAttackableByEnemyTowers(MapLocation target) {
        MapLocation[] towers = rc.senseEnemyTowerLocations();
        boolean attackable = false;
        for(MapLocation loc : towers) {
            if (target.distanceSquaredTo(loc) <= RobotType.TOWER.attackRadiusSquared) {
                attackable = true;
                break;
            }
        }

        return attackable;
    }

    protected int countOfNearbyFriendly(RobotType type, int radiusSquared) {
        int count = 0;
        RobotInfo[] friendlies = rc.senseNearbyRobots(radiusSquared, myTeam);
        for(RobotInfo r : friendlies) {
            if (r.type == type) {
                count++;
            }
        }
        return count;
    }

    protected int countOfNearbyEnemy(RobotType type, int radiusSquared) {
        int count = 0;
        RobotInfo[] friendlies = rc.senseNearbyRobots(radiusSquared, enemyTeam);
        for(RobotInfo r : friendlies) {
            if (r.type == type) {
                count++;
            }
        }
        return count;
    }

    protected boolean isHeavyTraffic() {
        RobotInfo[] robotInfos = rc.senseNearbyRobots(10, myTeam);

        return robotInfos.length >= 5;
    }

    protected Direction bestOreSpawnDirection() {
        double bestOre = Double.MIN_VALUE;
        Direction bestDirection = null;

        for(Direction d : directions) {
            double ore = rc.senseOre(rc.getLocation().add(d));
            if (rc.canSpawn(d, RobotType.MINER)) {
                if (bestDirection == null || ore > bestOre || (ore == bestOre && rand.nextBoolean())) {
                    bestOre = ore;
                    bestDirection = d;
                }
            }
        }
        return bestDirection;
    }

    protected Direction bestOreBuildDirection() {
        double bestOre = Double.MIN_VALUE;
        Direction bestDirection = null;

        for(Direction d : directions) {
            double ore = rc.senseOre(rc.getLocation().add(d));
            if (rc.canBuild(d, RobotType.MINERFACTORY)) {
                if (ore > bestOre || (ore == bestOre && rand.nextBoolean())) {
                    bestOre = ore;
                    bestDirection = d;
                }
            }
        }
        return bestDirection;
    }

    protected void mineAndTrack() throws GameActionException {
        minedBefore = rc.senseOre(rc.getLocation());
        rc.mine();
    }

    protected boolean supplyBorder(Direction direction) throws GameActionException {
        MapLocation target = rc.getLocation();
        boolean desired = false;

        for(int extent = 0; extent<3; extent++) {
            target = target.add(direction);
            TerrainTile terrainTile = rc.senseTerrainTile(target);
            if (terrainTile != TerrainTile.NORMAL) {
                break;
            } else if (rc.readBroadcast(BORDER_MAP + coordinateSystem.broadcastOffsetForNormalizated(coordinateSystem.toNormalized(target))) == 1) {
                desired = true;
                break;
            }
        }

        return desired;
    }

    protected void updateCommandTarget() throws GameActionException {
        if (rc.isCoreReady()) {
            if (commandTarget != null) {
                if (rc.canSenseLocation(commandTarget) && rc.isLocationOccupied(commandTarget)) {
//                    System.out.println("Command target is occupied. Done: "+commandTarget);
                    commandTarget = null;
                }
            }

            if (commandTarget == null && Command.commandCount(rc) > 0) {
                Command command = Command.getRandomCommand(rc);
                commandTarget = coordinateSystem.toGame(command.target);
//                System.out.println("New command target: "+commandTarget);
                rc.setIndicatorString(1, "Command target: "+commandTarget);
                facing = rc.getLocation().directionTo(commandTarget);
            }
        }
    }
}
