package petrobot.system.pet.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetBagEnlarge;

/**
 * @author xiao_FL
 * @date 2020/1/2
 */
@MsgId(msgId = MsgIdEnum.SC_PetBagEnlarge_VALUE)
public class PetBagEnlargeHandler extends AbstractHandler<SC_PetBagEnlarge> {
    @Override
    protected SC_PetBagEnlarge parse(byte[] bytes) throws Exception {
        return SC_PetBagEnlarge.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetBagEnlarge scPetBagEnlarge, int i) {

    }
}
