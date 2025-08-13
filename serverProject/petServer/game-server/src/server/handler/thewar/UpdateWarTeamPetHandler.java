package server.handler.thewar;

import common.AbstractBaseHandler;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.TheWar.CS_UpdateWarTeamPet;
import protocol.TheWar.SC_UpdateWarTeamPet;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateWarTeamPet_VALUE)
public class UpdateWarTeamPetHandler extends AbstractBaseHandler<CS_UpdateWarTeamPet> {
    @Override
    protected CS_UpdateWarTeamPet parse(byte[] bytes) throws Exception {
        return CS_UpdateWarTeamPet.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateWarTeamPet req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_UpdateWarTeamPet.Builder retBuilder = SC_UpdateWarTeamPet.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_UpdateWarTeamPet_VALUE, retBuilder);
            return;
        }

        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_UpdateWarTeamPet_VALUE, req.toByteString())) {
            SC_UpdateWarTeamPet.Builder retBuilder = SC_UpdateWarTeamPet.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_UpdateWarTeamPet_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_UpdateWarTeamPet_VALUE,
                SC_UpdateWarTeamPet.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
