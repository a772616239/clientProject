package server.handler.teams;

import cfg.MainLineEpisodeNodeConfig;
import cfg.MainLineEpisodeNodeConfigObject;
import cfg.PlayerSkillConfig;
import cfg.PlayerSkillConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.cp.CpTeamManger;
import model.crossarena.CrossArenaManager;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.team.util.TeamsUtil;
import model.warpServer.crossServer.CrossServerManager;
import org.springframework.util.CollectionUtils;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerInfo.PetTransferInfo;
import protocol.PrepareWar.CS_UpdateTeam;
import protocol.PrepareWar.PositionPetMap;
import protocol.PrepareWar.SC_UpdateTeam;
import protocol.PrepareWar.SC_UpdateTeam.Builder;
import protocol.PrepareWar.SkillMap;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamSkillPositionEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_ApplyUpdatePetData;
import util.ArrayUtil;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_UpdateTeam_VALUE)
public class UpdateTeamHandler extends AbstractBaseHandler<CS_UpdateTeam> {
    @Override
    protected CS_UpdateTeam parse(byte[] bytes) throws Exception {
        return CS_UpdateTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UpdateTeam req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        TeamNumEnum teamNum = req.getTeamNum();
        teamEntity teams = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);

        Builder resultBuilder = SC_UpdateTeam.newBuilder();

        RetCodeEnum retCodeEnum = beforeCheck(playerIdx, req);
        if (retCodeEnum != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
            gsChn.send(MsgIdEnum.SC_UpdateTeam_VALUE, resultBuilder);
            return;
        }

