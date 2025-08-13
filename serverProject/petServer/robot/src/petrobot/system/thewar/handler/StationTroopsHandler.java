package petrobot.system.thewar.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.SC_StationTroops;

@MsgId(msgId = MsgIdEnum.SC_StationTroops_VALUE)
public class StationTroopsHandler extends AbstractHandler<SC_StationTroops> {
    @Override
    protected SC_StationTroops parse(byte[] bytes) throws Exception {
        return SC_StationTroops.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_StationTroops ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
