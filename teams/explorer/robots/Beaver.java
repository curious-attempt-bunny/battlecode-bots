package explorer.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    private MapLocation target;

    public Beaver(RobotController _rc) throws GameActionException {
        super(_rc);

        target = nextTarget();
        facing = rc.getLocation().directionTo(target);
    }

    private MapLocation nextTarget() throws GameActionException {
        MapLocation mapLocation = arena.nearestUnexploredGridCenter(rc.getLocation());
        return mapLocation;
    }


    @Override
    protected void act() throws GameActionException {
        if (target != null && arena.explored(target)) {
//            System.out.println("Explored "+arena.gridDesc(target));
            target = nextTarget();
        }

        if (target != null) {
//            rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);
            rc.setIndicatorDot(target, 255, 0, 0);
        }

//        for(MapLocation visible : MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), rc.getType().sensorRadiusSquared)) {
//
//        }

        if (rc.isCoreReady()) {
//            if (target != null) {
//                facing = rc.getLocation().directionTo(target);
//            }
//            if (!rc.canMove(facing)) {
//                facing = facing.rotateRight();
//            }
            if (target != null) {
//                System.out.println("Want to move from "+rc.getLocation()+" to "+target);
                facing = rc.getLocation().directionTo(target);
                tryMove(facing);
//                rc.move(facing);
            }
        }
    }
}
