package energizer.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Tank extends BaseRobot {

    private int movePauseTicks = 0;

    public Tank(RobotController _rc) throws GameActionException {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        MapLocation rallyPoint = rc.senseEnemyHQLocation();

        if (rc.isWeaponReady()) {
            attackSomething();
        }

        if (rc.getWeaponDelay() > 0 && inRangeToAttack()) {
            movePauseTicks--;
            facing = rc.getLocation().directionTo(rallyPoint);
            return;
        }

        int nearbyTanks = countOfNearbyFriendly(RobotType.TANK, 10);
        rc.setIndicatorString(2, "Nearby tanks "+nearbyTanks);
        if (rc.isCoreReady() && nearbyTanks >= 3) {
            MapLocation tower =  nearbyEnemyTower();
            if (tower == null) {
                tower = rc.senseEnemyHQLocation();
            }

            int distanceToTower = rc.getLocation().distanceSquaredTo(tower);
            rc.setIndicatorString(2, "Distance to tower is "+distanceToTower+" with nearby tanks "+nearbyTanks);
            if (distanceToTower <= RobotType.TOWER.attackRadiusSquared + 10) {
                facing = rc.getLocation().directionTo(tower);

                int retries = 8;
                while (rc.isCoreReady() && retries > 0) {
                    if (rc.canMove(facing)) {
                        rc.move(facing);
                    }
                    if (rc.isCoreReady()) {
                        facing = facing.rotateRight();
                    }
                    retries--;
                }
            }
        }


        boolean relayVisible = isRelayVisible();
        rc.setIndicatorString(0, "relay visible? = "+relayVisible);
        if (rc.isCoreReady() && rc.getSupplyLevel() >= 100 && relayVisible) {
            int retries = 8;
            while (rc.isCoreReady() && retries > 0) {
                if (!isAttackableByEnemyTowers(rc.getLocation().add(facing)) && rc.canMove(facing)) {
                    rc.move(facing);
                }
                if (rc.isCoreReady()) {
                    facing = facing.rotateRight();
                }
                retries--;
            }
//        } else if (rand.nextInt(50) == 0) {
//            tryMove(facing.opposite());
        }
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

    private int countOfNearbyFriendly(RobotType type, int radiusSquared) {
        int count = 0;
        RobotInfo[] friendlies = rc.senseNearbyRobots(radiusSquared, myTeam);
        for(RobotInfo r : friendlies) {
            if (r.type == type) {
                count++;
            }
        }
        return count;
    }
}
