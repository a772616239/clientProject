package petrobot.system.petrune.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetRuneGet;

@MsgId(msgId = MsgIdEnum.SC_PetRuneGet_VALUE)
public class PetRuneGetHandler extends AbstractHandler<SC_PetRuneGet> {
    @Override
    protected SC_PetRuneGet parse(byte[] bytes) throws Exception {
        return SC_PetRuneGet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetRuneGet result, int i) {

    }
}
