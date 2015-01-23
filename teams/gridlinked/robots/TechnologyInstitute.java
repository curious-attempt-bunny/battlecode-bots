package gridlinked.robots;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by home on 1/18/15.
 */
public class TechnologyInstitute extends BaseRobot {
    private int computersBuilt;

    public TechnologyInstitute(RobotController _rc) {
        super(_rc);
        computersBuilt = 0;
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady() && rc.getTeamOre() >= RobotType.COMPUTER.oreCost && computersBuilt <= 12) {
            if (!isHeavyTraffic()) {
                trySpawn(directions[rand.nextInt(8)], RobotType.COMPUTER);
                if (!rc.isCoreReady()) {
                    computersBuilt++;
                }
            }
        }

        if (Clock.getRoundNum() % 25 == 0) {
            computersBuilt--;
        }
    }
}
