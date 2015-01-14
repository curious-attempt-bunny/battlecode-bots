package explorer;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/**
 * Created by home on 1/7/15.
 */
public class ClientArena extends Arena {

    private final boolean[][] explored;

    @Override
    public void dumpGrid(MapLocation location) {

    }

    public ClientArena(RobotController rc, MapLocation origin) {
        super(rc, origin);

        explored = new boolean[GRID_SIZE][GRID_SIZE];
    }

    @Override
    public boolean explored(MapLocation location) throws GameActionException {
        return explored(gridX(mapX(location)), gridY(mapY(location)));
    }

    public boolean explored(int gx, int gy) throws GameActionException {
        if (!(gx >= 0 && gx < GRID_SIZE && gy >= 0 && gy < GRID_SIZE)) {
            return true;
        }

        if (!explored[gx][gy]) {
            int v = rc.readBroadcast(this.broadCastOffset(gx, gy, Usage.EXPLORED));
            if (v == 1) {
                explored[gx][gy] = true;
//                System.out.println("HQ tells us that G"+gx+","+gy+" is explored!");
//                dumpMap(gx,gy);
            }
        }

        return explored[gx][gy];
    }

    public void dumpMap(int gx, int gy) throws GameActionException {
        for(int y=0; y<GRID_SIZE; y++) {
            for(int x=0; x<GRID_SIZE; x++) {
                if (gx == x && gy == y) {
                    System.out.print("X");
                } else if (explored(x,y)) {
                    System.out.print('.');
                } else {
                    System.out.print("?");
                }
            }
            System.out.println();
        }
    }
}
