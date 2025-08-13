package petrobot.system.pet.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetDisCharge;

/**
 * @author xiao_FL
 * @date 2019/12/17
 */
@MsgId(msgId = MsgIdEnum.SC_PetDisCharge_VALUE)
public class PetDischargeHandler extends AbstractHandler<SC_PetDisCharge> {
    @Override
    protected SC_PetDisCharge parse(byte[] bytes) throws Exception {
        return SC_PetDisCharge.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetDisCharge scPetDisCharge, int i) {

    }
}
