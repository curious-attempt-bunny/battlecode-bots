package explorer;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;
import explorer.Arena;

/**
 * Created by home on 1/7/15.
 */
public class ServerArena extends Arena {
    private final TerrainTile[][] tiles;
    private final int[][] gridExploredCount;

    public ServerArena(RobotController rc, MapLocation origin) {
        super(rc, origin);

        tiles = new TerrainTile[WIDTH][HEIGHT];
        gridExploredCount = new int[GRID_SIZE][GRID_SIZE];
    }

    public void addVisibleTerrain(MapLocation location, int sensorRadiusSquared) throws GameActionException {
        for(MapLocation visible : MapLocation.getAllMapLocationsWithinRadiusSq(location, sensorRadiusSquared)) {
            if (rc.senseTerrainTile(visible) == TerrainTile.NORMAL) {
                rc.setIndicatorDot(visible, 0, 0, 0);
            } else if (rc.senseTerrainTile(visible) == TerrainTile.UNKNOWN) {
                rc.setIndicatorDot(visible, 255, 0, 0);
            } else {
                rc.setIndicatorDot(visible, 0, 0, 255);
            }
            int x = mapX(visible);
            int y = mapY(visible);
            if (tiles[x][y] == null) {
                TerrainTile tile = rc.senseTerrainTile(visible);
//                System.out.println((mapX(location)-x)+" , "+(mapY(location)-y)+ " : " +tile.name());
                if (tile != TerrainTile.UNKNOWN) {
                    // not sure why visible terrain is unknown ..
                    tiles[x][y] = tile;
                    int gx = gridX(x);
                    int gy = gridY(y);

                    if (++gridExploredCount[gx][gy] == GRID_CELL_COUNT) {
                        rc.broadcast(this.broadCastOffset(gx, gy, Usage.EXPLORED), 1);
                        System.out.println("G"+gx+","+gy+" explored!");
                        indicateGrid(gx,gy);
//                        dumpGrid(gx,gy);
//                        dumpMap(gx,gy);
                    }
                }

                if (tile == TerrainTile.OFF_MAP) {
                    int modX = (x + GRID_CELL_SIZE/2) % GRID_CELL_SIZE;
                    int modY = (y + GRID_CELL_SIZE/2) % GRID_CELL_SIZE;
                    Integer dx = null;
                    Integer dy = null;
                    if (modX == 0) {
                        dx = -1;
                    } else if (modX == GRID_CELL_SIZE - 1) {
                        dx = 1;
                    }
                    if (modY % GRID_CELL_SIZE == 0) {
                        dy = -1;
                    } else if (modY % GRID_CELL_SIZE == GRID_CELL_SIZE - 1) {
                        dy = 1;
                    }

                    if (dx != null || dy != null) {
                        if (dx == null) dx = 0;
                        if (dy == null) dy = 0;

                        int gx = gridX(x) + dx;
                        int gy = gridY(y) + dy;

                        if (!explored(gx, gy)) {
                            gridExploredCount[gx][gy] = GRID_CELL_COUNT;
                            rc.broadcast(this.broadCastOffset(gx, gy, Usage.EXPLORED), 1);
//                            System.out.println("G"+gx+","+gy+" explored because it's offmap! ("+modX+''));
                        }
                    }
                }

            }
        }
    }

    @Override
    public boolean explored(MapLocation location) {
        return explored(gridX(mapX(location)), gridY(mapY(location)));
    }

    public boolean explored(int gx, int gy) {
        return (gridExploredCount[gx][gy] == GRID_CELL_COUNT);
    }

    public void dumpGrid(MapLocation location) {
        dumpGrid(gridX(mapX(location)), gridY(mapY(location)));
    }

    private void dumpGrid(int gx, int gy) {
        System.out.println(gx+","+gy+" contains "+gridExploredCount[gx][gy]);
        for(int y=GRID_CELL_SIZE*gy-GRID_CELL_SIZE/2; y<GRID_CELL_SIZE*(gy+1)-GRID_CELL_SIZE/2; y++) {
            for(int x=GRID_CELL_SIZE*gx-GRID_CELL_SIZE/2; x<GRID_CELL_SIZE*(gx+1)-GRID_CELL_SIZE/2; x++) {
                TerrainTile tile = tiles[x][y];
                if (tile == TerrainTile.UNKNOWN) {
                    System.out.print("?");
                } else if (tile == TerrainTile.NORMAL) {
                    System.out.print(".");
                } else if (tile == TerrainTile.OFF_MAP) {
                    System.out.print("~");
                } else if (tile == TerrainTile.VOID) {
                    System.out.print("#");
                } else if (tile == null) {
                    System.out.print("!");
                } else {
                    throw new RuntimeException(tile.name());
                }
            }
            System.out.println();
        }
    }

    public void dumpMap(int gx, int gy) {
        for(int y=0; y<GRID_SIZE; y++) {
            for(int x=0; x<GRID_SIZE; x++) {
                if (gx == x && gy == y) {
                    System.out.print("X");
                } else if (explored(x,y)) {
                    System.out.print('.');
                } else {
                    System.out.print('?');
                }
            }
            System.out.println();
        }
    }
}
