package petrobot.system.pet.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetAbilityUpdate;

@MsgId(msgId = MsgIdEnum.SC_PetAbilityUpdate_VALUE)
public class UpdatePetAbilityHandler extends AbstractHandler<SC_PetAbilityUpdate> {
    @Override
    protected SC_PetAbilityUpdate parse(byte[] bytes) throws Exception {
        return SC_PetAbilityUpdate.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetAbilityUpdate ret, int i) {

    }
}
