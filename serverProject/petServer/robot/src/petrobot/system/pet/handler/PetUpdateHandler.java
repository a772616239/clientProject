package petrobot.system.pet.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetUpdate;

@MsgId(msgId = MsgIdEnum.SC_PetUpdate_VALUE)
public class PetUpdateHandler extends AbstractHandler<SC_PetUpdate> {
    @Override
    protected SC_PetUpdate parse(byte[] bytes) throws Exception {
        return SC_PetUpdate.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetUpdate result, int i) {

    }
}
