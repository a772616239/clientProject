package petrobot.system.drawCard.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.DrawCard.SC_ResetHighCardPool;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;

@MsgId(msgId = MsgIdEnum.SC_ResetHighCardPool_VALUE)
public class ResetHighCardHandler extends AbstractHandler<SC_ResetHighCardPool> {
    @Override
    protected SC_ResetHighCardPool parse(byte[] bytes) throws Exception {
        return SC_ResetHighCardPool.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_ResetHighCardPool result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        RetCodeEnum retCode = result.getRetCode().getRetCode();
        if (retCode != RetCodeEnum.RCE_Success) {
            LogUtil.error("reset high card result is error, ret = " + retCode);
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
