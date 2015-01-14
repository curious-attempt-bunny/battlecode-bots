package harvester.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by home on 1/6/15.
 */
public class Barracks extends BaseRobot {
    boolean soldierNext = true;

    public Barracks(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (countOf(RobotType.SOLDIER) + countOf(RobotType.BASHER) < 20) {
            soldierNext = true; //countOf(RobotType.SOLDIER) <= countOf(RobotType.BASHER);
            if (rc.isCoreReady() && soldierNext && rc.getTeamOre() >= 60) {
                trySpawn(directions[rand.nextInt(8)], RobotType.SOLDIER);
                soldierNext = false;
            } else if (rc.isCoreReady() && !soldierNext && rc.getTeamOre() >= 80) {
                trySpawn(directions[rand.nextInt(8)], RobotType.BASHER);
                soldierNext = true;
            }
        }

        transferSupply();
    }
}
