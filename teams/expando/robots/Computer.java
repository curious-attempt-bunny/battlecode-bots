package expando.robots;

import battlecode.common.*;

/**
 * Created by home on 1/18/15.
 */
public class Computer extends BaseRobot {

    private Integer rallyIndex;
    private boolean searchingForStation;

    public Computer(RobotController _rc) {
        super(_rc);
        searchingForStation = true;
        if (rand.nextInt(2) == 0) {
            facing = rc.getLocation().directionTo(rc.senseHQLocation()).opposite();
            int towers = rc.senseEnemyTowerLocations().length;
            rallyIndex = towers == 0 ? 0 : rand.nextInt(towers);
        }
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady()) {
//            int bestDistance = Integer.MAX_VALUE;
//            if (searchingForStation) {
//                for(RobotInfo r : nearby()) {
//                    if (isSupplyRelay(r.type)) {
//                        int distance = rc.getLocation().distanceSquaredTo(r.location);
//                        if (distance < bestDistance) {
//                            bestDistance = distance;
//                        }
//                    }
//                }
//
//                if (bestDistance == GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED
//                        || bestDistance == GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED-1
//                        || bestDistance == Integer.MAX_VALUE) {
//                searchingForStation = false;
//                }
//            }

            if (rallyIndex != null && rand.nextInt(10) == 0) {
                facing = rc.getLocation().directionTo(getRallyPoint());
            }

            // ahead is bad for building but left or right is good.
            if (!supplyBorder(facing)) {
                if (supplyBorder(facing.rotateLeft().rotateLeft())) {
                    facing = facing.rotateLeft().rotateLeft();
                } else if (supplyBorder(facing.rotateRight().rotateRight())) {
                    facing = facing.rotateRight().rotateRight();
                }
            }

//            rc.setIndicatorString(0, "Searching for station = "+searchingForStation+" best distance = "+bestDistance+ "(vs "+GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED+")");
            if (searchingForStation) {
                int retries = 8;
                while (rc.isCoreReady() && retries > 0) {
                    MapLocation target = rc.getLocation().add(facing);
                    if (!isAttackableByEnemyTowers(target) && rc.canMove(facing)) {
                        rc.move(facing);
                        if (rc.readBroadcast(200 + coordinateSystem.broadcastOffsetForNormalizated(coordinateSystem.toNormalized(target))) == 1) {
                            searchingForStation = false;
                        }
                    }
                    if (rc.isCoreReady()) {
                        facing = facing.rotateRight();
                    }
                    retries--;
                }
            }
        }
    }

    private MapLocation getRallyPoint() {
        MapLocation rallyPoint;
        MapLocation[] towers = rc.senseEnemyTowerLocations();
        if (rallyIndex >= towers.length) {
            rallyPoint = rc.senseEnemyHQLocation();
        } else {
            rallyPoint = towers[rallyIndex%towers.length];
        }
        return rallyPoint;
    }

}
