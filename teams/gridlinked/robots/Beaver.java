package gridlinked.robots;

import battlecode.common.*;
import gridlinked.CoordinateSystem;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    private final CoordinateSystem coordinateSystem;

    public Beaver(RobotController _rc) {
        super(_rc);
        facing = rc.getLocation().directionTo(rc.senseHQLocation()).opposite();
        coordinateSystem = new CoordinateSystem(rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isWeaponReady()) {
            attackSomething();
        }

        // nynyn
        // ynnny
        // nnnnn
        // ynnny
        // nynyn

        if (rc.isCoreReady()) {
            if (!isRelayVisible()) {
                Direction buildDirection = getBuildDirection();
                if (buildDirection != null) {
                    if (countOf(RobotType.TANK) == 0 || countOfNearbyFriendly(RobotType.TANK, 2*GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) > 0) {
                        boolean built = false;
                        if (rc.getTeamOre() >= RobotType.MINERFACTORY.oreCost && countOf(RobotType.MINERFACTORY) == 0) {
    //                        Direction bestOrdeBuildDirection = bestOreBuildDirection();
    //                        if (bestOrdeBuildDirection != null) {
    //                            buildDirection = bestOrdeBuildDirection;
    //                        }
                            built = tryBuild(buildDirection, RobotType.MINERFACTORY);
                        } else if (rc.getTeamOre() >= RobotType.BARRACKS.oreCost && countOf(RobotType.BARRACKS) == 0 && countOf(RobotType.MINER) >= 5) {
                            built = tryBuild(buildDirection, RobotType.BARRACKS);
                        } else if (rc.getTeamOre() >= RobotType.TANKFACTORY.oreCost && rc.hasBuildRequirements(RobotType.TANKFACTORY) &&
                                (countOf(RobotType.TANKFACTORY) == 0 || rc.getTeamOre() > Math.min(3000, 1500*countOf(RobotType.TANKFACTORY)))) {
                            built = tryBuild(buildDirection, RobotType.TANKFACTORY);
                        } else if (rc.getTeamOre() >= RobotType.TECHNOLOGYINSTITUTE.oreCost && countOf(RobotType.TANK) > 0 && countOfNearbyFriendly(RobotType.TECHNOLOGYINSTITUTE, 20*20) == 0) {
                            built = tryBuild(buildDirection, RobotType.TECHNOLOGYINSTITUTE);
                        } else if (rc.getTeamOre() >= RobotType.SUPPLYDEPOT.oreCost && 5*countOf(RobotType.SUPPLYDEPOT) < 4*countOf(RobotType.COMPUTER)) {
                            built = tryBuild(buildDirection, RobotType.SUPPLYDEPOT);
                        }

                        if (rc.isCoreReady()) {
                            mineAndTrack();
                        }

                        if (built || (rand.nextInt(15) == 0 && rc.getSupplyLevel() < 100)) {
                            facing = rc.getLocation().directionTo(rc.senseHQLocation());
                        }
                    }
                    return;
                }
            }

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
        }
    }

    static int[][] buildLocations = new int[][] {
            new int[] { 0, 1, 0, 1, 0 },
            new int[] { 1, 0, 0, 0, 1 },
            new int[] { 0, 0, 0, 0, 0 },
            new int[] { 1, 0, 0, 0, 1 },
            new int[] { 0, 1, 0, 1, 0 },
    };

    private Direction getBuildDirection() {
        MapLocation normalizedLocation = coordinateSystem.toNormalized(rc.getLocation());
        int gx = normalizedLocation.x % 5;
        int gy = normalizedLocation.y % 5;
        Direction direction = null;

        for(Direction d : directions) {
            if (rc.senseTerrainTile(rc.getLocation().add(d)).isTraversable()) {
                int x = (5 + d.dx + gx)%5;
                int y = (5 + d.dy + gy)%5;

                if (buildLocations[x][y] == 1) {
                    direction = d;
                    rc.setIndicatorDot(rc.getLocation().add(d), 100, 100, 100);
                    break;
                } else {
                    rc.setIndicatorDot(rc.getLocation().add(d), 0, 0, 0);
                }
            }
        }

        return direction;
    }
}


