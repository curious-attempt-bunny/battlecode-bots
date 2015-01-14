package harvester.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
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
        if (rc.isCoreReady() && rc.getTeamOre() >= 50 && countOf(RobotType.MINER) < minerLimit()) {
            trySpawn(directions[rand.nextInt(8)], RobotType.MINER);
        }

        transferSupply();
    }

    private int minerLimit() throws GameActionException {
        if (countOf(RobotType.TANKFACTORY) >= 4) {
            return 40;
        } else {
            return 20;
        }
    }
}
