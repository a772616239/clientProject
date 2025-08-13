package server.handler.teams;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.team.dbCache.teamCache;
import model.team.entity.TeamsDB;
import model.team.entity.teamEntity;
import model.team.util.TeamsUtil;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.CS_ChangeUsedTeam;
import protocol.PrepareWar.SC_ChangeUsedTeam;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ChangeUsedTeam_VALUE)
public class ChangeUsedTeamHandler extends AbstractBaseHandler<CS_ChangeUsedTeam> {

    @Override
    protected CS_ChangeUsedTeam parse(byte[] bytes) throws Exception {
        return CS_ChangeUsedTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChangeUsedTeam req, int i) {
        TeamNumEnum usedTeam = req.getUsedTeam();
        SC_ChangeUsedTeam.Builder resultBuilder = SC_ChangeUsedTeam.newBuilder();
        if (usedTeam == null || usedTeam == TeamNumEnum.TNE_Team_Null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ChangeUsedTeam_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        teamEntity teams = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teams == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ChangeUsedTeam_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(teams, entity -> {
            TeamsDB builder = teams.getDB_Builder();
            if (builder == null) {
                LogUtil.error("ChangeUsedTeamHandler, playerIdx[" + playerIdx + "] dbTeam is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ChangeUsedTeam_VALUE, resultBuilder);
                return;
            }

            TeamTypeEnum teamType = TeamsUtil.getTeamType(usedTeam);
            if (teamType != null && teamType != TeamTypeEnum.TTE_Null) {
                builder.putNowUsedTeam(teamType.getNumber(), usedTeam.getNumber());
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            } else {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            }

            gsChn.send(MsgIdEnum.SC_ChangeUsedTeam_VALUE, resultBuilder);
        });

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Teams;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ChangeUsedTeam_VALUE, SC_ChangeUsedTeam.newBuilder().setRetCode(retCode));
    }
}
