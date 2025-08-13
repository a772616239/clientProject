package model.team.util;

import cfg.PlayerSkillConfig;
import cfg.PlayerSkillConfigObject;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import protocol.PetMessage;
import protocol.PlayerDB;
import protocol.PlayerInfo;
import protocol.PrepareWar;
import protocol.RetCodeId;
import util.GameUtil;

public class UpdateTeamUtil {


    /**
     * 宠物是否能上阵
     *
     * @return
     */
    public static RetCodeId.RetCodeEnum checkPetAndSkill(teamEntity team, PrepareWar.TeamNumEnum teamNum, List<PrepareWar.PositionPetMap> petMaps, List<PrepareWar.SkillMap> skillMaps) {
        if (team == null) {
            return RetCodeId.RetCodeEnum.RCE_UnknownError;
        }
        if (teamNum == null || teamNum == PrepareWar.TeamNumEnum.TNE_Team_Null) {
            return RetCodeId.RetCodeEnum.RCE_ErrorParam;
        }
        RetCodeId.RetCodeEnum retCodeEnum = checkPet(team, teamNum, petMaps);
        if (retCodeEnum != RetCodeId.RetCodeEnum.RCE_Success) {
            return retCodeEnum;
        }
        return checkSkill(team.getLinkplayeridx(), skillMaps);
    }

    /**
     * 宠物是否能上阵
     *
     * @return
     */
    public static RetCodeId.RetCodeEnum checkPet(teamEntity team, PrepareWar.TeamNumEnum teamNum, List<PrepareWar.PositionPetMap> petMaps) {
        if (petMaps == null || petMaps.isEmpty()) {
            return RetCodeId.RetCodeEnum.RCE_Success;
        }
        PrepareWar.TeamTypeEnum teamType = TeamsUtil.getTeamType(teamNum);

        //竞技场小队可上阵的宠物不能超过分区限制
        if (teamType == PrepareWar.TeamTypeEnum.TTE_Arena
                && petMaps.size() > TeamsUtil.getArenaCanUsePositionCount(team.getLinkplayeridx(), teamNum.getNumber())) {
            return RetCodeId.RetCodeEnum.RCE_PrepareWar_PetCountOutOfLimit;
        }

        //当前小队锁定和空的状态下无法编辑
        return SyncExecuteFunction.executeFunction(team, t -> {
            Team dbTeam = team.getDBTeam(teamNum);
            if (dbTeam == null) {
                return RetCodeId.RetCodeEnum.RCE_PrepareWar_TeamIslock;
            }
            if (dbTeam.isLock()) {
                return RetCodeId.RetCodeEnum.RCE_PrepareWar_TeamStatusIslock;
            }

            String playerIdx = team.getLinkplayeridx();

            Set<String> set = new HashSet<>();
            Set<Integer> teamPetCfgIds = new HashSet<>();
            int unlockPosition = team.getDB_Builder().getUnlockPosition();
            for (PrepareWar.PositionPetMap petMap : petMaps) {
                if (petMap.getPositionValue() > unlockPosition) {
                    return RetCodeId.RetCodeEnum.RCE_PrepareWar_PositionIsLock;
                }

                //验证宠物是否存在背包或者是巡逻队助阵宠物t
                if (!team.petExist(teamNum, petMap.getPetIdx())) {
                    return RetCodeId.RetCodeEnum.RCE_PrepareWar_PetNoExist;
                }

                PetMessage.Pet pet = team.getTeamPet(teamNum, petMap.getPetIdx());
                if (pet == null) {
                    return RetCodeId.RetCodeEnum.RCE_PrepareWar_PetNoExist;
                }

                //验证是否是重复宠物
                if (teamPetCfgIds.contains(pet.getPetBookId())) {
                    return RetCodeId.RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
                }

                if (set.contains(petMap.getPetIdx())) {
                    return RetCodeId.RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
                }
                set.add(petMap.getPetIdx());

                //如果当前宠物处于宠物转化中,不允许编队
                if (isInPetTransfer(playerIdx, petMap.getPetIdx())) {
                    return RetCodeId.RetCodeEnum.RCE_PrepareWar_PetIsInPetTransfer;
                }

                RetCodeId.RetCodeEnum result = RetCodeId.RetCodeEnum.RCE_Success;
                //已阵亡宠物不允许上阵
                if (TeamsUtil.getPetRemainHp(playerIdx, teamType, petMap.getPetIdx()) <= 0) {
                    result = RetCodeId.RetCodeEnum.RCE_PrepareWar_PetIsDead;
                }

                //矿区小队不允许一个宠物装备多个
                if (teamType == PrepareWar.TeamTypeEnum.TTE_Mine) {
                    result = checkMineTeam(team, teamNum, petMap);

                    //竞技场小队 进攻或者防守小队之间不允许使用相同宠物,进攻或者防守小队不允许使用
                } else if (teamType == PrepareWar.TeamTypeEnum.TTE_Arena) {
                    result = checkArenaTeam(team, teamNum, petMap);
                }

                if (result != RetCodeId.RetCodeEnum.RCE_Success) {
                    return result;
                }
                teamPetCfgIds.add(pet.getPetBookId());
            }

            return RetCodeId.RetCodeEnum.RCE_Success;
        });
    }