        retCodeEnum = checkPetAndSkill(teams, teamNum, req.getMapsList(), req.getSkillMapList());
        if (retCodeEnum != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
            gsChn.send(MsgIdEnum.SC_UpdateTeam_VALUE, resultBuilder);
            return;
        }
        Team teamDb = teams.getDBTeam(teamNum);
        if (teamDb == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_UpdateTeam_VALUE, resultBuilder);
            return;
        }
        if (teamDb.isLock()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_PrepareWar_TeamIslock));
            gsChn.send(MsgIdEnum.SC_UpdateTeam_VALUE, resultBuilder);
            return;
        }

        if (teamNum == TeamNumEnum.TNE_MatchArenaLeiTai_1) {
            if (CrossArenaManager.getInstance().hasJionArena(playerIdx) > 0) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_CrossArena_ATTABLE));
                gsChn.send(MsgIdEnum.SC_UpdateTeam_VALUE, resultBuilder);
                return;
            }
        }

        if (teamNum == TeamNumEnum.TNE_MistForest_1 && CrossServerManager.getInstance().getMistForestPlayerServerIndex(playerIdx) > 0) {
            GS_CS_ApplyUpdatePetData.Builder builder = GS_CS_ApplyUpdatePetData.newBuilder();
            builder.setIdx(playerIdx);
            builder.setUpdateTeamData(req);
            CrossServerManager.getInstance().sendMsgToMistForest(playerIdx, MsgIdEnum.GS_CS_ApplyUpdatePetData_VALUE, builder, false);
        } else {
            TeamsUtil.updateTeamInfo(teams, req);
        }


    }

    private RetCodeEnum beforeCheck(String playerIdx, CS_UpdateTeam req) {
        if (TeamNumEnum.TNE_Coupon == req.getTeamNum()) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        if (TeamNumEnum.TNE_LtCP_1 == req.getTeamNum()) {
            if (CpTeamManger.getInstance().playerInTeam(playerIdx)) {
                return RetCodeEnum.RCE_CP_PlayerInTeam;
            }
        }
        return RetCodeEnum.RCE_Success;
    }

    /**
     * 宠物是否能上阵
     *
     * @return
     */
    public RetCodeEnum checkPetAndSkill(teamEntity team, TeamNumEnum teamNum, List<PositionPetMap> petMaps, List<SkillMap> skillMaps) {
        if (team == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        if (teamNum == TeamNumEnum.TNE_Episode_1) {
            return checkEpisodePetAndSkill(team, teamNum, petMaps, skillMaps);
        }
        RetCodeEnum retCodeEnum = checkPet(team, teamNum, petMaps);
        if (retCodeEnum != RetCodeEnum.RCE_Success) {
            return retCodeEnum;
        }
        return checkSkill(team.getLinkplayeridx(), skillMaps);
    }

    private RetCodeEnum checkEpisodePetAndSkill(teamEntity team, TeamNumEnum teamNum, List<PositionPetMap> petMaps, List<SkillMap> skillMaps) {
        mainlineEntity mainlineEntity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(team.getLinkplayeridx());
        if (mainlineEntity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        int playerCurEpisodeNode = mainlineEntity.queryPlayerCurEpisode();

        MainLineEpisodeNodeConfigObject cfg = MainLineEpisodeNodeConfig.getById(playerCurEpisodeNode);
        if (cfg == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
/*        if (petMaps.size() != cfg.getHelppetnum()) {
            return RetCodeEnum.RCE_ErrorParam;
        }*/
        if (petMaps.stream().anyMatch(e -> !ArrayUtil.intArrayContain(cfg.getHelppetpool(), Integer.parseInt(e.getPetIdx())))) {
            return RetCodeEnum.RCE_PrepareWar_PetNoExist;
        }
/*        if (skillMaps.stream().anyMatch(e -> !ArrayUtil.intArrayContain(cfg.getPlayerskill(), e.getSkillCfgId()))) {
            return RetCodeEnum.RCE_PrepareWar_SkillNotExist;
        }*/
        return RetCodeEnum.RCE_Success;

    }

    /**
     * 宠物是否能上阵
     *
     * @return
     */
    private RetCodeEnum checkPet(teamEntity team, TeamNumEnum teamNum, List<PositionPetMap> petMaps) {
        if (petMaps == null || petMaps.isEmpty()) {
            return RetCodeEnum.RCE_Success;
        }
        TeamTypeEnum teamType = TeamsUtil.getTeamType(teamNum);

        //竞技场小队可上阵的宠物不能超过分区限制
        if (teamType == TeamTypeEnum.TTE_Arena
                && petMaps.size() > TeamsUtil.getArenaCanUsePositionCount(team.getLinkplayeridx(), teamNum.getNumber())) {
            return RetCodeEnum.RCE_PrepareWar_PetCountOutOfLimit;
        }

        //当前小队锁定和空的状态下无法编辑
        return SyncExecuteFunction.executeFunction(team, t -> {
            Team dbTeam = team.getDBTeam(teamNum);
            if (dbTeam == null) {
                return RetCodeEnum.RCE_PrepareWar_TeamIslock;
            }
            if (dbTeam.isLock()) {
                return RetCodeEnum.RCE_PrepareWar_TeamStatusIslock;
            }

            String playerIdx = team.getLinkplayeridx();

            Set<String> set = new HashSet<>();
            Set<Integer> teamPetCfgIds = new HashSet<>();
            int unlockPosition = team.getDB_Builder().getUnlockPosition();
            for (PositionPetMap petMap : petMaps) {
                if (petMap.getPositionValue() > unlockPosition) {
                    return RetCodeEnum.RCE_PrepareWar_PositionIsLock;
                }

                //验证宠物是否存在背包或者是巡逻队助阵宠物t
                if (!team.petExist(teamNum, petMap.getPetIdx())) {
                    return RetCodeEnum.RCE_PrepareWar_PetNoExist;
                }

                PetMessage.Pet pet = team.getTeamPet(teamNum, petMap.getPetIdx());
                if (pet == null) {
                    return RetCodeEnum.RCE_PrepareWar_PetNoExist;
                }

                //验证是否是重复宠物
                if (teamPetCfgIds.contains(pet.getPetBookId())) {
                    return RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
                }

                if (set.contains(petMap.getPetIdx())) {
                    return RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
                }
                set.add(petMap.getPetIdx());

                //如果当前宠物处于宠物转化中,不允许编队
                if (isInPetTransfer(playerIdx, petMap.getPetIdx())) {
                    return RetCodeEnum.RCE_PrepareWar_PetIsInPetTransfer;
                }

                RetCodeEnum result = RetCodeEnum.RCE_Success;
                //已阵亡宠物不允许上阵
                if (TeamsUtil.getPetRemainHp(playerIdx, teamType, petMap.getPetIdx()) <= 0) {
                    result = RetCodeEnum.RCE_PrepareWar_PetIsDead;
                }

                //矿区小队不允许一个宠物装备多个
                if (teamType == TeamTypeEnum.TTE_Mine) {
                    result = checkMineTeam(team, teamNum, petMap);

                    //竞技场小队 进攻或者防守小队之间不允许使用相同宠物,进攻或者防守小队不允许使用
                } else if (teamType == TeamTypeEnum.TTE_Arena) {
                    result = checkArenaTeam(team, teamNum, petMap);
                }

                if (result != RetCodeEnum.RCE_Success) {
                    return result;
                }
                teamPetCfgIds.add(pet.getPetBookId());
            }

            return RetCodeEnum.RCE_Success;
        });
    }

    private RetCodeEnum checkArenaTeam(teamEntity team, TeamNumEnum teamNum, PositionPetMap petMap) {
        List<Team> teams = team.getTeamsByTeamType(TeamTypeEnum.TTE_Arena);
        if (GameUtil.collectionIsEmpty(teams)) {
            return RetCodeEnum.RCE_UnknownError;
        }

        for (Team db_team : teams) {
            //不同类型 不用判断是否重复,小队号一致跳过
            if (TeamsUtil.isArenaAttack(teamNum) != TeamsUtil.isArenaAttack(db_team.getTeamNum())
                    || teamNum == db_team.getTeamNum()) {
                continue;
            }

            Collection<String> linkPetMap = db_team.getLinkPetMap().values();
            if (linkPetMap.contains(petMap.getPetIdx())) {
                return RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
            }
        }
        return RetCodeEnum.RCE_Success;
    }

    private RetCodeEnum checkMineTeam(teamEntity team, TeamNumEnum teamNum, PositionPetMap petMap) {
        List<Team> teamsByTeamType = team.getTeamsByTeamType(TeamTypeEnum.TTE_Mine);
        if (GameUtil.collectionIsEmpty(teamsByTeamType)) {
            return RetCodeEnum.RCE_UnknownError;
        }
        for (Team db_team : teamsByTeamType) {
            if (db_team.getTeamNum() != teamNum) {
                Collection<String> linkPetMap = db_team.getLinkPetMap().values();
                if (linkPetMap.contains(petMap.getPetIdx())) {
                    return RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
                }
            }
        }
        return RetCodeEnum.RCE_Success;
    }

    private RetCodeEnum checkSkill(String playerIdx, List<SkillMap> skillMaps) {
        if (skillMaps == null || skillMaps.isEmpty()) {
            return RetCodeEnum.RCE_Success;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        int playerLv = player.getLevel();
        List<Integer> ownSkill = player.getSkillIds();

        Set<Integer> skillSet = new HashSet<>();
        for (SkillMap skillMap : skillMaps) {
            if (skillMap.getSkillPosition() == TeamSkillPositionEnum.TSPE_Position_Null) {
                return RetCodeEnum.RCE_ErrorParam;
            }

            if (!ownSkill.contains(skillMap.getSkillCfgId())) {
                return RetCodeEnum.RCE_PrepareWar_SkillNotExist;
            }

            PlayerSkillConfigObject skillById = PlayerSkillConfig.getBySkillIdAndStar(skillMap.getSkillCfgId(), GameConst.PlayerSkillDefaultStar);
            if (skillById == null) {
                return RetCodeEnum.RCE_PrepareWar_SkillNotExist;
            }

            if (playerLv < skillById.getPlayerlv()) {
                return RetCodeEnum.RCE_LvNotEnough;
            }

            if (skillSet.contains(skillMap.getSkillCfgId())) {
                return RetCodeEnum.RCE_ErrorParam;
            }

            skillSet.add(skillMap.getSkillCfgId());
        }
        return RetCodeEnum.RCE_Success;
    }

    private boolean isInPetTransfer(String playerIdx, String petIdx) {
        if (petIdx == null || playerIdx == null) {
            return false;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }

        return SyncExecuteFunction.executeFunction(player, entity -> {
            DB_PlayerData.Builder db_data = player.getDb_data();
            if (db_data == null) {
                return false;
            }

            PetTransferInfo petTransfer = db_data.getAncientAltarBuilder().getPetTransfer();
            if (petIdx.equalsIgnoreCase(petTransfer.getSrcPetIdx())) {
                return true;
            }
            return false;
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Teams;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_UpdateTeam_VALUE, SC_UpdateTeam.newBuilder().setRetCode(retCode));
    }
}
