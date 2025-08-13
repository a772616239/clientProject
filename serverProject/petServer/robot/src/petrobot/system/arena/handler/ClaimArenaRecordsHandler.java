package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Arena.SC_ClaimArenaRecords;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/05/28
 */
@MsgId(msgId = MsgIdEnum.SC_ClaimArenaRecords_VALUE)
public class ClaimArenaRecordsHandler extends AbstractHandler<SC_ClaimArenaRecords> {
    @Override
    protected SC_ClaimArenaRecords parse(byte[] bytes) throws Exception {
        return SC_ClaimArenaRecords.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimArenaRecords req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
