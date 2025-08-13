package petrobot.system.petrune.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetRuneLvlUp;

/**
 * @author xiao_FL
 * @date 2020/1/6
 */
@MsgId(msgId = MsgIdEnum.SC_PetRuneLvlUp_VALUE)
public class PetRuneLvlUpHandler extends AbstractHandler<SC_PetRuneLvlUp> {
    @Override
    protected SC_PetRuneLvlUp parse(byte[] bytes) throws Exception {
        return SC_PetRuneLvlUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_PetRuneLvlUp result, int i) {

    }
}
