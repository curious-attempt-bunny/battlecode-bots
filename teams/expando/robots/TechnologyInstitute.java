package expando.robots;

import battlecode.common.*;

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
        if (rc.isCoreReady() && rc.getTeamOre() >= RobotType.COMPUTER.oreCost) { // && computersBuilt <= 12) {
            if (countOfNearbyFriendly(RobotType.COMPUTER, GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) < 8) {
//            if (!isHeavyTraffic()) {
                trySpawn(directions[rand.nextInt(8)], RobotType.COMPUTER);
                if (!rc.isCoreReady()) {
                    computersBuilt++;
                }
//            }
            }
        }

        if (Clock.getRoundNum() % 5 == 0) {
            computersBuilt--;
        }
    }
}
