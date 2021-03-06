package harvester.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

/**
 * Created by home on 1/6/15.
 */
public class Tower extends BaseRobot {
    public Tower(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isWeaponReady()) {
            attackSomething();
        }

        transferSupply();
    }
}
