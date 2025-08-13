package petrobot.system.monthCard;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MonthCard.SC_ClaimMonthCard;

@MsgId(msgId = MsgIdEnum.SC_ClaimMonthCard_VALUE)
public class ClaimMonthCardHandler extends AbstractHandler<SC_ClaimMonthCard> {
    @Override
    protected SC_ClaimMonthCard parse(byte[] bytes) throws Exception {
        return SC_ClaimMonthCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimMonthCard result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robotByChannel.getData().setMonthCardInfo(result.getMonthCardListList());
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
