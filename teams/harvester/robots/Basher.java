package harvester.robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Created by home on 1/6/15.
 */
public class Basher extends BaseRobot {

    public Basher(RobotController _rc) throws GameActionException {
        super(_rc);

        facing = rc.getLocation().directionTo(rallyPoint());
    }

    @Override
    protected void act() throws GameActionException {
        MapLocation rallyPoint = rallyPoint();

        if (rc.isCoreReady()) {
            tryMove(facing);

            if (rand.nextInt() < 10) {
                facing = rc.getLocation().directionTo(rallyPoint);
            }
        }
    }
}