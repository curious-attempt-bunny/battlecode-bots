package levelup.robots;

import battlecode.common.*;
import levelup.GradientMap;

/**
 * Created by home on 1/6/15.
 */
public class HQ extends BaseRobot {
    private int beaverCount;

    public HQ(RobotController _rc) {
        super(_rc);
        beaverCount = 0;
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isCoreReady()) {
            if (rc.getTeamOre() >= RobotType.BEAVER.oreCost && beaverCount < 5) {
                trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
                beaverCount++;
            }
        }
    }

    @Override
    protected void compute() throws GameActionException {
        if (Clock.getRoundNum() % 200 == 0) {
            hqGradient = new GradientMap(rc);
        }
        hqGradient.computeUntil(3500);
    }
}
