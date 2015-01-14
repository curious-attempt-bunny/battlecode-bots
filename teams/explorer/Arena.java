package explorer;

import battlecode.common.*;
import explorer.robots.BaseRobot;

import java.util.Random;

/**
 * Created by home on 1/7/15.
 */
public abstract class Arena {
    public abstract void dumpGrid(MapLocation location);

    enum Usage {
        EXPLORED
    };

    private static final int WIDTH_OFFSET = 120;
    private static final int HEIGHT_OFFSET = WIDTH_OFFSET;
    protected static final int WIDTH = WIDTH_OFFSET * 2 + 1;
    protected static final int HEIGHT = WIDTH;
    protected static final int GRID_CELL_SIZE = 5;
    protected static final int GRID_CELL_COUNT = GRID_CELL_SIZE*GRID_CELL_SIZE;
    private static final int GRID_OFFSET = WIDTH_OFFSET/GRID_CELL_SIZE+1;
    protected static final int GRID_SIZE = GRID_OFFSET*2-1;
    protected final RobotController rc;
    private final MapLocation origin;
    public Random rand;

    public Arena(RobotController rc, MapLocation origin) {
        this.rc = rc;
        this.origin = origin;

//        int mx = mapX(origin);
//        int my = mapY(origin);
//        int gx = gridX(mx);
//        int gy = gridY(my);
//        System.out.println("Origin is "+origin);
//        System.out.println("Map is "+mx+","+my);
//        System.out.println("Grid is "+gx+","+gy);
//        System.out.println("Center is "+centerOf(gx,gy));
//        int mx2 = (GRID_CELL_SIZE*gx+GRID_CELL_SIZE/2);
//        int my2 = (GRID_CELL_SIZE*gy+GRID_CELL_SIZE/2);
//        System.out.println("Bottom right map is "+mx2+","+my2);
//        MapLocation loc = new MapLocation(mx2+origin.x-WIDTH_OFFSET, my2+origin.y-HEIGHT_OFFSET);
//        System.out.println("Bottom right is "+loc);
//        int mx3 = mapX(loc);
//        int my3 = mapY(loc);
//        System.out.println("Map is "+mx3+","+my3);
//        int gx2 = gridX(mx3);
//        int gy2 = gridY(my3);
//        System.out.println("Grid is "+gx2+","+gy2);
//        int mx2 = (GRID_CELL_SIZE*gx-GRID_CELL_SIZE/2);
//        int my2 = (GRID_CELL_SIZE*gy-GRID_CELL_SIZE/2);
//        System.out.println("Top left map is "+mx2+","+my2);
//        MapLocation loc = new MapLocation(mx2+origin.x-WIDTH_OFFSET, my2+origin.y-HEIGHT_OFFSET);
//        System.out.println("Top left is "+loc);
//        int mx3 = mapX(loc);
//        int my3 = mapY(loc);
//        System.out.println("Map is "+mx3+","+my3);
//        int gx2 = gridX(mx3);
//        int gy2 = gridY(my3);
//        System.out.println("Grid is "+gx2+","+gy2);
    }

    protected int mapX(MapLocation location) {
        return location.x - origin.x + WIDTH_OFFSET;
    }

    protected int mapY(MapLocation location) {
        return location.y - origin.y + HEIGHT_OFFSET;
    }

    private MapLocation centerOf(int gx, int gy) {
        int mx = gx * GRID_CELL_SIZE;
        int my = gy * GRID_CELL_SIZE;

        int x = mx + origin.x - WIDTH_OFFSET;
        int y = my + origin.y - HEIGHT_OFFSET;

        return new MapLocation(x, y);
    }

    protected int gridX(int x) {
        return (x+GRID_CELL_SIZE/2) / GRID_CELL_SIZE;
    }

    protected int gridY(int y) {
        return (y+GRID_CELL_SIZE/2) / GRID_CELL_SIZE;
    }

    public void addVisibleTerrain(MapLocation location, int sensorRadiusSquared) throws GameActionException {
        throw new RuntimeException("Not implemented...");
    }

    protected int broadCastOffset(int gx, int gy, Usage usage) {
        return (GRID_SIZE*GRID_SIZE*usage.ordinal())+gx+(gy*GRID_SIZE);
    }

    public String gridDesc(MapLocation location) {
        return "G"+gridX(mapX(location))+","+gridY(mapY(location));
    }

    public abstract void dumpMap(int gx, int gy) throws GameActionException;

    public MapLocation nearestUnexploredGridCenter(MapLocation location) throws GameActionException {
        int gx = gridX(mapX(location));
        int gy = gridY(mapY(location));

        if (!explored(gx, gy)) {
            System.out.println("Heading for "+centerOf(gx, gy));
            return centerOf(gx, gy);
        } else {
            MapLocation best = null;
            System.out.println("Looking around from G"+gx+","+gy);
            for(Direction dir : BaseRobot.directions) {
//                System.out.println(dir.dx);
//                System.out.println(dir.dy);
                int dx = gx + dir.dx;
                int dy = gy + dir.dy;
//                System.out.println("    at G"+dx+","+dy+" (of "+GRID_SIZE+","+GRID_SIZE+") = "+explored(dx, dy));
                if (!explored(dx, dy)) {
                    if (best == null || rand.nextBoolean()) {
                        best = centerOf(dx,dy);
                        System.out.println("Picked G"+dx+","+dy);
                    }
                }
            }
            return best;
        }

//        System.out.println("Picked nothing.");
//        return null;
    }

    protected abstract boolean explored(int gx, int gy) throws GameActionException;

    public abstract boolean explored(MapLocation location) throws GameActionException;

    public void indicateGrid(int gx, int gy) {
        int mx1 = (GRID_CELL_SIZE*gx+GRID_CELL_SIZE/2);
        int my1 = (GRID_CELL_SIZE*gy+GRID_CELL_SIZE/2);
        MapLocation loc1 = new MapLocation(mx1+origin.x-WIDTH_OFFSET, my1+origin.y-HEIGHT_OFFSET);

        int mx2 = (GRID_CELL_SIZE*gx-GRID_CELL_SIZE/2);
        int my2 = (GRID_CELL_SIZE*gy-GRID_CELL_SIZE/2);
        System.out.println("Top left map is "+mx2+","+my2);
        MapLocation loc2 = new MapLocation(mx2+origin.x-WIDTH_OFFSET, my2+origin.y-HEIGHT_OFFSET);

        rc.setIndicatorLine(loc1, loc2, 0, 0, 255);

    }


}
