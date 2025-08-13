package petrobot.system.drawCard.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.DrawCard.SC_DrawCommonCard;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;

@MsgId(msgId = MsgIdEnum.SC_DrawCommonCard_VALUE)
public class DrawCommonCardHandler extends AbstractHandler<SC_DrawCommonCard> {
    @Override
    protected SC_DrawCommonCard parse(byte[] bytes) throws Exception {
        return SC_DrawCommonCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_DrawCommonCard result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }

        RetCodeEnum retCode = result.getRetCode().getRetCode();
        if (retCode != RetCodeEnum.RCE_Success) {
            LogUtil.error("draw common card result is error, ret = " + retCode);
        }

        SyncExecuteFunction.executeConsumer(robotByChannel, r -> {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        });
    }
}
