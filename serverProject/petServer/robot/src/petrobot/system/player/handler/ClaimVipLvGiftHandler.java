package petrobot.system.player.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_ClaimVipLvGift;

@MsgId(msgId = MsgIdEnum.SC_ClaimVipLvGift_VALUE)
public class ClaimVipLvGiftHandler extends AbstractHandler<SC_ClaimVipLvGift> {
    @Override
    protected SC_ClaimVipLvGift parse(byte[] bytes) throws Exception {
        return SC_ClaimVipLvGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimVipLvGift result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, t -> robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
        }
    }
}
