package harvester.robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by home on 1/6/15.
 */
public class Soldier extends BaseRobot {

    public Soldier(RobotController _rc) throws GameActionException {
        super(_rc);

//        facing = rc.getLocation().directionTo(rallyPoint());
    }

    @Override
    protected void act() throws GameActionException {
        MapLocation rallyPoint = rallyPoint();

        if (rc.isWeaponReady()) {
            attackSomething();
        }

        if (rc.isCoreReady()) {
            tryMove(facing);

//            if (rand.nextInt() < 10) {
//                facing = rc.getLocation().directionTo(rallyPoint);
//            }
        }
    }
}
