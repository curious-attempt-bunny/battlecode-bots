package energizer.robots;

import battlecode.common.Direction;
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
        if (rc.getTeamOre() >= RobotType.COMPUTER.oreCost && computersBuilt <= 12) {
            trySpawn(Direction.NORTH, RobotType.COMPUTER);
            if (!rc.isCoreReady()) {
                computersBuilt++;
            }
        }
    }
}
