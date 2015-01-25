package expando2.robots;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by home on 1/25/15.
 */
public class Command {
    public static List<Command> list = new ArrayList<Command>();

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

    public void addToList() throws GameActionException {
//        System.out.println("Adding: "+target);
        if (list.size() >= 100) throw new RuntimeException("Too many commands!");
        if (listIndex != null) throw new RuntimeException("Already added to list!");
        this.listIndex = list.size();
        rc.broadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*listIndex + 0, target.x);
        rc.broadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*listIndex + 1, target.y);
        list.add(this);
        rc.broadcast(BaseRobot.COMMAND_COUNT, list.size());
        if (listIndex != list.size()-1) throw new RuntimeException("Bad size!");

        verify(rc);
    }

    private static int commandCount(RobotController rc) throws GameActionException {
        return rc.readBroadcast(BaseRobot.COMMAND_COUNT);
    }

    public void removeFromList() throws GameActionException {
        Command last = list.get(list.size()-1);
        if (last.listIndex != list.size()-1) throw new RuntimeException("Bad index! "+(last.listIndex)+" != "+(list.size()-1));
        list.remove((int)last.listIndex);

//        System.out.println("Removing: "+target+" @ "+listIndex+"/"+list.size());

        if (listIndex >= list.size()) return;

        list.set(listIndex, last);
        last.listIndex = listIndex;
        rc.broadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*last.listIndex + 0, last.target.x);
        rc.broadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*last.listIndex + 1, last.target.y);
        rc.broadcast(BaseRobot.COMMAND_COUNT, list.size());

//        System.out.println("Removed: "+target+" @ "+listIndex+" --> "+list.size());

        verify(rc);
    }

    public static void removeFromList(MapLocation target) throws GameActionException {
        Command found = null;
//        System.out.println("Removing: "+target);

        for(int i=0; i<list.size(); i++) {
            Command command = list.get(i);
//            System.out.println("Looking at: "+command.target);
            if (command.target.equals(target)) {
                found = command;
                break;
            }
        }

        if (found == null) throw new RuntimeException("Command ("+target+") not found to remove from list! ("+list.size()+" commands)");
        found.removeFromList();
    }

    public static void startOfTurn(RobotController rc) throws GameActionException {
        int commandCount = commandCount(rc);
        list.clear();

        for(int i=0; i<commandCount; i++) {
            Command command = new Command(rc, i);
            command.target = new MapLocation(
                    rc.readBroadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*i + 0),
                    rc.readBroadcast(BaseRobot.COMMAND_LIST + BaseRobot.COMMAND_WIDTH*i + 1));
            list.add(command);
//            if (rc.getType() == RobotType.HQ) System.out.println("Present ("+i+"): "+command.target);
        }

        rc.setIndicatorString(0, "Commands read: "+commandCount);
    }

    public static void verify(RobotController rc) throws GameActionException {
//        List<Command> verifyList = list;
//        list = new ArrayList<Command>();
//
//        startOfTurn(rc);
//
//        if (verifyList.size() != commandCount(rc)) throw new RuntimeException("Size is wrong now! ("+list.size()+" != "+commandCount(rc)+")");
    }
}
