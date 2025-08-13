package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_ClaimAfkReward;

@MsgId(msgId = MsgIdEnum.SC_ClaimAfkReward_VALUE)
public class ClaimAfkRewardHandler extends AbstractHandler<SC_ClaimAfkReward> {
    @Override
    protected SC_ClaimAfkReward parse(byte[] bytes) throws Exception {
        return SC_ClaimAfkReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimAfkReward ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
