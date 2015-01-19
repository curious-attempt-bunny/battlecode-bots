package energizer.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    public Beaver(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady()) {
            boolean isRelayVisible = false;

            for(RobotInfo r : nearby) {
                if (isSupplyRelay(r.type)) {
                    isRelayVisible = true;
                    break;
                }
            }

            if (!isRelayVisible) {
                if (rc.getTeamOre() >= RobotType.MINERFACTORY.oreCost && countOf(RobotType.MINERFACTORY) == 0) {
                    tryBuild(planDirection.rotateLeft().rotateLeft(), RobotType.MINERFACTORY);
                } else if (rc.getTeamOre() >= RobotType.BARRACKS.oreCost && countOf(RobotType.BARRACKS) == 0 && countOf(RobotType.MINERFACTORY) >= 1) {
                    tryBuild(planDirection.rotateLeft().rotateLeft(), RobotType.BARRACKS);
                } else if (rc.getTeamOre() >= RobotType.TANKFACTORY.oreCost && (countOf(RobotType.TANKFACTORY) == 0 || rc.getTeamOre() > 2000)) {
                    tryBuild(planDirection.rotateLeft().rotateLeft(), RobotType.TANKFACTORY);
                } else if (rc.getTeamOre() >= RobotType.SUPPLYDEPOT.oreCost && countOf(RobotType.TANK) > 2) {
                    tryBuild(planDirection.rotateLeft().rotateLeft(), RobotType.SUPPLYDEPOT);
                } else {
                    rc.mine();
                }
            }

            if (rc.isCoreReady()) {
                if (congested) {
                    planMoveAway();
                } else {
                    planDirection = facing;
                    if (rand.nextInt(4) > 0) {
                        rc.mine();
                    }
                }
            }

            if (rc.isCoreReady()) {
                if (rc.canMove(planDirection) && planDirection != null) {
                    rc.move(planDirection);
                } else {
                    rc.move(directions[rand.nextInt(8)]);
                }
            }
        }
    }
}
