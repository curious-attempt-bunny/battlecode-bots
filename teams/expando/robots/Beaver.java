package expando.robots;

import battlecode.common.*;
import expando.CoordinateSystem;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    private final CoordinateSystem coordinateSystem;
    private final Set<MapLocation> builtLocations = new HashSet<MapLocation>();

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

        if (rc.isCoreReady()) {
            Direction buildDirection = getBuildDirection();
            int relayCount = relayCount();
            if (relayCount == 1) {
                if (rc.getTeamOre() >= RobotType.TANKFACTORY.oreCost && rc.hasBuildRequirements(RobotType.TANKFACTORY) &&
                        (countOf(RobotType.TANKFACTORY) == 0 || rc.getTeamOre() > Math.min(3000, 1500*countOf(RobotType.TANKFACTORY))) &&
                        (!isCongested() || rc.getTeamOre() > 2000)) {
                    doBuild(buildDirection, RobotType.TANKFACTORY);
                }
            } else if (relayCount == 0) {
                if (buildDirection != null) {

                    boolean built = false;
                    if (countOf(RobotType.MINERFACTORY) == 0) {
                        if (rc.getTeamOre() >= RobotType.MINERFACTORY.oreCost) {
    //                        Direction bestOrdeBuildDirection = bestOreBuildDirection();
    //                        if (bestOrdeBuildDirection != null) {
    //                            buildDirection = bestOrdeBuildDirection;
    //                        }
                            built = tryBuild(buildDirection, RobotType.MINERFACTORY);
                        }
                    } else if (countOf(RobotType.BARRACKS) == 0) {
                        if (rc.getTeamOre() >= RobotType.BARRACKS.oreCost) {
                            built = doBuild(buildDirection, RobotType.BARRACKS);
                        }
                    } else if (countOf(RobotType.TANK) == 0 || countOfNearbyFriendly(RobotType.TANK, 2 * GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) > 0 || 2*rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()) > 3*rc.getLocation().distanceSquaredTo(rc.senseHQLocation())) {
                        if (rc.getTeamOre() >= RobotType.TANKFACTORY.oreCost && rc.hasBuildRequirements(RobotType.TANKFACTORY) &&
                                (countOf(RobotType.TANKFACTORY) == 0 || rc.getTeamOre() > Math.min(3000, 1500*countOf(RobotType.TANKFACTORY))) &&
                                (!isCongested() || rc.getTeamOre() > 2000)) {
                            built = doBuild(buildDirection, RobotType.TANKFACTORY);
//                        } else if (rc.getTeamOre() >= RobotType.TECHNOLOGYINSTITUTE.oreCost && (congested || countOf(RobotType.TANK) > 0) && countOfNearbyFriendly(RobotType.TECHNOLOGYINSTITUTE, 20*20) == 0) {
//                            built = doBuild(buildDirection, RobotType.TECHNOLOGYINSTITUTE);
                        } else if (rc.getTeamOre() >= RobotType.SUPPLYDEPOT.oreCost) { // && 5*countOf(RobotType.SUPPLYDEPOT) < 4*countOf(RobotType.COMPUTER)) {
                            built = doBuild(buildDirection, RobotType.SUPPLYDEPOT);
                        }
                    }

                    if (rc.isCoreReady()) {
                        mineAndTrack();
                    }

                    if (built || (rand.nextInt(15) == 0 && rc.getSupplyLevel() < 100)) {
                        facing = rc.getLocation().directionTo(rc.senseHQLocation());
                    }
                    return;
                }
            }

            RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared + 2, enemyTeam);
            int retries = 8;
            while (rc.isCoreReady() && retries > 0) {
                MapLocation nextLocation = rc.getLocation().add(facing);
                boolean attackable = isAttackableByEnemyTowers(nextLocation);
                MapLocation retreatFrom = null;

                if (!attackable) {
                    for(RobotInfo r : enemies) {
                        if (nextLocation.distanceSquaredTo(r.location) <= r.type.attackRadiusSquared+2) {
                            attackable = true;
                            retreatFrom = r.location;
                            break;
                        }
                    }
                }

                if (!attackable && rc.canMove(facing)) {
                    rc.move(facing);
                } else if (retreatFrom != null) {
                    facing = retreatFrom.directionTo(rc.getLocation());
                    tryMove(facing);
                } else if (rc.isCoreReady()) {
                    facing = facing.rotateRight();
                }
                retries--;
            }
        }
    }

    private boolean doBuild(Direction buildDirection, RobotType type) throws GameActionException {
        rc.build(buildDirection, type);
        builtLocations.add(rc.getLocation().add(buildDirection));
        return true;
    }

    private Direction getBuildDirection() {
        return facing.rotateRight().rotateRight().rotateRight();
    }
}


