package petrobot.system.mine.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MineFight.SC_MineFightRecord;

@MsgId(msgId = MsgIdEnum.SC_MineFightRecord_VALUE)
public class MineRecordHandler extends AbstractHandler<SC_MineFightRecord> {
    @Override
    protected SC_MineFightRecord parse(byte[] bytes) throws Exception {
        return SC_MineFightRecord.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_MineFightRecord ret, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
