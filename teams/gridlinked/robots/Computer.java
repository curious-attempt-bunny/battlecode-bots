package gridlinked.robots;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

/**
 * Created by home on 1/18/15.
 */
public class Computer extends BaseRobot {

    private boolean searchingForStation;

    public Computer(RobotController _rc) {
        super(_rc);
        searchingForStation = true;
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady()) {
            int bestDistance = Integer.MAX_VALUE;
            if (searchingForStation) {
                for(RobotInfo r : nearby) {
                    if (isSupplyRelay(r.type)) {
                        int distance = rc.getLocation().distanceSquaredTo(r.location);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                        }
                    }
                }

                if (bestDistance == GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED
                        || bestDistance == GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED-1
                        || bestDistance == Integer.MAX_VALUE) {
                    searchingForStation = false;
                }
            }

//            rc.setIndicatorString(0, "Searching for station = "+searchingForStation+" best distance = "+bestDistance+ "(vs "+GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED+")");
            if (searchingForStation) {
                int retries = 8;
                while (rc.isCoreReady() && retries > 0) {
                    if (!isAttackableByEnemyTowers(rc.getLocation().add(facing)) && rc.canMove(facing)) {
                        rc.move(facing);
                    }
                    if (rc.isCoreReady()) {
                        facing = facing.rotateRight();
                    }
                    retries--;
                }
            }
        }
    }
}
