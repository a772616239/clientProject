package petrobot.system.ancientCall.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.AncientCall.SC_PetTransfer;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_PetTransfer_VALUE)
public class PetTransferHandler extends AbstractHandler<SC_PetTransfer> {
    @Override
    protected SC_PetTransfer parse(byte[] bytes) throws Exception {
        return SC_PetTransfer.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetTransfer result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel == null) {
            return;
        }
//        RetCodeEnum retCode = result.getRetCode().getRetCode();
//        if (retCode == RetCodeEnum.RCE_Success) {
//            Builder builder = CS_EnsureTransfer.newBuilder();
//            builder.setEnsure(new Random().nextInt(10) > 5);
//            gsChn.send(MsgIdEnum.CS_EnsureTransfer_VALUE, builder);
//        }
        SyncExecuteFunction.executeConsumer(robotByChannel, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
