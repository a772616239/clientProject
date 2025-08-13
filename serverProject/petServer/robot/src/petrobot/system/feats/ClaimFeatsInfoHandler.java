package petrobot.system.feats;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.SC_GetFeatsInfo;

@MsgId(msgId = MsgIdEnum.SC_GetFeatsInfo_VALUE)
public class ClaimFeatsInfoHandler extends AbstractHandler<SC_GetFeatsInfo> {
    @Override
    protected SC_GetFeatsInfo parse(byte[] bytes) throws Exception {
        return SC_GetFeatsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_GetFeatsInfo result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                if (result.getRetCode().getRetCode()== RetCodeEnum.RCE_Success){
                    robot.getData().setFeatsInfos(result);
                }
                robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
            });
        }
    }
}
