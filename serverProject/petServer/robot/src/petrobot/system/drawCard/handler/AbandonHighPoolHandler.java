package petrobot.system.drawCard.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.DrawCard.SC_AbandonHighPool;
import protocol.MessageId.MsgIdEnum;

/**
 * @author huhan
 * @date 2020/12/25
 */
@MsgId(msgId = MsgIdEnum.SC_AbandonHighPool_VALUE)
public class AbandonHighPoolHandler extends AbstractHandler<SC_AbandonHighPool> {
    @Override
    protected SC_AbandonHighPool parse(byte[] bytes) throws Exception {
        return SC_AbandonHighPool.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_AbandonHighPool req, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
