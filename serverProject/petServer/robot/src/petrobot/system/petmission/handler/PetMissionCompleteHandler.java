package petrobot.system.petmission.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.SC_PetMissionComplete;

/**
 * @author xiao_FL
 * @date 2019/12/18
 */
@MsgId(msgId = MsgIdEnum.SC_PetMissionComplete_VALUE)
public class PetMissionCompleteHandler extends AbstractHandler<SC_PetMissionComplete> {
    @Override
    protected SC_PetMissionComplete parse(byte[] bytes) throws Exception {
        return SC_PetMissionComplete.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, SC_PetMissionComplete scPetMissionComplete, int i) {

    }
}
