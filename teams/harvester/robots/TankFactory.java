package harvester.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by home on 1/6/15.
 */
public class TankFactory extends BaseRobot {
    public TankFactory(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady() && rc.getTeamOre() >= 250) {
            trySpawn(directions[rand.nextInt(8)], RobotType.TANK);
        }

        transferSupply();
    }
}
