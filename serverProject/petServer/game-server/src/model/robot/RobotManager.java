//package model.robot;
//
//import cfg.RobotCfg;
//import cfg.RobotCfgObject;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import model.arena.ArenaManager;
//import protocol.Arena.OpponentSimpleInfo;
//import util.GameUtil;
//
///**
// * @author huhan
// * @date 2020/03/17
// */
//public class RobotManager {
//    private static RobotManager instance;
//
//    public static RobotManager getInstance() {
//        if (instance == null) {
//            synchronized (RobotManager.class) {
//                if (instance == null) {
//                    instance = new RobotManager();
//                }
//            }
//        }
//        return instance;
//    }
//
//    private RobotManager() {
//    }
//
//    private final Map<String, Robot> robotMap = new ConcurrentHashMap<>();
//
//    public void init() {
//        for (RobotCfgObject cfg : RobotCfg._ix_id.values()) {
//            Robot newRobot = Robot.createNewRobot(cfg);
//            if (newRobot != null) {
//                addRobot(newRobot);
//            }
//        }
//    }
//
//    public void addRobot(Robot robot) {
//        if (robot == null) {
//            return;
//        }
//
//        robotMap.put(robot.getIdx(), robot);
//    }
//
//    /**
//     * 获得机器人的数据，
//     * @param count 需要几个数据
//     * @return
//     */
//    public List<OpponentSimpleInfo> getArenaInfo(int score, int count) {
//        if (robotMap.isEmpty()) {
//            return null;
//        }
//
//        ArrayList<Robot> robotList = new ArrayList<>(robotMap.values());
//        Random random = new Random();
//
//        Set<Robot> findSet = new HashSet<>();
//        for (int i = 0; i < ArenaManager.FIND_MAX_TIMES; i++) {
//            Robot robot = robotList.get(random.nextInt(robotList.size()));
//            if (robot == null) {
//                continue;
//            }
//
//            if (GameUtil.inScope(robot.getUpArenaRecommend(), robot.getLowArenaRecommend(), score)) {
//                findSet.add(robot);
//            }
//
//            if (findSet.size() >= count) {
//                break;
//            }
//        }
//
//        List<OpponentSimpleInfo> result = new ArrayList<>();
//        for (Robot robot : findSet) {
//            result.add(robot.buildArenaOpponentSimpleInfo());
//        }
//
//        return result;
//    }
//
//    public Robot getRobot(String idx) {
//        return robotMap.get(idx);
//    }
//
//    public boolean isRobot(String idx) {
//        return robotMap.containsKey(idx);
//    }
//
//    public int getRobotArenaScore(String idx) {
//        Robot robot = getRobot(idx);
//        if (robot != null) {
//            return robot.getArenaScore();
//        }
//        return 0;
//    }
//}