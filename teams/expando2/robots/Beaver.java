package expando2.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    public Beaver(RobotController _rc) {
        super(_rc);
        facing = rc.getLocation().directionTo(rc.senseHQLocation()).opposite();
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isWeaponReady()) {
            attackSomething();
        }

        if (rc.isCoreReady()) {
            Direction buildDirection = getBuildDirection();
            RobotType buildingToBuild = null;
            boolean shouldWait = false;
//            if (buildDirection != null) {
//                int relayCount = relayCount();
//                if (relayCount == 1) {
//                    if (rc.getTeamOre() >= RobotType.TANKFACTORY.oreCost && rc.hasBuildRequirements(RobotType.TANKFACTORY) &&
//                            (countOf(RobotType.TANKFACTORY) == 0 || rc.getTeamOre() > Math.min(3000, 1500*countOf(RobotType.TANKFACTORY))) &&
//                            (!isCongested() || rc.getTeamOre() > 2000)) {
//                        doBuild(buildDirection, RobotType.TANKFACTORY);
//                    }
//                } else if (relayCount == 0) {
//                    if (buildDirection != null) {

                        boolean built = false;
                        if (countOf(RobotType.MINERFACTORY) == 0) {
                            if (rc.getTeamOre() >= RobotType.MINERFACTORY.oreCost) {
        //                        Direction bestOrdeBuildDirection = bestOreBuildDirection();
        //                        if (bestOrdeBuildDirection != null) {
        //                            buildDirection = bestOrdeBuildDirection;
        //                        }
                                buildingToBuild = RobotType.MINERFACTORY;
                            } else {
                                rc.setIndicatorString(2, "Want to build: minerfactory");
                                shouldWait = true;
                            }
                        } else if (countOf(RobotType.BARRACKS) == 0) {
                            if (rc.getTeamOre() >= RobotType.BARRACKS.oreCost) {
                                buildingToBuild = RobotType.BARRACKS;
                            } else {
                                rc.setIndicatorString(2, "Want to build: barracks");
                                shouldWait = true;
                            }
                        } else if (countOf(RobotType.TANK) == 0 || countOfNearbyFriendly(RobotType.TANK, 2 * GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) > 0) { // || 2*rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()) > 3*rc.getLocation().distanceSquaredTo(rc.senseHQLocation())) {
                            shouldWait = !isCongested();
                            if (rc.getTeamOre() >= RobotType.TANKFACTORY.oreCost && rc.hasBuildRequirements(RobotType.TANKFACTORY) &&
                                    (countOf(RobotType.TANKFACTORY) == 0 || rc.getTeamOre() > Math.min(3000, 1500*countOf(RobotType.TANKFACTORY))) &&
                                    (!isCongested() || rc.getTeamOre() > 2000)) {
                                buildingToBuild = RobotType.TANKFACTORY;
                            } else {
                                shouldWait = true;
                                if (rc.getTeamOre() >= RobotType.TECHNOLOGYINSTITUTE.oreCost && countOfNearbyFriendly(RobotType.TECHNOLOGYINSTITUTE, 10*10) == 0) {
                                    buildingToBuild = RobotType.TECHNOLOGYINSTITUTE;
                                } else if (rc.getTeamOre() >= RobotType.SUPPLYDEPOT.oreCost && Math.ceil(countOf(RobotType.TANK)/3.0 + countOf(RobotType.COMPUTER)/8.0) > countOf(RobotType.SUPPLYDEPOT)) {
                                    buildingToBuild = RobotType.SUPPLYDEPOT;
                                } else {
                                    rc.setIndicatorString(2, "Round: "+Clock.getRoundNum()+". Want to build: nothing! (countOf(RobotType.TANK) = "+countOf(RobotType.TANK)+", countOf(RobotType.SUPPLYDEPOT) = "+countOf(RobotType.SUPPLYDEPOT)+", countOf(RobotType.COMPUTER) = "+countOf(RobotType.COMPUTER));
                                }
                            }
                        }
                        if (buildingToBuild != null) {
                            rc.setIndicatorString(2, "Round: "+Clock.getRoundNum()+". Find location to build: "+buildingToBuild.name());
                        }


//                        return;
//                    }
//                }
//            }
            if (buildDirection != null) {
                if (buildingToBuild != null) {
                    built = doBuild(buildDirection, buildingToBuild);
                }

                if (rc.isCoreReady()) {
                    if (shouldWait) {
                        mineAndTrack();
                    } else {
                        facing = rc.getLocation().directionTo(rc.senseHQLocation());
                    }
                }

//                if (built || (rand.nextInt(15) == 0 && rc.getSupplyLevel() < 100)) {
//                    facing = rc.getLocation().directionTo(rc.senseHQLocation());
//                }
            }

            updateCommandTarget();

            if (commandTarget != null && (built || rand.nextInt(2) == 0)) {
                facing = rc.getLocation().directionTo(commandTarget);
                rc.setIndicatorString(1, "Command target: "+(commandTarget.x-rc.getLocation().x)+","+(commandTarget.y-rc.getLocation().y)+" facing "+facing.name()+" to get there.");
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
        return true;
    }

//    private Direction getBuildDirection() {
//        return facing.rotateRight().rotateRight().rotateRight();
//    }

    private Direction getBuildDirection() throws GameActionException {
        MapLocation normalizedLocation = coordinateSystem.toNormalized(rc.getLocation());
        Direction direction = null;

        for(Direction d : directions) {
            MapLocation location = normalizedLocation.add(d);
            if (rc.readBroadcast(BORDER_MAP + coordinateSystem.broadcastOffsetForNormalizated(location)) == 1 && !rc.isLocationOccupied(rc.getLocation().add(d))) {
                direction = d;
                break;
            }
        }

        return direction;
    }
}


