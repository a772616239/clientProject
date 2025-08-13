package petrobot.system.mistForest;

import petrobot.robot.Robot;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.DealResultConst;
import petrobot.robotConst.IndexConst;
import petrobot.system.team.TeamManager;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_JoinMistForest;
import protocol.MistForest.EnumMistRuleKind;
import protocol.PrepareWar.TeamNumEnum;

//@Controller
public class MistForestManager {

    @Index(value = IndexConst.MIST_JoinMistForest)
    public void joinMistForest(Robot robot) {
        if (robot == null) {
            return;
        }
        if (!TeamManager.addPetToTeamIfEmpty(robot, TeamNumEnum.TNE_MistForest_1)) {
            SyncExecuteFunction.executeConsumer(robot, rob -> rob.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
            return;
        }
        CS_JoinMistForest.Builder builder = CS_JoinMistForest.newBuilder();
        builder.setMistRule(EnumMistRuleKind.EMRK_Common);
//        builder.setMistLevel(new Random().nextInt(9) + 1);
        robot.getClient().send(MsgIdEnum.CS_JoinMistForest_VALUE, builder);
    }

    @Index(value = IndexConst.MIST_MistForestMove)
    public void mistForestMove(Robot robot) {
        if (robot == null) {
            return;
        }
        if (robot.getData().getRobotMistForest() == null) {
            LogUtil.error("mistforest not init");
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> {
            try {
                r.getData().getRobotMistForest().onTick();
                r.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        });
    }
}
