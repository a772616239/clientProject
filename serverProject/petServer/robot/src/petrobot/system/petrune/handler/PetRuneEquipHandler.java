package petrobot.system.petrune.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import petrobot.robot.Robot;
import petrobot.robot.RobotManager;
import petrobot.robotConst.DealResultConst;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetRuneEquip;

/**
 * @author xiao_FL
 * @date 2019/12/20
 */
@MsgId(msgId = MsgIdEnum.SC_PetRuneEquip_VALUE)
public class PetRuneEquipHandler extends AbstractHandler<SC_PetRuneEquip> {
    @Override
    protected SC_PetRuneEquip parse(byte[] bytes) throws Exception {
        return SC_PetRuneEquip.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetRuneEquip sc_petRuneEquip, int i) {
        Robot robot = RobotManager.getInstance().getRobotByChannel(gameServerTcpChannel.channel);
        if (robot != null) {
            robot.setDealResult(DealResultConst.CUR_STEP_SUCCESS);
        }
    }
}
