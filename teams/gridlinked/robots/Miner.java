package gridlinked.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Miner extends BaseRobot {
    public Miner(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isWeaponReady()) {
            attackSomething();
        }

        if (rc.isCoreReady()) {
            planMine();

            if (planDirection != null) {
                Direction dir;

                if (rc.canMove(planDirection)) {
                    dir = planDirection;
                } else {
                    dir = directions[rand.nextInt(8)];
                }

                if (rc.canMove(dir) && !isAttackableByEnemyTowers(rc.getLocation().add(dir))) {
                    rc.move(dir);
                }
            }

            if (rc.isCoreReady()) {
                mineAndTrack();
            }
        }
    }

    private void planMine() {
        planDirection = null;
        MapLocation[] towers = rc.senseEnemyTowerLocations();
        RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.TANK.attackRadiusSquared + 2, enemyTeam);

        Direction bestDirection = null;
        double bestOrePerTurn = mineAmount(rc.getLocation());

        for(Direction d : directions) {
            MapLocation mineLocation = rc.getLocation().add(d);
            double costToMove = rc.getType().movementDelay;
            if (d.isDiagonal()) {
                costToMove *= 1.4;
            }
            if (rc.canMove(d)) {
                boolean attackable = false;
                for(MapLocation loc : towers) {
                    if (mineLocation.distanceSquaredTo(loc) <= RobotType.TOWER.attackRadiusSquared) {
                        attackable = true;
                        break;
                    }
                }
                MapLocation retreatFrom = null;
                if (!attackable) {
                    for(RobotInfo r : enemies) {
                        if (mineLocation.distanceSquaredTo(r.location) <= r.type.attackRadiusSquared+2) {
                            attackable = true;
                            retreatFrom = r.location;
                            break;
                        }
                    }
                }
                if (!attackable) {
                    double orePerTurn = mineAmount(mineLocation) / costToMove;

                    if (orePerTurn > bestOrePerTurn || (orePerTurn == bestOrePerTurn && rand.nextBoolean())) {
                        bestOrePerTurn = orePerTurn;
                        bestDirection = d;
                    }
                }
                if (retreatFrom != null) {
                    planDirection = retreatFrom.directionTo(rc.getLocation());
                }
            }
        }

        if (planDirection == null) {
            if (bestOrePerTurn <= GameConstants.MINIMUM_MINE_AMOUNT) {
                planMoveAway();
            } else {
                planDirection = bestDirection;
            }
        }
    }

    private double mineAmount(MapLocation location) {
        return Math.max(
                Math.min(GameConstants.MINER_MINE_MAX, rc.senseOre(location) / GameConstants.MINER_MINE_RATE),
                GameConstants.MINIMUM_MINE_AMOUNT);
    }
}
