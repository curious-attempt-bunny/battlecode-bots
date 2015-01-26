package expando2.robots;

import battlecode.common.*;
import expando2.CoordinateSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by home on 1/6/15.
 */
public class HQ extends BaseRobot {

    private final CoordinateSystem coordinateSystem;
    private final Set<MapLocation> validBuildLocations;
    private final Set<MapLocation> designatedBuildLocations;
    private final HashSet<Integer> allBuildings;
    private final ArrayList<MapLocation> buildingsToAdd;
    private final ArrayList<Integer> confirmedDead;
    Boolean[][] buildable;
    private int startingRound;
    private final HashSet<Integer> aliveBuildings;
    private final HashMap<Integer, MapLocation> buildingLocations;

    public HQ(RobotController _rc) {
        super(_rc);
        coordinateSystem = new CoordinateSystem(rc);
        validBuildLocations = new HashSet<MapLocation>();
        designatedBuildLocations = new HashSet<MapLocation>();
        allBuildings = new HashSet<Integer>();
        buildable = new Boolean[CoordinateSystem.MAP_WIDTH][CoordinateSystem.MAP_HEIGHT];
        buildingsToAdd = new ArrayList<MapLocation>(10000);
        buildingsToAdd.add(rc.getLocation());
        aliveBuildings = new HashSet<Integer>();
        buildingLocations = new HashMap<Integer, MapLocation>(10000);
        confirmedDead = new ArrayList<Integer>();
    }

    @Override
    protected void act() throws GameActionException {
        startingRound = Clock.getRoundNum();
        aliveBuildings.clear();

        rc.setIndicatorString(0, "A: Same round: "+(Clock.getRoundNum() == startingRound)+ " ("+startingRound+" -> "+Clock.getRoundNum()+" rem "+Clock.getBytecodesLeft());

        if (rc.isWeaponReady()) {
            attackSomething();
        }

        if (Clock.getRoundNum() % 2 == 0) {
            RobotInfo[] myRobots = rc.senseNearbyRobots(999999, myTeam);
            int[] counts = new int[RobotType.MISSILE.ordinal()+1];
            for (RobotInfo r : myRobots) {
                counts[r.type.ordinal()]++;

                boolean building = isBuilding(r.type);
                if (!building) {
                    if (r.type == RobotType.COMPUTER) {
                        MapLocation normalized = coordinateSystem.toNormalized(r.location);
                        if (Boolean.TRUE.equals(buildable[normalized.x][normalized.y])) {
                            building = true;
                        } else if (allBuildings.contains(r.ID)) {
                            aliveBuildings.add(r.ID);
                        }
                    }
                }
                if (building) {
                    aliveBuildings.add(r.ID);
                    if (!allBuildings.contains(r.ID)) {
                        buildingsToAdd.add(r.location);
                        allBuildings.add(r.ID);
                        buildingLocations.put(r.ID, r.location);
                    }
                }
            }

            for(RobotType type : RobotType.values()) {
                rc.broadcast(type.ordinal(), counts[type.ordinal()]);
            }

            if (aliveBuildings.size() != allBuildings.size()) {
                confirmedDead.clear();
                for(Integer id : allBuildings) {
                    if (!aliveBuildings.contains(id)) {
                        MapLocation loc = coordinateSystem.toNormalized(buildingLocations.get(id));
                        buildable[loc.x][loc.y] = true;
                        validBuildLocations.add(loc);
                        new Command(rc, loc).addToList(rc);
                        rc.broadcast(BORDER_MAP + coordinateSystem.broadcastOffsetForNormalizated(loc), 1);
                        confirmedDead.add(id);
                    }
                }
                for(Integer id : confirmedDead) {
                    allBuildings.remove(id);
                }
            }
        }

//        rc.setIndicatorString(0, "A: Same round: "+(Clock.getRoundNum() == startingRound)+ " ("+startingRound+" -> "+Clock.getRoundNum()+" rem "+Clock.getBytecodesLeft());

        if (rc.isCoreReady() &&
                rc.getTeamOre() >= RobotType.BEAVER.oreCost && countOf(RobotType.BEAVER) < maxBeavers()) {
            trySpawn(rc.getLocation().directionTo(rc.senseEnemyHQLocation()), RobotType.BEAVER);
        }

        rc.setIndicatorString(1, "B: Same round: "+(Clock.getRoundNum() == startingRound)+ " ("+startingRound+" -> "+Clock.getRoundNum()+" rem "+Clock.getBytecodesLeft()+ " buildings to add "+buildingsToAdd.size());

    }

    @Override
    protected void compute() throws GameActionException {
        super.compute();

        while (Clock.getBytecodesLeft() > 550 && !buildingsToAdd.isEmpty()) {
            addBuildLocations(buildingsToAdd.remove(buildingsToAdd.size()-1));
        }

        rc.setIndicatorString(2, "C: Same round: "+(Clock.getRoundNum() == startingRound)+ " ("+startingRound+" -> "+Clock.getRoundNum()+" rem "+Clock.getBytecodesLeft());

        for(MapLocation loc : validBuildLocations) {
            if (Clock.getBytecodesLeft() < 200) break;
            rc.setIndicatorDot(coordinateSystem.toGame(loc), 0, 255, 0);
        }
    }

    private void addBuildLocations(MapLocation location) throws GameActionException {
        MapLocation normalized = coordinateSystem.toNormalized(location);
        int x = normalized.x;
        int y = normalized.y;
        for(int i = -2; i<=2; i++) {
            addBuildLocation(x+i, y-3);
            addBuildLocation(x+i, y+3);
            addBuildLocation(x-3, y+i);
            addBuildLocation(x+3, y+i);
        }
        for(int xx = x-2; xx<=x+2; xx++) {
            for(int yy = y-2; yy<=y+2; yy++) {
                if (Boolean.TRUE.equals(buildable[xx][yy])) {
                    MapLocation loc = new MapLocation(xx, yy);
                    validBuildLocations.remove(loc);
                    Command.removeFromList(rc, loc);
                    rc.broadcast(BORDER_MAP + coordinateSystem.broadcastOffsetForNormalizated(loc), 0);
                }
                buildable[xx][yy] = false;
            }
        }
    }

    private void addBuildLocation(int x, int y) throws GameActionException {
        if (buildable[x][y] == null) {
            MapLocation loc = new MapLocation(x, y);
            if ((x + y) % 2 != 0 && // leave gaps
                    rc.senseTerrainTile(coordinateSystem.toGame(loc)) == TerrainTile.NORMAL) {
                buildable[x][y] = true;
                validBuildLocations.add(loc);
                new Command(rc, loc).addToList(rc);
                rc.broadcast(BORDER_MAP + coordinateSystem.broadcastOffsetForNormalizated(loc), 1);
            } else {
                buildable[x][y] = false;
            }
        }
    }


    private boolean isBuilding(RobotType type) {
        return type == RobotType.MINERFACTORY ||
                type == RobotType.SUPPLYDEPOT ||
                type == RobotType.BARRACKS ||
                type == RobotType.TANKFACTORY ||
                type == RobotType.TECHNOLOGYINSTITUTE;
    }

    private int maxBeavers() throws GameActionException {
        return 1 +
            Math.max(
                    countOf(RobotType.BARRACKS) + countOf(RobotType.TANKFACTORY),
                    countOf(RobotType.TANK) / 3);
}
}
