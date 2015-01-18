package energizer.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    private Direction planDirection;
    private RobotInfo[] nearby;
    private boolean congested;

    public Beaver(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady()) {
            nearby = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);

            congested = isCongested();

            if (!congested) {
                if (rc.getTeamOre() >= RobotType.MINERFACTORY.oreCost && countOf(RobotType.MINERFACTORY) == 0) {
                    tryBuild(planDirection.rotateLeft().rotateLeft(), RobotType.MINERFACTORY);
                } else if (rc.getTeamOre() >= RobotType.BARRACKS.oreCost && countOf(RobotType.BARRACKS) == 0) {
                    tryBuild(planDirection.rotateLeft().rotateLeft(), RobotType.BARRACKS);
                } else if (rc.getTeamOre() >= RobotType.TANKFACTORY.oreCost && countOf(RobotType.TANKFACTORY) == 0) {
                    tryBuild(planDirection.rotateLeft().rotateLeft(), RobotType.TANKFACTORY);
                }
            }
            if (congested) {
                planMoveAway();
            } else {
                planDirection = facing;
            }

            if (planDirection != null) {
                tryMove(planDirection);
                if (rc.isCoreReady()) {
                    tryMove(planDirection.rotateRight().rotateRight());
                }
            }
        }
    }

    private boolean isCongested() {
//        int xMin = rc.getLocation().x - 4;
//        int xMax = rc.getLocation().x + 4;
//        int yMin = rc.getLocation().y - 4;
//        int yMax = rc.getLocation().y + 4;
//        int obstructed = 0;
//
//        for(int x=xMin; x<=xMax; x++) {
//            for(int y=yMin; y<=yMax; y++) {
//                TerrainTile terrainTile = rc.senseTerrainTile(new MapLocation(x, y));
//                if (!terrainTile.isTraversable()) {
//                    obstructed++;
//                }
//            }
//        }
//
//        double ratio = obstructed / (9.0*9.0);
//        rc.setIndicatorString(1, "Obstruction ratio is "+ratio);
//
//        return ratio > 0.25;

        int clearCount = 0;
        for(Direction d : directions) {
            MapLocation target = rc.getLocation();
            boolean clear = true;
            for(int extent = 0; extent<5; extent++) {
                target = target.add(d);
                TerrainTile terrainTile = rc.senseTerrainTile(target);
                if (!terrainTile.isTraversable()) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                clearCount++;
            }
        }

        rc.setIndicatorString(1, "Obstruction: clear count = "+clearCount+". Congested? = "+(clearCount <= 3));

        return clearCount < 3;
    }

    private void planMoveAway() {
        int sumX = 0;
        int sumY = 0;
        double count = 0;

        for(RobotInfo r : nearby) {
            sumX += r.location.x - rc.getLocation().x;
            sumY += r.location.y - rc.getLocation().y;
            count++;
        }

        double x = -(sumX / count);
        double y = -(sumY / count);

        if (count > 0) {
            planDirection = vectorToDirection(x,y);
        } else {
//            planDirection = null;
        }

        rc.setIndicatorString(0, planDirection + " from vector " + x + "," + y);
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
}
