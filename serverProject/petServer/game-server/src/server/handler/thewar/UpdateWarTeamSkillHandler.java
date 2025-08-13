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
import protocol.TheWar.CS_UpdateWarTeamSkill;
import protocol.TheWar.SC_UpdateWarTeamSkill;
import protocol.TheWarDefine.TheWarRetCode;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateWarTeamSkill_VALUE)
public class UpdateWarTeamSkillHandler extends AbstractBaseHandler<CS_UpdateWarTeamSkill> {
    @Override
    protected CS_UpdateWarTeamSkill parse(byte[] bytes) throws Exception {
        return CS_UpdateWarTeamSkill.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateWarTeamSkill req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        String roomIdx = player.getDb_data().getTheWarRoomIdx();
        if (StringHelper.isNull(roomIdx)) {
            SC_UpdateWarTeamSkill.Builder retBuilder = SC_UpdateWarTeamSkill.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_RoomNotFound);
            gsChn.send(MsgIdEnum.SC_UpdateWarTeamSkill_VALUE, retBuilder);
            return;
        }
        if (!CrossServerManager.getInstance().transferMsgToTheWar(roomIdx, playerIdx, MsgIdEnum.CS_UpdateWarTeamSkill_VALUE, req.toByteString())) {
            SC_UpdateWarTeamSkill.Builder retBuilder = SC_UpdateWarTeamSkill.newBuilder();
            retBuilder.setRetCode(TheWarRetCode.TWRC_ServerNotFound);
            gsChn.send(MsgIdEnum.SC_UpdateWarTeamSkill_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.TheWar;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_UpdateWarTeamSkill_VALUE,
                SC_UpdateWarTeamSkill.newBuilder().setRetCode(TheWarRetCode.TWRC_AbnormalMaintenance));
    }
}
