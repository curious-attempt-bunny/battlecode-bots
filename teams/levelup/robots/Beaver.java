package levelup.robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Arrays;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    Direction[] individualDirections;
    private MapLocation stuckLoc;

    public Beaver(RobotController _rc) {
        super(_rc);

        int offset = rand.nextInt(8-1);
        individualDirections = new Direction[8];

        for(int i=0; i<8; i++) {
            individualDirections[(i+offset)%8] = directions[i];
        }
        for(int i=0; i<8; i++) {
            int j = rand.nextInt(8);
            Direction x = individualDirections[i];
            individualDirections[i] = individualDirections[j];
            individualDirections[j] = x;
        }

        stuckLoc = null;
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady()) {
            moveUpGradient();
        }
    }

    private void moveUpGradient() throws GameActionException {
        int bestHeight = hqGradient.height(rc.getLocation());
        int bestMaxHeight = hqGradient.maxHeight(rc.getLocation());
        Direction selected = null;

        int downgradientUntilBetterThan = (stuckLoc == null ? -1 : hqGradient.maxHeight(stuckLoc));

        if (bestMaxHeight > downgradientUntilBetterThan) {
            stuckLoc = null;
        }

        for(Direction d : individualDirections) {
            MapLocation nextDirection = rc.getLocation();
            for(int extension=0; extension<5; extension++) {
                nextDirection.add(d);
                System.out.println("Considering "+nextDirection+" rc.canMove(d) = "+rc.canMove(d)+" rc.isPathable(rc.getType(), nextDirection) = "+rc.isPathable(rc.getType(), nextDirection));
                if (!rc.canMove(d) || rc.isPathable(rc.getType(), nextDirection)) {
                    break;
                }
                int nextHeight = hqGradient.height(nextDirection);
                if (nextHeight == 0) {
                    break;
                }
                System.out.println("Really considering "+nextDirection);
                int nextMaxHeight = hqGradient.maxHeight(nextDirection);
                if (downgradientUntilBetterThan == -1) {
                    if (nextHeight > bestHeight ||
                            (nextHeight == bestHeight && (nextMaxHeight > bestMaxHeight))) {
                        selected = d;
                        bestMaxHeight = nextMaxHeight;
                        bestHeight = nextHeight;
                    }
                } else {
                    System.out.println("Best is "+bestHeight+" considering "+nextHeight+" for "+downgradientUntilBetterThan);
                    if (bestHeight > downgradientUntilBetterThan || nextHeight > downgradientUntilBetterThan) {
                        if (nextHeight > bestHeight ||
                                (nextHeight == bestHeight && (nextMaxHeight > bestMaxHeight))) {
                            selected = d;
                            bestMaxHeight = nextMaxHeight;
                            bestHeight = nextHeight;
                        }
                    } else if (nextHeight < bestHeight ||
                            (nextHeight == bestHeight && (nextMaxHeight < bestMaxHeight))) {
                        selected = d;
                        bestMaxHeight = nextMaxHeight;
                        bestHeight = nextHeight;
                    }
                }
            }
        }

        if (selected != null) {
            rc.move(selected);
        } else {
            stuckLoc = rc.getLocation();
        }

        if (stuckLoc == null) {
            rc.setIndicatorString(0, "Heading up from "+hqGradient.maxHeight(rc.getLocation()));
        } else {
            rc.setIndicatorString(0, "Heading down to maxHeight of "+hqGradient.maxHeight(stuckLoc));
        }
    }
}
