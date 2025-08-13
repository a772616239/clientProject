package petrobot.system.pet.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import petrobot.util.SyncExecuteFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetLvlUp;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@MsgId(msgId = MsgIdEnum.SC_PetLvlUp_VALUE)
public class PetLvlUpHandler extends AbstractHandler<SC_PetLvlUp> {
    @Override
    protected SC_PetLvlUp parse(byte[] bytes) throws Exception {
        return SC_PetLvlUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetLvlUp scPetLvlUp, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gsChn.channel);
        if (robot == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(robot, r -> r.setDealResult(DealResultConst.CUR_STEP_SUCCESS));
    }
}
