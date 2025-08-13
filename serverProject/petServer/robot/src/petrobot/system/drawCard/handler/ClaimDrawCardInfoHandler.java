package petrobot.system.drawCard.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.DrawCard.SC_ClaimDrawCardInfo;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_ClaimDrawCardInfo_VALUE)
public class ClaimDrawCardInfoHandler extends AbstractHandler<SC_ClaimDrawCardInfo> {
    @Override
    protected SC_ClaimDrawCardInfo parse(byte[] bytes) throws Exception {
        return SC_ClaimDrawCardInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimDrawCardInfo result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
