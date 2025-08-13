package model.warpServer.crossServer.handler.mistforest;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.dbCache.teamCache;
import model.team.entity.teamEntity;
import model.team.util.TeamsUtil;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.SC_UpdateTeam;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_ReplyUpdatePetData;

@MsgId(msgId = MsgIdEnum.CS_GS_ReplyUpdatePetData_VALUE)
public class ReplyUpdateMistTeamHandler extends AbstractHandler<CS_GS_ReplyUpdatePetData> {
    @Override
    protected CS_GS_ReplyUpdatePetData parse(byte[] bytes) throws Exception {
        return CS_GS_ReplyUpdatePetData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_ReplyUpdatePetData ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getIdx());
        if (player == null) {
            return;
        }
        teamEntity teams = teamCache.getInstance().getTeamEntityByPlayerId(ret.getIdx());
        if (teams == null) {
            return;
        }
        if (ret.getRetCode().getRetCode() != RetCodeEnum.RCE_Success) {
            GlobalData.getInstance().sendMsg(ret.getIdx(), MsgIdEnum.SC_UpdateTeam_VALUE, SC_UpdateTeam.newBuilder().setRetCode(ret.getRetCode()));
            return;
        }
        TeamsUtil.updateTeamInfo(teams, ret.getUpdateTeamData());
    }
}
