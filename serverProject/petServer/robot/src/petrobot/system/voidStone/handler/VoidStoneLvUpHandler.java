package petrobot.system.voidStone.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_VoidStoneUnLock;

/**
 * @author xiao_FL
 * @date 2020/1/2
 */
@MsgId(msgId = MsgIdEnum.SC_VoidStoneUnLock_VALUE)
public class VoidStoneLvUpHandler extends AbstractHandler<SC_VoidStoneUnLock> {
    @Override
    protected SC_VoidStoneUnLock parse(byte[] bytes) throws Exception {
        return SC_VoidStoneUnLock.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_VoidStoneUnLock result, int i) {
        Robot robotByChannel = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robotByChannel != null) {
            robotByChannel.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        }
    }
}
