package pheromone;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.List;

/**
 * Created by home on 1/21/15.
 */
public class PheromoneMap {
    private final int offset;
    private final RobotController rc;
    private CoordinateSystem coordinateSystem;

    public PheromoneMap(RobotController rc) {
        this.rc = rc;
        offset = 200;
        coordinateSystem = new CoordinateSystem(rc);
    }

    public double scentAt(MapLocation game) throws GameActionException {
        MapLocation normalized = coordinateSystem.toNormalized(game);
        int channel = offset + normalized.x + (CoordinateSystem.MAP_WIDTH)*normalized.y;

        return rc.readBroadcast(channel) / 10000.0;
    }

    public void addTrail(double signal, double decay, List<MapLocation> trail) throws GameActionException {
        rc.setIndicatorString(1, "Adding trail of length "+trail.size()+" with scent of "+signal);
        for(MapLocation game : trail) {
            if (signal < 0.01) return;

            MapLocation normalized = coordinateSystem.toNormalized(game);
            int channel = offset + normalized.x + (CoordinateSystem.MAP_WIDTH)*normalized.y;
            double existing = rc.readBroadcast(offset) / 10000.0;
            rc.broadcast(channel, (int) ((existing+signal)*10000));
            signal *= decay;
        }
    }
}
