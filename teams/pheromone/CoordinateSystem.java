package pheromone;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.List;

/**
 * Created by home on 1/21/15.
 */
public class CoordinateSystem {
    public static final int MAP_WIDTH = GameConstants.MAP_MAX_WIDTH+2;
    public static final int MAP_HEIGHT = GameConstants.MAP_MAX_HEIGHT+2;

    private final MapLocation topLeft;

    public CoordinateSystem(RobotController rc) {
        MapLocation us = rc.senseHQLocation();
        MapLocation them = rc.senseEnemyHQLocation();
        MapLocation center = new MapLocation((us.x + them.x)/2, (us.y + them.y)/2);
        topLeft = new MapLocation(center.x - GameConstants.MAP_MAX_WIDTH/2 - 1, center.y - GameConstants.MAP_MAX_HEIGHT/2 - 1);
    }

    public MapLocation toNormalized(MapLocation loc) {
        return loc.add(-topLeft.x, -topLeft.y);
    }

    public MapLocation toGame(MapLocation loc) {
        return loc.add(topLeft.x, topLeft.y);
    }

}
