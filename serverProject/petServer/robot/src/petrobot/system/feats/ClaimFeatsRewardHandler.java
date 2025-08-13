package petrobot.system.feats;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TargetSystem.SC_ClaimFeatsReward;

@MsgId(msgId = MsgIdEnum.SC_ClaimFeatsReward_VALUE)
public class ClaimFeatsRewardHandler extends AbstractHandler<SC_ClaimFeatsReward> {
    @Override
    protected SC_ClaimFeatsReward parse(byte[] bytes) throws Exception {
        return SC_ClaimFeatsReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimFeatsReward result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
