package harvester.robots;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 1/6/15.
 */
public class HQ extends BaseRobot {
    private int nextWaveThreshold;
    private MapLocation attackPoint;

    public HQ(RobotController _rc) {
        super(_rc);

        nextWaveThreshold = 15;
        attackPoint = rc.getLocation();
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isWeaponReady()) {
            attackSomething();
        }

        transferSupply();

        RobotInfo[] myRobots = rc.senseNearbyRobots(999999, myTeam);
        int[] counts = new int[RobotType.MISSILE.ordinal()+1];
        for (RobotInfo r : myRobots) {
            counts[r.type.ordinal()]++;
        }
        for(RobotType type : RobotType.values()) {
            rc.broadcast(type.ordinal(), counts[type.ordinal()]);
        }

        RobotInfo[] enemyRobots = rc.senseNearbyRobots(999999, enemyTeam);
        List<MapLocation> towerPoints = new ArrayList<MapLocation>();
        for (RobotInfo r : enemyRobots) {
            if (r.type == RobotType.TOWER) {
                towerPoints.add(r.location);
            }
        }

        MapLocation nextAttackPoint;

        if (towerPoints.size() > 0) {
            nextAttackPoint = towerPoints.get(0);
        } else {
            nextAttackPoint = rc.senseEnemyHQLocation();
        }

        if (countOf(RobotType.TANK) > nextWaveThreshold || !nextAttackPoint.equals(attackPoint) || tick() == 2000-200) {
            if (nextAttackPoint.equals(attackPoint)) {
                nextWaveThreshold += 5;
            } else {
                nextWaveThreshold -= 5;
            }
            attackPoint = nextAttackPoint;

            rc.broadcast(100, attackPoint.x);
            rc.broadcast(101, attackPoint.y);
            rc.broadcast(102, 1);
        } else {
            rc.broadcast(102, 0);
        }
        rc.broadcast(99, tick());

        if (rc.isCoreReady() && rc.getTeamOre() >= 100 && (countOf(RobotType.BEAVER) < desiredBeavers() || tick() % 250 == 0)) {
            trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
        }
    }

    private int desiredBeavers() throws GameActionException {
        if (countOf(RobotType.MINERFACTORY) == 0) {
            return 5;
        } else {
            return 2;
        }
    }
}
