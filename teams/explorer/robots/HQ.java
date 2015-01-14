package explorer.robots;

import battlecode.common.*;
import explorer.ServerArena;

import java.util.HashSet;

/**
 * Created by home on 1/6/15.
 */
public class HQ extends BaseRobot {
    public HQ(RobotController _rc) throws GameActionException {
        super(_rc, new ServerArena(_rc, _rc.getLocation()));
//        arena.addVisibleTerrain(rc.getLocation(), rc.getType().sensorRadiusSquared);
    }

    @Override
    protected void act() throws GameActionException {
        RobotInfo[] myRobots = rc.senseNearbyRobots(999999, myTeam);
        int[] counts = new int[RobotType.MISSILE.ordinal()+1];
        RobotInfo track = null;

        for (RobotInfo r : myRobots) {
            counts[r.type.ordinal()]++;
            if (r.type == RobotType.BEAVER && (track == null || rand.nextBoolean())) {
                track = r;
            }
//            System.out.println(r.type.name() + " " + r.coreDelay + " " + r.type.canMove() + " " + r.type.movementDelay);
//            arena.addVisibleTerrain(r.location, r.type.sensorRadiusSquared);
        }
        for(RobotType type : RobotType.values()) {
            rc.broadcast(type.ordinal(), counts[type.ordinal()]);
        }

        if (track != null) {
//            System.out.println("Adding visible for "+track.type.name()+"-"+track.ID);
            arena.addVisibleTerrain(track.location, track.type.sensorRadiusSquared);
//            arena.dumpGrid(track.location);
        }

        if (rc.isCoreReady() && rc.getTeamOre() >= 100 && countOf(RobotType.BEAVER) < 3) {
            trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
        }

//        arena.dumpGrid(rc.getLocation());
    }

}
