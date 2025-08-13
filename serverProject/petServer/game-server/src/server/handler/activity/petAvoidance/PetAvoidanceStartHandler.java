package server.handler.activity.petAvoidance;


import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.petAvoidance.PetAvoidanceGameManager;
import protocol.Activity.CS_PetAvoidanceStart;
import protocol.Activity.SC_PetAvoidanceStart;
import protocol.Common;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_PetAvoidanceStart_VALUE)
public class PetAvoidanceStartHandler extends AbstractBaseHandler<CS_PetAvoidanceStart> {

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_PetAvoidanceStart parse(byte[] bytes) throws Exception {
        return CS_PetAvoidanceStart.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetAvoidanceStart req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_PetAvoidanceStart.Builder rt = PetAvoidanceGameManager.getInstance().startPetAvoidGame(playerIdx);
        gsChn.send(MsgIdEnum.SC_PetAvoidanceStart_VALUE, rt);
    }

}
