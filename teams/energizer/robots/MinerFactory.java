package energizer.robots;

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
            trySpawn(directions[rand.nextInt(8)], RobotType.MINER);
        }
    }

    private int maxMiners() throws GameActionException {
        return countOf(RobotType.TANKFACTORY) >= 1 ? 20 : 10;
    }

}
