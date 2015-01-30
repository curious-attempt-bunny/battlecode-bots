package analysis;

import battlecode.common.*;
import battlecode.engine.signal.Signal;
import battlecode.serial.MatchFooter;
import battlecode.serial.MatchHeader;
import battlecode.serial.RoundDelta;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.ProxyFactory;
import battlecode.server.proxy.XStreamProxy;
import battlecode.world.GameMap;
import battlecode.world.signal.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class GameRater {

    public GameRater(String filename) {
        this.filename = filename;
    }

    private class RobotStat {
        public MapLocation location;
        public Team team;
        public RobotType type;
        public int countdown;

        public RobotStat(MapLocation location, RobotType type, Team team) {
            this.location = location;
            this.team = team;
            this.type = type;
        }

        public void resetCountDown() {
            countdown = RobotType.TANK.buildTurns;
        }
    }

    private final String filename;
    private GameMap map;
    private Set<MapLocation> mapTilesSeen = new HashSet<MapLocation>();
    private HashMap<Integer, RobotStat> robots = new HashMap<Integer, RobotStat>();
    private HashMap<MapLocation, Double> minedOre = new HashMap<MapLocation, Double>();
    private HashMap<MapLocation, RobotStat> robotLocations = new HashMap<MapLocation, RobotStat>();

    private int tanksProduced = 0;
    private int unitsProduced = 0;
    private int tanksLost = 0;
    private int unitsLost = 0;
    private double totalMined = 0.0;
    private int unitsUnsuppliedCount = 0;
    private double unspent = 0;
    private double totalSupply = 0.0;
    private int supplyCounts = 0;
    private int factoryIdleTurns = 0;
    private int infrastructureSpending = 0;
    private int militarySpending = 0;
    private int unitsKilled = 0;

    private void visitRound(RoundDelta round) {
        final Signal[] signals = round.getSignals();
        RobotStat r;

        Set<Integer> createdThisRound = new HashSet<Integer>();

//        System.out.println("--- turn ---");
        for (int i = 0; i < signals.length; i++) {
            Signal signal = signals[i];
            if (signal instanceof SpawnSignal) {
                SpawnSignal s = (SpawnSignal) signal;
                final int parent = s.getParentID();
                if (parent != 0) {
//                    System.out.println("Resetting parent: "+parent+" with type "+s.getType());
                    robots.get(parent).resetCountDown();
                }

                MapLocation loc = s.getLoc();
                if (loc != null) {
                    r = new RobotStat(loc, s.getType(), s.getTeam());

                    robots.put(s.getRobotID(), r);
                    createdThisRound.add(s.getRobotID());
                    robotLocations.put(s.getLoc(), r);

                    if (s.getTeam() == Team.A) {
                        if (s.getType() == RobotType.TANK) {
                            tanksProduced++;
                        }
                        if (s.getType() == RobotType.MINER || s.getType() == RobotType.BEAVER || s.getType() == RobotType.TANK) {
                            unitsProduced++;
                            militarySpending += s.getType().oreCost;
                        } else {
                            infrastructureSpending += s.getType().oreCost;
                        }


//                        System.out.println(r.type+" (ID "+s.getRobotID()+") created");
                    }

                    seenBy(r);
                }
            } else if (signal instanceof MovementSignal) {
                MovementSignal s = (MovementSignal) signal;
                RobotStat robot = robots.get(s.getRobotID());

                robotLocations.remove(robot.location);
                robot.location = s.getNewLoc();
                robotLocations.put(robot.location, robot);
                seenBy(robot);
            } else if (signal instanceof AttackSignal) {
                AttackSignal s = (AttackSignal) signal;
                r = robots.get(s.getRobotID());
            } else if (signal instanceof DeathSignal) {
                DeathSignal s = (DeathSignal) signal;
                r = robots.remove(s.getObjectID());
                robotLocations.remove(r.location);

                if (r.team == Team.A) {
                    if (r.type == RobotType.TANK ||
                            r.type == RobotType.MINER ||
                            r.type == RobotType.BEAVER) {
                        unitsLost++;
                    }
                    if (r.type == RobotType.TANK) {
                        tanksLost++;
                    }
                } else {
                    if (r.type == RobotType.TANK ||
                            r.type == RobotType.MINER ||
                            r.type == RobotType.BEAVER) {
                        unitsKilled++;
                    }
                }

            } else if (signal instanceof LocationOreChangeSignal) {
                LocationOreChangeSignal s = (LocationOreChangeSignal)signal;

                if (!minedOre.containsKey(s.getLocation())) {
                    minedOre.put(s.getLocation(), 0.0);
                }

//                System.out.println("LocationOreChangeSignal : "+s.getLocation());
                double mined = s.getOre() - minedOre.get(s.getLocation());
                minedOre.put(s.getLocation(), s.getOre());
                RobotStat miner = robotLocations.get(s.getLocation());
//                System.out.println("Team "+miner.team+" mined "+mined);

                if (miner.team == Team.A) {
                    totalMined += mined;
                }
            } else if (signal instanceof RobotInfoSignal) {
                RobotInfoSignal s = (RobotInfoSignal)signal;

                for(int index=0; index<s.getRobotIDs().length; index++) {
                    int id = s.getRobotIDs()[index];
                    RobotStat robot = robots.get(id);
                    if (robot.team == Team.A && !createdThisRound.contains(id)) {
                        if (robot.type == RobotType.BEAVER ||
                                robot.type == RobotType.MINER ||
                                robot.type == RobotType.TANK) {
                            double supply = s.getSupplyLevels()[index];
                            if (supply == 0) {
                                unitsUnsuppliedCount++;
                            } else {
                                totalSupply += supply;
                                supplyCounts++;
                            }
                        }
                    }
                }
            } else if (signal instanceof TeamOreSignal) {
                TeamOreSignal s = (TeamOreSignal)signal;

                unspent += s.getOre()[0];

//                System.out.println("Ore: "+s.getOre()[0]);
            }
        }

        totalMined += 5;

        Set<RobotStat> ourRobots = new HashSet<RobotStat>();
        for(RobotStat robot : robots.values()) {
            if (robot.team == Team.A) {
                ourRobots.add(robot);

                if (robot.type == RobotType.TANKFACTORY) {
                    if (robot.countdown > 0) {
                        robot.countdown--;
                    }
                    if (robot.countdown == 0) {
                        factoryIdleTurns++;
//                        System.out.println(robot.type+" IDLE");
                    }
                }
            }
        }

//        System.out.println("--- turn ---");


//        System.out.println("Tanks produced: "+tanksProduced);
    }

    private void seenBy(RobotStat robot) {
        for(MapLocation location : MapLocation.getAllMapLocationsWithinRadiusSq(robot.location, robot.type.sensorRadiusSquared)) {
            if (map.onTheMap(location)) {
                mapTilesSeen.add(location);
            }
        }

    }

    private int countTanks(Set<RobotStat> ourRobots) {
        int count = 0;
        for(RobotStat robot : ourRobots) {
            if (robot.type == RobotType.TANK) {
                count += 1;
            }
        }
        return count;
    }


    public void rate() throws IOException, ClassNotFoundException {
        ObjectInputStream input = null;
        input = XStreamProxy.getXStream().createObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));

        System.out.println("Rating: " + filename);


        Object o = input.readObject();
        if (o == null || !(o instanceof MatchHeader)) {
            System.err.println("Error: Missing MatchHeader.");
            System.exit(-2);
        }

        MatchHeader s = (MatchHeader)o;
        map = (GameMap) s.getMap();

        while ((o = input.readObject()) != null) {
//            System.out.println(o.getClass().getCanonicalName());

            if (o instanceof RoundDelta) {
                visitRound((RoundDelta) o);
            } else if (o instanceof MatchFooter) {
                break;
            }

        }

        System.out.println("Resources ------------------------");
        System.out.println("Ore mined                  : "+(int)totalMined+bonus((int) (totalMined/250)));
        System.out.println();

        System.out.println("Miltary --------------------------");
        System.out.println("Military spending          : "+militarySpending);
        System.out.println("Tanks produced             : "+tanksProduced);
        System.out.println("Tanks lost                 : "+tanksLost);
        System.out.println("Units killed               : "+unitsKilled+bonus(unitsKilled*2));
        System.out.println("Map tiles seen             : "+mapTilesSeen.size());
