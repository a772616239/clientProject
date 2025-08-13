package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.SC_ClaimMineGift;

@MsgId(msgId = MsgIdEnum.SC_ClaimMineGift_VALUE)
public class ClaimMineGiftHandler extends AbstractHandler<SC_ClaimMineGift> {
    @Override
    protected SC_ClaimMineGift parse(byte[] bytes) throws Exception {
        return SC_ClaimMineGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimMineGift ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
