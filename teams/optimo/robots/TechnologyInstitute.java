package optimo.robots;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by home on 1/18/15.
 */
public class TechnologyInstitute extends BaseRobot {
    public TechnologyInstitute(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady() && rc.getTeamOre() >= RobotType.COMPUTER.oreCost && countOf(RobotType.COMPUTER) < 10*countOf(RobotType.TECHNOLOGYINSTITUTE)) {
            if (countOfNearbyFriendly(RobotType.COMPUTER, GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) < 8) {
                trySpawn(directions[rand.nextInt(8)], RobotType.COMPUTER);
            }
        }
    }
}
