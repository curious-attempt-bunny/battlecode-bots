package fighter.robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by home on 1/6/15.
 */
public class MinerFactory extends BaseRobot {
    public MinerFactory(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady() && rc.getTeamOre() >= RobotType.MINER.oreCost && countOf(RobotType.MINER) < maxMiners()) {
            double bestOre = Double.MIN_VALUE;
            Direction bestDirection = null;

            for(Direction d : directions) {
                double ore = rc.senseOre(rc.getLocation().add(d));
                if (rc.canSpawn(d, RobotType.MINER)) {
                    if (ore > bestOre || (ore == bestOre && rand.nextBoolean())) {
                        bestOre = ore;
                        bestDirection = d;
                    }
                }
            }
            if (bestDirection != null) {
                trySpawn(bestDirection, RobotType.MINER);
            }
        }
    }

    private int maxMiners() throws GameActionException {
        return countOf(RobotType.TANKFACTORY) >= 1 ? 20 : 10;
    }

}
