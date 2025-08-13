package server.handler.activity.petAvoidance;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.petAvoidance.PetAvoidanceGameManager;
import protocol.Activity.CS_PetAvoidanceStatus;
import protocol.Activity.PetAvoidanceStatus;
import protocol.Activity.SC_PetAvoidanceStatus;
import protocol.Common;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_PetAvoidanceStatus_VALUE)
public class PetAvoidanceStatusHandler extends AbstractBaseHandler<CS_PetAvoidanceStatus> {

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_PetAvoidanceStatus parse(byte[] bytes) throws Exception {
        return CS_PetAvoidanceStatus.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetAvoidanceStatus req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_PetAvoidanceStatus.Builder clBuilder = SC_PetAvoidanceStatus.newBuilder();
        clBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        if (PetAvoidanceGameManager.getInstance().isInGame(playerIdx)) {
            clBuilder.setStatus(PetAvoidanceStatus.PAS_InGame);
        }else {
            clBuilder.setStatus(PetAvoidanceStatus.PAS_NotInGame);
        }
        gsChn.send(MsgIdEnum.SC_PetAvoidanceStatus_VALUE, clBuilder);
    }
}
