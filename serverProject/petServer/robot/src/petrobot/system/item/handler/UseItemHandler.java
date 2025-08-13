package petrobot.system.item.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Bag.SC_UseItem;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_UseItem_VALUE)
public class UseItemHandler extends AbstractHandler<SC_UseItem> {

    @Override
    protected SC_UseItem parse(byte[] bytes) throws Exception {
        return SC_UseItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_UseItem req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, t -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
