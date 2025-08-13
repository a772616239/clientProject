package petrobot.system.item.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Bag.SC_SellItem;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_SellItem_VALUE)
public class SellItemHandler extends AbstractHandler<SC_SellItem> {
    @Override
    protected SC_SellItem parse(byte[] bytes) throws Exception {
        return SC_SellItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_SellItem req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
