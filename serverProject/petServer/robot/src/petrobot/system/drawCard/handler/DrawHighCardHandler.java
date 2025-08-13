package petrobot.system.drawCard.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.DrawCard.SC_DrawHighCard;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_DrawHighCard_VALUE)
public class DrawHighCardHandler extends AbstractHandler<SC_DrawHighCard> {
    @Override
    protected SC_DrawHighCard parse(byte[] bytes) throws Exception {
        return SC_DrawHighCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_DrawHighCard result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
