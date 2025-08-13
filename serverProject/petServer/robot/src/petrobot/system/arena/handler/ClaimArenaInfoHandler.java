package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Arena.SC_ClaimArenaInfo;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/05/28
 */
@MsgId(msgId = MsgIdEnum.SC_ClaimArenaInfo_VALUE)
public class ClaimArenaInfoHandler extends AbstractHandler<SC_ClaimArenaInfo> {
    @Override
    protected SC_ClaimArenaInfo parse(byte[] bytes) throws Exception {
        return SC_ClaimArenaInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimArenaInfo req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
            robot.getData().setArenaInfo(req.toBuilder());
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
