package energizer.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    private Direction planDirection;

    public Beaver(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        planMove();
        if (rc.isCoreReady() && planDirection != null) {
            tryMove(planDirection);
        }
    }

    private void planMove() {
        RobotInfo[] nearby = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, myTeam);

        int sumX = 0;
        int sumY = 0;
        double count = 0;

        for(RobotInfo r : nearby) {
            sumX += r.location.x - rc.getLocation().x;
            sumY += r.location.y - rc.getLocation().y;
            count++;
        }

        double x = -(sumX / count);
        double y = -(sumY / count);

        planDirection = vectorToDirection(x,y);

        rc.setIndicatorString(0, planDirection+" from vector "+x+","+y);
    }

    private Direction vectorToDirection(double x, double y) {
        if (x < 0) {
            if (y < 0) {
                return Direction.NORTH_WEST;
            } else if (y > 0) {
                return Direction.SOUTH_WEST;
            } else { // y == 0
                return Direction.WEST;
            }
        } else if (x > 0) {
            if (y < 0) {
                return Direction.NORTH_EAST;
            } else if (y > 0) {
                return Direction.SOUTH_EAST;
            } else { // y == 0
                return Direction.EAST;
            }
        } else { // x == 0
            if (y < 0) {
                return Direction.NORTH;
            } else if (y > 0) {
                return Direction.SOUTH;
            } else { // y == 0
                return planDirection;
            }
        }
    }
}
