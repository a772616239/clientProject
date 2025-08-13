package server.handler.teams;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Map;
import java.util.Map.Entry;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.CS_UpdateTeamSkill;
import protocol.PrepareWar.SC_UpdateTeamSkill;
import protocol.PrepareWar.SC_UpdateTeamSkill.Builder;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamSkillPositionEnum;
import protocol.PrepareWar.TeamSkillState;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateTeamSkill_VALUE)
public class UpdateTeamSkillHandler extends AbstractBaseHandler<CS_UpdateTeamSkill> {
    @Override
    protected CS_UpdateTeamSkill parse(byte[] bytes) throws Exception {
        return CS_UpdateTeamSkill.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateTeamSkill req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        TeamNumEnum teamNum = req.getTeamNum();
        teamEntity teams = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);

        Builder resultBuilder = SC_UpdateTeamSkill.newBuilder();
        RetCodeEnum retCodeEnum = checkPetAndSkill(teams, teamNum, req.getSkillId(), req.getSkillPosition());
        if (retCodeEnum != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
            gsChn.send(MsgIdEnum.SC_UpdateTeamSkill_VALUE, resultBuilder);
            return;
        }
        Team teamDb = teams.getDBTeam(teamNum);
        if (teamDb == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_UpdateTeamSkill_VALUE, resultBuilder);
            return;
        }
        if (teamDb.isLock()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_PrepareWar_TeamIslock));
            gsChn.send(MsgIdEnum.SC_UpdateTeamSkill_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum setResult = SyncExecuteFunction.executeFunction(teams, t -> {
            if (req.getState() == TeamSkillState.TSS_Equip) {
                //技能已装备先卸载
                Map<Integer, Integer> linkSkillMap = teamDb.getLinkSkillMap();
                for (Entry<Integer, Integer> entry : linkSkillMap.entrySet()) {
                    if (entry.getValue() == req.getSkillId()) {
                        teamDb.removeLinkSkill(entry.getKey());
                    }
                }

                teamDb.putLinkSkill(req.getSkillPositionValue(), req.getSkillId());
            } else if (req.getState() == TeamSkillState.TSS_UnLoad) {
                teamDb.removeLinkSkill(req.getSkillPositionValue());
            }
            return RetCodeEnum.RCE_Success;
        });

        teams.sendUpdateTeamSkill(req.getTeamNum(), req.getSkillId());
        if (setResult != RetCodeEnum.RCE_Success) {
            return;
        }
        teams.updateTeamInfoToCrossServer(playerIdx, teamNum);
    }

    /**
     * 宠物是否能上阵
     *
     * @return
     */
    public RetCodeEnum checkPetAndSkill(teamEntity team, TeamNumEnum teamNum, int skillId, TeamSkillPositionEnum position) {
        if (team == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        return checkSkill(team.getLinkplayeridx(), skillId, position);
    }


    private RetCodeEnum checkSkill(String playerIdx, int skillId, TeamSkillPositionEnum position) {
        if (position == TeamSkillPositionEnum.TSPE_Position_Null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        int skillLv = player.getSkillLv(skillId);
        if (skillLv <= 0) {
            return RetCodeEnum.RCE_PrepareWar_SkillNotExist;
        }
        return RetCodeEnum.RCE_Success;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Teams;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_UpdateTeamSkill_VALUE, SC_UpdateTeamSkill.newBuilder().setRetCode(retCode));
    }
}
