package energizer.robots;

import battlecode.common.Direction;
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
        if (rc.getTeamOre() >= RobotType.TANK.oreCost && (countOf(RobotType.TANK) < 20 || rc.getTeamOre() >= 2*RobotType.TANK.oreCost)) {
            trySpawn(Direction.NORTH, RobotType.TANK);
        }
    }
}
