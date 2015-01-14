package template.robots;

import battlecode.common.*;

import java.util.Random;

/**
 * Created by home on 1/6/15.
 */
public abstract class BaseRobot {
    protected final RobotController rc;
    protected final int myRange;
    protected final Team myTeam;
    protected final Team enemyTeam;
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    protected final Random rand;
    protected Direction facing;

    protected BaseRobot(RobotController _rc) {
        rc = _rc;
        myRange = rc.getType().attackRadiusSquared;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        rand = new Random(rc.getID());
        facing = directions[rand.nextInt(8)];
    }

    public final void run() {
        while(true) {
            try {
                act();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            rc.yield();
        }
    }

    protected abstract void act() throws GameActionException;

    // This method will attack an enemy in sight, if there is one
    protected boolean attackSomething() throws GameActionException {
        RobotInfo target = null;
        double attackRatio = Double.MAX_VALUE;

        RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
        if (enemies.length > 0) {
            target = enemies[0];
        }

        for(RobotInfo info : enemies) {
            if (info.type == RobotType.TOWER) {
                target = info;
                break;
            }
            double ratio = info.health; //info.type.attackPower / info.health;
            if (ratio > attackRatio) {
                target = info;
                attackRatio = ratio;
            }
        }

        if (target != null) {
            rc.attackLocation(target.location);
            return true;
        } else {
            return false;
        }
    }

    // This method will attempt to move in Direction d (or as close to it as possible)
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

    // This method will attempt to spawn in the given direction (or as close to it as possible)
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

    // This method will attempt to build in the given direction (or as close to it as possible)
    protected void tryBuild(Direction d, RobotType type) throws GameActionException {
        int offsetIndex = 0;
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        int dirint = directionToInt(d);
        boolean blocked = false;
        while (offsetIndex < 8 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
            offsetIndex++;
        }
        if (offsetIndex < 8) {
            rc.build(directions[(dirint+offsets[offsetIndex]+8)%8], type);
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
        double mySupply = rc.getSupplyLevel();
        if (mySupply > 250) {
            RobotInfo[] myRobots = rc.senseNearbyRobots(15, myTeam);
            MapLocation target = null;
            double supply = Double.MAX_VALUE;

            for(RobotInfo info : myRobots) {
                if (info.supplyLevel < supply) {
                    target = info.location;
                    supply = info.supplyLevel;
                }
            }

            if (supply < mySupply/2) {
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
}
