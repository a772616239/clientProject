package petrobot.system.bossTower.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.BossTower.SC_SweepBossTower;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/12/25
 */
@MsgId(msgId = MsgIdEnum.SC_SweepBossTower_VALUE)
public class SweepBossTowerHandler extends AbstractHandler<SC_SweepBossTower> {
    @Override
    protected SC_SweepBossTower parse(byte[] bytes) throws Exception {
        return SC_SweepBossTower.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_SweepBossTower req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
