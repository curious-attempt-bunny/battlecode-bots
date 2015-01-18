package energizer.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Miner extends BaseRobot {
    public Miner(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady()) {
            planMine();

            if (planDirection != null) {
                if (rc.canMove(planDirection)) {
                    rc.move(planDirection);
                } else {
                    rc.move(directions[rand.nextInt(8)]);
                }
            } else {
                rc.mine();
            }
        }
    }

    private void planMine() {
        Direction bestDirection = null;
        double bestOrePerTurn = mineAmount(rc.getLocation());

        for(Direction d : directions) {
            MapLocation mineLocation = rc.getLocation().add(d);
            double costToMove = rc.getType().movementDelay;
            if (d.isDiagonal()) {
                costToMove *= 1.4;
            }
            if (rc.canMove(d)) {
                double orePerTurn = mineAmount(mineLocation) / costToMove;

                if (orePerTurn > bestOrePerTurn) {
                    bestOrePerTurn = orePerTurn;
                    bestDirection = d;
                }
            }
        }

        if (bestOrePerTurn <= GameConstants.MINIMUM_MINE_AMOUNT) {
            planMoveAway();
        } else {
            planDirection = bestDirection;
        }
    }

    private double mineAmount(MapLocation location) {
        return Math.max(
                Math.min(GameConstants.MINER_MINE_MAX, rc.senseOre(location) / GameConstants.MINER_MINE_RATE),
                GameConstants.MINIMUM_MINE_AMOUNT);
    }
}