//        System.out.println("Map tiles seen %           : "+100*mapTilesSeen.size()/(map.getHeight()*map.getWidth()));
//        System.out.println("Cumulative unspent   : "+(int)unspent);
        System.out.println();

        System.out.println("Waste ----------------------------");
        System.out.println("Units lost                 : "+unitsLost+bonus(-unitsLost));
        System.out.println("Unit turns without supply  : "+unitsUnsuppliedCount+bonus(-unitsUnsuppliedCount/50));
        System.out.println("Infrastructure spending    : "+infrastructureSpending+bonus(-infrastructureSpending/500));
        System.out.println("Average unspent            : "+(int)(unspent/2000)+bonus((int) (-(unspent/2000)/25)));
        System.out.println("Non-military units produced: "+(unitsProduced-tanksProduced));
        System.out.println("Tank factory idle turns    : "+factoryIdleTurns+bonus(-factoryIdleTurns/100));
        System.out.println("Unit supply average        : "+(int)(totalSupply/supplyCounts));
        System.out.println();

        System.out.println("Score                      :       \t "+score);
    }

    private String bonus(int value) {
        score += value;
        return " \t "+value;
    }

    private int score = 0;



    public static void rate(String file) throws IOException, ClassNotFoundException {
        GameRater analyzer = new GameRater(file);
        analyzer.rate();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        rate("match.rms");
   }
}