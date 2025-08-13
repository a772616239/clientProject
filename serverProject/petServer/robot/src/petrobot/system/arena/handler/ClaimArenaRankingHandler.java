package petrobot.system.arena.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.Arena.SC_ClaimArenaRanking;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/05/28
 */
@MsgId(msgId = MsgIdEnum.SC_ClaimArenaRanking_VALUE)
public class ClaimArenaRankingHandler extends AbstractHandler<SC_ClaimArenaRanking> {
    @Override
    protected SC_ClaimArenaRanking parse(byte[] bytes) throws Exception {
        return SC_ClaimArenaRanking.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ClaimArenaRanking req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
