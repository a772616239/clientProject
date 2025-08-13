package petrobot.system.targetSystem;

import petrobot.robot.anotation.Controller;
import java.util.List;
import java.util.Random;
import petrobot.robot.Robot;
import petrobot.robot.anotation.Index;
import petrobot.robotConst.IndexConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.*;
import protocol.TargetSystem.CS_ClaimDailyMissionReward.Builder;

@Controller
public class TargetSystemManager {

    @Index(value = IndexConst.CLAIM_TARGET_INFO)
    public void claimTargetInfo(Robot robot) {
        if (robot == null) {
            return;
        }

        robot.getClient().send(MsgIdEnum.CS_ClaimTargetInfo_VALUE, CS_ClaimTargetInfo.newBuilder());
    }

    @Index(value = IndexConst.CLAIM_DAILY_REWARD)
    public void claimDailyReward(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            List<TargetMission> dailyMission = robot.getData().getDailyMission();
            Builder builder = CS_ClaimDailyMissionReward.newBuilder();
            Random random = new Random();
            if (dailyMission.size() <= 0) {
                builder.setCfgId(random.nextInt(10));
            }
            builder.setCfgId(dailyMission.get(random.nextInt(dailyMission.size())).getCfgId());
            robot.getClient().send(MsgIdEnum.CS_ClaimDailyMissionReward_VALUE, builder);
        });
    }

    @Index(value = IndexConst.CLAIM_ACHIEVEMENT_REWARD)
    public void claimAchievementReward(Robot robot) {
        if (robot == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robot, r -> {
            CS_ClaimAchievementReward.Builder builder = CS_ClaimAchievementReward.newBuilder();
            Random random = new Random();
            builder.setCfgId(random.nextInt(100));
            builder.setIndex(random.nextInt(50));
            robot.getClient().send(MsgIdEnum.CS_ClaimAchievementReward_VALUE, builder);
        });
    }


}