    private static RetCodeId.RetCodeEnum checkArenaTeam(teamEntity team, PrepareWar.TeamNumEnum teamNum, PrepareWar.PositionPetMap petMap) {
        List<Team> teams = team.getTeamsByTeamType(PrepareWar.TeamTypeEnum.TTE_Arena);
        if (GameUtil.collectionIsEmpty(teams)) {
            return RetCodeId.RetCodeEnum.RCE_UnknownError;
        }

        for (Team db_team : teams) {
            //不同类型 不用判断是否重复,小队号一致跳过
            if (TeamsUtil.isArenaAttack(teamNum) != TeamsUtil.isArenaAttack(db_team.getTeamNum())
                    || teamNum == db_team.getTeamNum()) {
                continue;
            }

            Collection<String> linkPetMap = db_team.getLinkPetMap().values();
            if (linkPetMap.contains(petMap.getPetIdx())) {
                return RetCodeId.RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
            }
        }
        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    private static RetCodeId.RetCodeEnum checkMineTeam(teamEntity team, PrepareWar.TeamNumEnum teamNum, PrepareWar.PositionPetMap petMap) {
        List<Team> teamsByTeamType = team.getTeamsByTeamType(PrepareWar.TeamTypeEnum.TTE_Mine);
        if (GameUtil.collectionIsEmpty(teamsByTeamType)) {
            return RetCodeId.RetCodeEnum.RCE_UnknownError;
        }
        for (Team db_team : teamsByTeamType) {
            if (db_team.getTeamNum() != teamNum) {
                Collection<String> linkPetMap = db_team.getLinkPetMap().values();
                if (linkPetMap.contains(petMap.getPetIdx())) {
                    return RetCodeId.RetCodeEnum.RCE_PrepareWar_RepeatedSetSamePet;
                }
            }
        }
        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    public static RetCodeId.RetCodeEnum checkSkill(String playerIdx, List<PrepareWar.SkillMap> skillMaps) {
        if (skillMaps == null || skillMaps.isEmpty()) {
            return RetCodeId.RetCodeEnum.RCE_Success;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return RetCodeId.RetCodeEnum.RCE_UnknownError;
        }
        int playerLv = player.getLevel();
        List<Integer> ownSkill = player.getSkillIds();

        Set<Integer> skillSet = new HashSet<>();
        for (PrepareWar.SkillMap skillMap : skillMaps) {
            if (skillMap.getSkillPosition() == null || skillMap.getSkillPosition() == PrepareWar.TeamSkillPositionEnum.TSPE_Position_Null) {
                return RetCodeId.RetCodeEnum.RCE_ErrorParam;
            }

            if (!ownSkill.contains(skillMap.getSkillCfgId())) {
                return RetCodeId.RetCodeEnum.RCE_PrepareWar_SkillNotExist;
            }

            PlayerSkillConfigObject skillById = PlayerSkillConfig.getBySkillIdAndStar(skillMap.getSkillCfgId(), GameConst.PlayerSkillDefaultStar);
            if (skillById == null) {
                return RetCodeId.RetCodeEnum.RCE_PrepareWar_SkillNotExist;
            }

            if (playerLv < skillById.getPlayerlv()) {
                return RetCodeId.RetCodeEnum.RCE_LvNotEnough;
            }

            if (skillSet.contains(skillMap.getSkillCfgId())) {
                return RetCodeId.RetCodeEnum.RCE_ErrorParam;
            }

            skillSet.add(skillMap.getSkillCfgId());
        }
        return RetCodeId.RetCodeEnum.RCE_Success;
    }

    public static boolean isInPetTransfer(String playerIdx, String petIdx) {
        if (petIdx == null || playerIdx == null) {
            return false;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }

        return SyncExecuteFunction.executeFunction(player, entity -> {
            PlayerDB.DB_PlayerData.Builder db_data = player.getDb_data();
            if (db_data == null) {
                return false;
            }

            PlayerInfo.PetTransferInfo petTransfer = db_data.getAncientAltarBuilder().getPetTransfer();
            if (petIdx.equalsIgnoreCase(petTransfer.getSrcPetIdx())) {
                return true;
            }
            return false;
        });
    }
}
