package petrobot.system.petrune.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetRuneUnEquip;

/**
 * @author xiao_FL
 * @date 2019/12/20
 */
@MsgId(msgId = MsgIdEnum.SC_PetRuneUnEquip_VALUE)
public class PetRuneUnEquipHandler extends AbstractHandler<SC_PetRuneUnEquip> {
    @Override
    protected SC_PetRuneUnEquip parse(byte[] bytes) throws Exception {
        return SC_PetRuneUnEquip.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetRuneUnEquip sc_petRuneUnEquip, int i) {

    }
}
