package petrobot.system.endlessSpire.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.EndlessSpire.SC_ClaimSpireAchievementReward;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/12/25
 */
@MsgId(msgId = MsgIdEnum.SC_ClaimSpireAchievementReward_VALUE)
public class ClaimSpireAchievementRewardHandler extends AbstractHandler<SC_ClaimSpireAchievementReward> {
    @Override
    protected SC_ClaimSpireAchievementReward parse(byte[] bytes) throws Exception {
        return SC_ClaimSpireAchievementReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimSpireAchievementReward req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
