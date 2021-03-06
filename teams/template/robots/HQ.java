package template.robots;

import battlecode.common.*;
import template.robots.BaseRobot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 1/6/15.
 */
public class HQ extends BaseRobot {
    public HQ(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        RobotInfo[] myRobots = rc.senseNearbyRobots(999999, myTeam);
        int[] counts = new int[RobotType.MISSILE.ordinal()+1];
        for (RobotInfo r : myRobots) {
            counts[r.type.ordinal()]++;
        }
        for(RobotType type : RobotType.values()) {
            rc.broadcast(type.ordinal(), counts[type.ordinal()]);
        }
    }
}
