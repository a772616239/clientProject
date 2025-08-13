package petrobot.system.feats;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.SC_BuyAdvanceFeats;

@MsgId(msgId = MsgIdEnum.SC_BuyAdvanceFeats_VALUE)
public class BuyAdvancedFeatsHandler extends AbstractHandler<SC_BuyAdvanceFeats> {
    @Override
    protected SC_BuyAdvanceFeats parse(byte[] bytes) throws Exception {
        return SC_BuyAdvanceFeats.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_BuyAdvanceFeats result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            SyncExecuteFunction.executeConsumer(robotByChannel, robot -> {
                if (result.getRetCode().getRetCode()== RetCodeEnum.RCE_Success){
//                    Builder builder = robot.getData().getFeatsInfos().toBuilder().setFeatsType(1);
//                    robot.getData().setFeatsInfos(builder.build());
                }
            });
        }
    }
}
