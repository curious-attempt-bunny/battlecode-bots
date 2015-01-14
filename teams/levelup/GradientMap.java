package levelup;

import battlecode.common.*;
import levelup.robots.BaseRobot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 1/11/15.
 */
public class GradientMap {
    private final RobotController rc;
    private final MapLocation topLeft;
    private final int mapWidth = GameConstants.MAP_MAX_WIDTH+2;
    private final int mapHeight = GameConstants.MAP_MAX_HEIGHT+2;
    private final int CHANNEL_OFFSET = 100;
    private final int CHANNEL_OFFSET2 = mapWidth*mapHeight;
    private int[][] heights;
    private int[][] maxHeights;
    private MapLocation[][] reachedFrom;
    private List<MapLocation> toCompute;

    public GradientMap(RobotController rc) {
        this.rc = rc;
        MapLocation us = rc.senseHQLocation();
        MapLocation them = rc.senseEnemyHQLocation();
        MapLocation center = new MapLocation((us.x + them.x)/2, (us.y + them.y)/2);
        topLeft = new MapLocation(center.x - GameConstants.MAP_MAX_WIDTH/2 - 1, center.y - GameConstants.MAP_MAX_HEIGHT/2 - 1);
        MapLocation initialLocation = new MapLocation(us.x - topLeft.x, us.y - topLeft.y);
        toCompute = new ArrayList<MapLocation>();
        toCompute.add(initialLocation);
    }

    public int height(MapLocation location) throws GameActionException {
        int mx = location.x - topLeft.x;
        int my = location.y - topLeft.y;

        if (heights == null) {
            return rc.readBroadcast(channelIndex(mx,my));
        } else {
            return heights[mx][my];
        }
    }

    public int maxHeight(MapLocation location) throws GameActionException {
        int mx = location.x - topLeft.x;
        int my = location.y - topLeft.y;

        if (maxHeights == null) {
            return rc.readBroadcast(channelIndex(mx,my)+CHANNEL_OFFSET2);
        } else {
            return heights[mx][my];
        }
    }

    public void computeUntil(int bytecodeLimit) throws GameActionException {
        if (heights == null) {
            heights = new int[mapWidth][mapHeight];
            maxHeights = new int[mapWidth][mapHeight];
            reachedFrom = new MapLocation[mapWidth][mapHeight];
            MapLocation initialLocation = toCompute.get(0);
            heights[initialLocation.x][initialLocation.y] = 1;
        }
        int computes = 0;
        int incomplete = 0;
        int index = 0;
        while(Clock.getBytecodesLeft() > bytecodeLimit && index < toCompute.size()) {
            MapLocation location = toCompute.get(index);
            int nextHeight = heights[location.x][location.y] + 1;
            boolean allVisible = true;
            for(Direction dir : BaseRobot.directions) {
                MapLocation nextLocation = location.add(dir);
                if (nextLocation.x >= 0 && nextLocation.x < mapWidth && nextLocation.y >= 0 && nextLocation.y < mapHeight) {
                    MapLocation translated = new MapLocation(nextLocation.x + topLeft.x, nextLocation.y + topLeft.y);
                    TerrainTile terrainTile = rc.senseTerrainTile(translated);
                    if (terrainTile == TerrainTile.UNKNOWN) {
                        rc.setIndicatorDot(translated, 255, 0, 0);
                        allVisible = false;
                    } else if (terrainTile.isTraversable()) {
                        if (heights[nextLocation.x][nextLocation.y] == 0) {
                            heights[nextLocation.x][nextLocation.y] = nextHeight;
                            rc.broadcast(channelIndex(nextLocation.x, nextLocation.y), nextHeight);
//                            System.out.println(location+" -> "+nextLocation+" is height "+nextHeight+" ("+nextHeight+")");

                            maxHeights[nextLocation.x][nextLocation.y] = nextHeight;
                            rc.broadcast(CHANNEL_OFFSET2+channelIndex(nextLocation.x, nextLocation.y), nextHeight);

                            reachedFrom[nextLocation.x][nextLocation.y] = location;

                            MapLocation previous = location;
                            while(previous != null && maxHeights[previous.x][previous.y] < nextHeight) {
                                maxHeights[previous.x][previous.y] = nextHeight;
                                rc.broadcast(CHANNEL_OFFSET2+channelIndex(previous.x, previous.y), nextHeight);
//                                System.out.println("Making "+previous+" height "+heights[previous.x][previous.y]+" ("+nextHeight+")");
                                previous = reachedFrom[previous.x][previous.y];
                            }

                            toCompute.add(nextLocation);
                        }
                    }
                }
            }

            computes++;
            toCompute.remove(index);
            if (allVisible) {
                rc.setIndicatorDot(new MapLocation(location.x + topLeft.x, location.y + topLeft.y), 0, 0, 0);
            } else {
                toCompute.add(location);
                rc.setIndicatorDot(new MapLocation(location.x + topLeft.x, location.y + topLeft.y), 255, 255, 255);
                index++;
                incomplete++;
            }
        }
        rc.setIndicatorString(0, "Computes "+computes+" incomplete "+incomplete);
    }

    private int channelIndex(int mx, int my) {
        return CHANNEL_OFFSET + (my)*mapWidth + mx;
    }
}
