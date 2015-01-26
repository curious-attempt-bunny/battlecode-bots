package expando2.robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by home on 1/25/15.
 */
public class Command {
    private final static Random rand = new Random(1234);

    private Integer listIndex;
    public MapLocation target;
    public RobotController rc;

    public Command(RobotController rc, int listIndex) {
        this.rc = rc;
        this.listIndex = listIndex;
    }

    public Command(RobotController rc, MapLocation target) {
        this.rc = rc;
        this.target = target;
    }

    public void addToList(RobotController rc) throws GameActionException {
        int listSize = commandCount(rc);
        if (listSize >= 100) throw new RuntimeException("Too many commands!");
        if (listIndex != null) throw new RuntimeException("Already added to list!");
        this.listIndex = listSize;
        rc.broadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*listIndex + 0, target.x);
        rc.broadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH * listIndex + 1, target.y);
        rc.broadcast(BaseRobot.COMMAND_COUNT, listSize + 1);
    }

    public static int commandCount(RobotController rc) throws GameActionException {
        return rc.readBroadcast(BaseRobot.COMMAND_COUNT);
    }

    public static void removeFromList(RobotController rc, MapLocation target) throws GameActionException {
        int lastIndex = commandCount(rc) - 1;
        for(int i=lastIndex; i>=0; i--) {
            if (target.x == rc.readBroadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*i + 0) &&
                    target.y == rc.readBroadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*i + 1)) {

                rc.broadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*i + 0, rc.readBroadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*lastIndex + 0));
                rc.broadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*i + 1, rc.readBroadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*lastIndex + 1));
                rc.broadcast(BaseRobot.COMMAND_COUNT, lastIndex);
                return;
            }
        }

        throw new RuntimeException("Failed to remove!");
    }

    public static Command getRandomCommand(RobotController rc) throws GameActionException {
        int i = rand.nextInt(commandCount(rc));

        Command command = new Command(rc, i);
            command.target = new MapLocation(
                    rc.readBroadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*i + 0),
                    rc.readBroadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*i + 1));

        return command;
    }
}
