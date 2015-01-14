package harvester.robots;

import battlecode.common.*;

/**
 * Created by home on 1/6/15.
 */
public class Beaver extends BaseRobot {

    public Beaver(RobotController _rc) {
        super(_rc);
    }

    @Override
    protected void act() throws GameActionException {
        if (rc.isWeaponReady()) {
            attackSomething();
        }

        transferSupply();

        if (rc.isCoreReady()) {
            double oreHere = rc.senseOre(rc.getLocation());

            if (countOf(RobotType.MINERFACTORY) < 2 && rc.getTeamOre() >= minerfactoryBuildThreshold() && (oreHere >= 15 || rc.getTeamOre() >= 1500)) {
                tryBuild(facing, RobotType.MINERFACTORY);
            } else if (countOf(RobotType.MINERFACTORY) == minerFactoryLimit() &&
                    rc.getTeamOre() >= 800 &&
                    countOf(RobotType.BARRACKS) < 4 &&
                    countOf(RobotType.BARRACKS) <= countOf(RobotType.TANKFACTORY)*2) {
                tryBuild(facing, RobotType.BARRACKS);
            } else if (rc.getTeamOre() >= 500 &&
                    countOf(RobotType.BARRACKS) >= 1 &&
                    countOf(RobotType.TANKFACTORY) < tankFactoryLimit()) {
                tryBuild(facing, RobotType.TANKFACTORY);
            }

        }

//        if (rc.isCoreReady()) {
//            if (rc.getHealth() == 30 && rc.getTeamOre() > 100 && !canSeeBuilding() && countOf(RobotType.TANKFACTORY) > 0) {
//                tryBuild(facing, RobotType.SUPPLYDEPOT);
//            }
//        }

        if (rc.isCoreReady()) {
            mineOrMove();
        }
    }

    private int minerFactoryLimit() throws GameActionException {
        if (countOf(RobotType.TANKFACTORY) >= 4) {
            return 3;
        } else {
            return 2;
        }
    }

    private int tankFactoryLimit() throws GameActionException {
        if (rc.getTeamOre() < 1000) {
            return 2;
        } else if (countOf(RobotType.MINER) <= 20) {
            return 4;
        } else {
            return 6;
        }
    }

    private int minerfactoryBuildThreshold() throws GameActionException {
        if (countOf(RobotType.MINERFACTORY) == 0) {
            return 1000;
        } else {
            return 500;
        }
    }
}
