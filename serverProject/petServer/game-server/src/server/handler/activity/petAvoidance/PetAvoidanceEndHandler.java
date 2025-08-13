package server.handler.activity.petAvoidance;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.petAvoidance.PetAvoidanceGameManager;
import protocol.Activity.CS_PetAvoidanceEnd;
import protocol.Activity.SC_PetAvoidanceEnd;
import protocol.Activity.SC_PetAvoidanceEnd.Builder;
import protocol.Common;

import static protocol.MessageId.MsgIdEnum.CS_PetAvoidanceEnd_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetAvoidanceEnd_VALUE;
import util.LogUtil;

@MsgId(msgId = CS_PetAvoidanceEnd_VALUE)
public class PetAvoidanceEndHandler extends AbstractBaseHandler<CS_PetAvoidanceEnd> {

    @Override
    protected CS_PetAvoidanceEnd parse(byte[] bytes) throws Exception {
        return CS_PetAvoidanceEnd.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetAvoidanceEnd req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder clBuilder = SC_PetAvoidanceEnd.newBuilder();
        PetAvoidanceGameManager.getInstance().settle(playerIdx, clBuilder);
        LogUtil.info("PetAvoidanceEndHandler.execute " + clBuilder.getTimes());
        gsChn.send(SC_PetAvoidanceEnd_VALUE, clBuilder);
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

}
