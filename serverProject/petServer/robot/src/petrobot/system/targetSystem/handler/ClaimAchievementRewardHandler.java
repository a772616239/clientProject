package petrobot.system.targetSystem.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.SC_ClaimAchievementReward;

@MsgId(msgId = MsgIdEnum.SC_ClaimAchievementReward_VALUE)
public class ClaimAchievementRewardHandler extends AbstractHandler<SC_ClaimAchievementReward> {
    @Override
    protected SC_ClaimAchievementReward parse(byte[] bytes) throws Exception {
        return SC_ClaimAchievementReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimAchievementReward result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel!= null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
