package server.handler.teams;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.util.PlayerUtil;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.CS_ChangeTeamName;
import protocol.PrepareWar.SC_ChangeTeamName;
import protocol.PrepareWar.SC_ChangeTeamName.Builder;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ChangeTeamName_VALUE)
public class ChangeTeamNameHandler extends AbstractBaseHandler<CS_ChangeTeamName> {
    @Override
    protected CS_ChangeTeamName parse(byte[] bytes) throws Exception {
        return CS_ChangeTeamName.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ChangeTeamName req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_ChangeTeamName.newBuilder();
        if (canNotChangeTeamName(req.getTeamNum())) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ChangeTeamName_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum ret = PlayerUtil.checkName(req.getChangeName());
        if (ret != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(ret));
            gsChn.send(MsgIdEnum.SC_ChangeTeamName_VALUE, resultBuilder);
            return;
        }

        teamEntity team = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (team == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] teamEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ChangeTeamName_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(team, entity -> {
            Team dbTeam = team.getDBTeam(req.getTeamNum());
            if (dbTeam == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ChangeTeamName_VALUE, resultBuilder);
                return;
            }
            dbTeam.setTeamName(req.getChangeName());
            team.sendRefreshTeamsMsg(req.getTeamNum());
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ChangeTeamName_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Teams;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ChangeTeamName_VALUE, SC_ChangeTeamName.newBuilder().setRetCode(retCode));
    }

    public boolean canNotChangeTeamName(TeamNumEnum teamNum) {
        if (teamNum == TeamNumEnum.TNE_Team_1) {
            return true;
        }
        return false;
    }
}
