package petrobot.robot;

import petrobot.robot.index.IndexManager;
import petrobot.robot.index.MethodStruct;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.util.LogUtil;

public class RobotAI implements Runnable {
    private Robot robot;

    @Override
    public void run() {

        try {
            if (!robot.isReady()) {
                LogUtil.error("robot[" + robot.getLoginName() + "] not ready");
                return;
            }
            //初始化，执行机器人之前的类似于gm命令给资源等预备操作放在此处
            if (!robot.isInitRobot()) {
                robot.init();
                robot.setInitRobot(true);
                LogUtil.info("robot[" + robot.getLoginName() + "] init success");
                return;
            }
            if (!robot.isOnline()) {
                LogUtil.debug("robot[" + robot.getLoginName() + "] not online");
                return;
            }
            // 战斗中不处理
            if (robot.getData().getBattleId() > 0) {
                LogUtil.debug("robot[" + robot.getLoginName() + "] battling");
                return;
            }
            //初始化完毕，执行逻辑//执行成功开始下个逻辑
            if (robot.getDealResult() == 1) {
                robot.setDealResult(-1);

                Integer lastExecMethodId = IndexManager.getIns().DEAL_INDEX_MAPPING.get(robot.getIndex());

                int nextIndex = robot.getIndex();
                if (lastExecMethodId == null || lastExecMethodId != IndexConst.MIST_MistForestMove) {
                    nextIndex = IndexManager.getIns().getNextIndex(robot.getIndex());
                }

                robot.setIndex(nextIndex);
                if (robot.getIndex() == -1) {
                    robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                    robot.stop();
                    LogUtil.info("robot task finished, name=" + robot.getLoginName());
                    return;
                }

                Integer curExecMethodId = IndexManager.getIns().DEAL_INDEX_MAPPING.get(robot.getIndex());
                if (curExecMethodId == null || !IndexManager.getIns().METHOD_MAPPING.containsKey(curExecMethodId)) {
                    LogUtil.debug("robot[" + robot.getLoginName() + "] index[" + robot.getIndex() + "] not regist");
                    robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
                    return;
                }

                MethodStruct struct = IndexManager.getIns().METHOD_MAPPING.get(curExecMethodId);
                long startTime = System.currentTimeMillis();
                struct.getMethod().invoke(struct.getManger(), robot);
                long endTime = System.currentTimeMillis();
                if (curExecMethodId != IndexConst.MIST_MistForestMove || endTime - robot.getLastPrintMistTime() > 5000) {
                    LogUtil.info("robot[" + robot.getLoginName() + "] execute index = " + robot.getIndex()
                            + ", method id:" + curExecMethodId + ", cost time = " + (endTime - startTime));
                    robot.setLastPrintMistTime(endTime);
                }
            } else if (robot.getDealResult() == 0) {
                LogUtil.debug("robot[" + robot.getLoginName() + "] is running index=" + robot.getIndex());
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            LogUtil.error("robot[" + robot.getLoginName() + "] exception occurs,index =" + robot.getIndex());
        }


    }

    public RobotAI(Robot robot) {
        this.robot = robot;
    }

    public void sendLoginMsg() {
//        CS_Login.Builder builder = CS_Login.newBuilder();
//        builder.setUserId()
    }

}
