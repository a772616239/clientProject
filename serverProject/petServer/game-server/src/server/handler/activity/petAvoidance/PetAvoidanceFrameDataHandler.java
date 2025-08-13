package server.handler.activity.petAvoidance;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.petAvoidance.PetAvoidanceGameManager;
import protocol.Activity.CS_PetAvoidanceFrameData;
import protocol.Common;
import protocol.MessageId.MsgIdEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_PetAvoidanceFrameData_VALUE)
public class PetAvoidanceFrameDataHandler extends AbstractBaseHandler<CS_PetAvoidanceFrameData> {

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_PetAvoidanceFrameData parse(byte[] bytes) throws Exception {
        return CS_PetAvoidanceFrameData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetAvoidanceFrameData req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        PetAvoidanceGameManager.getInstance().updateGameData(playerIdx, req);
    }
}
