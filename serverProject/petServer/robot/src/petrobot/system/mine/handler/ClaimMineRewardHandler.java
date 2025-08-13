package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.SC_ClaimMineReward;

@MsgId(msgId = MsgIdEnum.SC_ClaimMineReward_VALUE)
public class ClaimMineRewardHandler extends AbstractHandler<SC_ClaimMineReward> {
    @Override
    protected SC_ClaimMineReward parse(byte[] bytes) throws Exception {
        return SC_ClaimMineReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimMineReward ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
