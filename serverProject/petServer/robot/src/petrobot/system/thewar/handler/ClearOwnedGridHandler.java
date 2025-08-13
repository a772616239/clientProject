package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_ClearOwnedGrid;

@MsgId(msgId = MsgIdEnum.SC_ClearOwnedGrid_VALUE)
public class ClearOwnedGridHandler extends AbstractHandler<SC_ClearOwnedGrid> {
    @Override
    protected SC_ClearOwnedGrid parse(byte[] bytes) throws Exception {
        return SC_ClearOwnedGrid.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClearOwnedGrid ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
