package petrobot.system.petfragment.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.LogUtil;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetFragmentUse;
import protocol.RetCodeId.RetCodeEnum;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@MsgId(msgId = MsgIdEnum.SC_PetFragmentUse_VALUE)
public class PetFragmentUseHandler extends AbstractHandler<SC_PetFragmentUse> {
    @Override
    protected SC_PetFragmentUse parse(byte[] bytes) throws Exception {
        return SC_PetFragmentUse.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetFragmentUse result, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        if (result.getResult().getRetCode() != RetCodeEnum.RCE_Success) {
            LogUtil.error("petFragmentUse failed by " + result.getResult().getRetCode());
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
