package petrobot.system.mainLine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MainLine.SC_SettleOnHookReward;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_SettleOnHookReward_VALUE)
public class SettleOnHookRewardHandler extends AbstractHandler<SC_SettleOnHookReward> {
    @Override
    protected SC_SettleOnHookReward parse(byte[] bytes) throws Exception {
        return SC_SettleOnHookReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_SettleOnHookReward result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
