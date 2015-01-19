package energizer.robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Created by home on 1/6/15.
 */
public class Tank extends BaseRobot {

    private int movePauseTicks = 0;

    public Tank(RobotController _rc) throws GameActionException {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        MapLocation rallyPoint = rc.senseEnemyHQLocation();

        if (rc.isWeaponReady()) {
            attackSomething();
        }

        if (rc.getWeaponDelay() > 0 && inRangeToAttack()) {
            movePauseTicks--;
            facing = rc.getLocation().directionTo(rallyPoint);
            return;
        }

        if (rc.getSupplyLevel() >= 100 && isRelayVisible()) {
            int retries = 8;
            while (rc.isCoreReady() && retries > 0) {
                tryMove(facing);
                if (rc.isCoreReady()) {
                    facing = facing.rotateRight();
                }
                retries--;
            }
//        } else if (rand.nextInt(50) == 0) {
//            tryMove(facing.opposite());
        }
    }
}
