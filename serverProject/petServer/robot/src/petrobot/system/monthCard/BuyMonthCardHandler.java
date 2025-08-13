package petrobot.system.monthCard;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MonthCard.SC_BuyMonthCard;

@MsgId(msgId = MsgIdEnum.SC_BuyMonthCard_VALUE)
public class BuyMonthCardHandler extends AbstractHandler<SC_BuyMonthCard> {
    @Override
    protected SC_BuyMonthCard parse(byte[] bytes) throws Exception {
        return SC_BuyMonthCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_BuyMonthCard result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
