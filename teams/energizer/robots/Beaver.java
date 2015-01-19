package energizer.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    public Beaver(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady()) {
            if (!isRelayVisible()) {
                boolean built = false;
                Direction buildDirection = facing.rotateRight().rotateRight().rotateRight();
                if (rc.getTeamOre() >= RobotType.MINERFACTORY.oreCost && countOf(RobotType.MINERFACTORY) == 0) {
                    built = tryBuild(buildDirection, RobotType.MINERFACTORY);
                } else if (rc.getTeamOre() >= RobotType.BARRACKS.oreCost && countOf(RobotType.BARRACKS) == 0 && countOf(RobotType.MINER) >= 5) {
                    built = tryBuild(buildDirection, RobotType.BARRACKS);
                } else if (rc.getTeamOre() >= RobotType.TANKFACTORY.oreCost && (countOf(RobotType.TANKFACTORY) == 0 || rc.getTeamOre() > 2000)) {
                    built = tryBuild(buildDirection, RobotType.TANKFACTORY);
                } else if (rc.getTeamOre() >= RobotType.TECHNOLOGYINSTITUTE.oreCost && countOf(RobotType.TANK) > 0 && countOfNearbyFriendly(RobotType.TECHNOLOGYINSTITUTE, 20*20) == 0) {
                    built = tryBuild(buildDirection, RobotType.TECHNOLOGYINSTITUTE);
                } else if (rc.getTeamOre() >= RobotType.SUPPLYDEPOT.oreCost && 2*countOf(RobotType.SUPPLYDEPOT) < countOf(RobotType.COMPUTER)) {
                    built = tryBuild(buildDirection, RobotType.SUPPLYDEPOT);
                }

                if (rc.isCoreReady()) {
                    rc.mine();
                }

                if (built || (rand.nextInt(15) == 0 && rc.getSupplyLevel() < 100)) {
                    facing = rc.getLocation().directionTo(rc.senseHQLocation());
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
}
