package petrobot.system.petmission.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetMissionAccept;

/**
 * @author xiao_FL
 * @date 2019/12/18
 */
@MsgId(msgId = MsgIdEnum.SC_PetMissionAccept_VALUE)
public class PetMissionAcceptHandler extends AbstractHandler<SC_PetMissionAccept> {
    @Override
    protected SC_PetMissionAccept parse(byte[] bytes) throws Exception {
        return SC_PetMissionAccept.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetMissionAccept sc_petMissionAccept, int i) {

    }
}
