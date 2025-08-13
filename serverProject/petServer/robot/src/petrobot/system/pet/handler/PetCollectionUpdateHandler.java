package petrobot.system.pet.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetCollectionUpdate;

@MsgId(msgId = MsgIdEnum.SC_PetCollectionUpdate_VALUE)
public class PetCollectionUpdateHandler extends AbstractHandler<SC_PetCollectionUpdate> {
    @Override
    protected SC_PetCollectionUpdate parse(byte[] bytes) throws Exception {
        return SC_PetCollectionUpdate.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetCollectionUpdate result, int i) {

    }
}
