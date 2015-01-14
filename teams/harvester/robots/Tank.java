package harvester.robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by home on 1/6/15.
 */
public class Tank extends BaseRobot {

    private int movePauseTicks = 0;

    public Tank(RobotController _rc) throws GameActionException {
        super(_rc);

        facing = rc.getLocation().directionTo(rallyPoint());
    }

    @Override
    protected void act() throws GameActionException {
        MapLocation rallyPoint = rallyPoint();

        if (rc.isWeaponReady()) {
            attackSomething();
        }

        if (rc.getWeaponDelay() > 0 && inRangeToAttack()) {
            movePauseTicks--;
            facing = rc.getLocation().directionTo(rallyPoint);
            return;
        }

        if (rc.isCoreReady()) {
            tryMove(facing);

            if (rand.nextInt() < 10) {
                facing = rc.getLocation().directionTo(rallyPoint);
            }
        }
    }
}
