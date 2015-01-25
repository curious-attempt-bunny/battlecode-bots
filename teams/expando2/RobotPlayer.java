package expando2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import expando2.robots.*;

/**
 * Created by home on 1/6/15.
 */
public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        if (rc.getType() == RobotType.HQ) {
            new HQ(rc).run();
        } else if (rc.getType() == RobotType.TOWER) {
            new Tower(rc).run();
        } else if (rc.getType() == RobotType.BEAVER) {
            new Beaver(rc).run();
        } else if (rc.getType() == RobotType.MINERFACTORY) {
            new MinerFactory(rc).run();
        } else if (rc.getType() == RobotType.BARRACKS) {
            new Barracks(rc).run();
        } else if (rc.getType() == RobotType.TANKFACTORY) {
            new TankFactory(rc).run();
        } else if (rc.getType() == RobotType.TECHNOLOGYINSTITUTE) {
            new TechnologyInstitute(rc).run();
        } else if (rc.getType() == RobotType.COMPUTER) {
            new Computer(rc).run();
        } else if (rc.getType() == RobotType.MINER) {
            new Miner(rc).run();
        } else if (rc.getType() == RobotType.SOLDIER) {
            new Soldier(rc).run();
        } else if (rc.getType() == RobotType.BASHER ) {
            new Basher(rc).run();
        } else if (rc.getType() == RobotType.TANK) {
            new Tank(rc).run();
        } else {
            new Immobile(rc).run();
        }
    }
}
