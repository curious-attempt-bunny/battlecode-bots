package fighter.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Tank extends BaseRobot {

    private final int rallyIndex;
    private final boolean rightRotation;
    private int movePauseTicks = 0;

    public Tank(RobotController _rc) throws GameActionException {
        super(_rc);
        facing = rc.getLocation().directionTo(rc.senseHQLocation()).opposite();
        rallyIndex = rand.nextInt(rc.senseEnemyTowerLocations().length);
        rightRotation = rand.nextBoolean();
    }

    @Override
    protected void act() throws GameActionException {
        MapLocation rallyPoint = getRallyPoint();

        if (rc.isWeaponReady()) {
            attackSomething();
        }

        if (rc.getWeaponDelay() > 0 && inRangeToAttack()) {
            movePauseTicks--;
            facing = rc.getLocation().directionTo(rallyPoint);
            return;
        }

        if (rand.nextInt(10) == 0) {
            facing = rc.getLocation().directionTo(rallyPoint);
        }

        int nearbyTanks = countOfNearbyFriendly(RobotType.TANK, 14);
        MapLocation tower =  nearbyEnemyTower();
        if (tower == null) {
            tower = rc.senseEnemyHQLocation();
        }

        int distanceToTower = rc.getLocation().distanceSquaredTo(tower);
        rc.setIndicatorString(2, "Distance to tower is "+distanceToTower+" with nearby tanks "+nearbyTanks);

        if (rc.isCoreReady() && nearbyTanks >= 3) {
            if (distanceToTower <= RobotType.TOWER.attackRadiusSquared + 10) {
                facing = rc.getLocation().directionTo(tower);

                int retries = 8;
                while (rc.isCoreReady() && retries > 0) {
                    if (rc.canMove(facing)) {
                        rc.move(facing);
                    }
                    if (rc.isCoreReady()) {
                        facing = rightRotation ? facing.rotateRight() : facing.rotateLeft();
                    }
                    retries--;
                }
            }
        }


        boolean relayVisible = isRelayVisible();
        rc.setIndicatorString(0, "relay visible? = "+relayVisible);
        if (rc.isCoreReady()) {
            if ((rc.getSupplyLevel() >= 100 && relayVisible  && distanceToTower >= RobotType.TOWER.attackRadiusSquared + 2)
                    || nearbyTanks >= tankPushThreshold()) {
                int retries = 8;
                while (rc.isCoreReady() && retries > 0) {
                    if (!isAttackableByEnemyTowers(rc.getLocation().add(facing)) && rc.canMove(facing)) {
                        rc.move(facing);
                    }
                    if (rc.isCoreReady()) {
                        facing = rightRotation ? facing.rotateRight() : facing.rotateLeft();
                    }
                    retries--;
                }
            }
        }
    }

    private int tankPushThreshold() {
        if (Clock.getRoundNum() > 1900) {
            return 1;
        } else if (Clock.getRoundNum() > 1800) {
            return 2;
        } else if (Clock.getRoundNum() > 1500) {
            return 3;
        } else {
            return 5;
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


    private MapLocation nearbyEnemyTower() {
        MapLocation[] towers = rc.senseEnemyTowerLocations();
        double bestDistance = Double.MAX_VALUE;
        MapLocation bestTower = null;

        for(MapLocation tower : towers) {
            double distance = rc.getLocation().distanceSquaredTo(tower);
            if (distance < bestDistance) {
                bestTower = tower;
                bestDistance = distance;
            }
        }

        return bestTower;
    }
}
