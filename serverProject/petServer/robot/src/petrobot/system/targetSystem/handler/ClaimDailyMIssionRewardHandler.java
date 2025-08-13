package petrobot.system.targetSystem.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.SC_ClaimDailyMissionReward;

@MsgId(msgId = MsgIdEnum.SC_ClaimDailyMissionReward_VALUE)
public class ClaimDailyMIssionRewardHandler extends AbstractHandler<SC_ClaimDailyMissionReward> {
    @Override
    protected SC_ClaimDailyMissionReward parse(byte[] bytes) throws Exception {
        return SC_ClaimDailyMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimDailyMissionReward result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
