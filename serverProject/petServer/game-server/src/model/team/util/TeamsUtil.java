package model.team.util;

import cfg.ArenaLeagueObject;
import cfg.ServerStringRes;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.arena.ArenaUtil;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.cp.CpTeamManger;
import model.crossarena.CrossArenaTopManager;
import model.foreignInvasion.dbCache.foreigninvasionCache;
import model.magicthron.MagicThronManager;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.util.PlayerUtil;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.CS_UpdateTeam;
import protocol.PrepareWar.PositionPetMap;
import protocol.PrepareWar.SC_UpdateTeam;
import protocol.PrepareWar.SkillMap;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import static protocol.RetCodeId.RetCodeEnum.RCE_Pet_PetNumLessThanCoupTeamNeed;
import static protocol.RetCodeId.RetCodeEnum.RCE_Success;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020.3.2
 */
public class TeamsUtil {

    public static TeamTypeEnum getTeamType(TeamNumEnum teamNum) {
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            return TeamTypeEnum.TTE_Null;
        }
        if (teamNum.getNumber() >= TeamNumEnum.TNE_Team_1_VALUE && teamNum.getNumber() <= TeamNumEnum.TNE_Team_5_VALUE) {
            return TeamTypeEnum.TTE_Common;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Courge_VALUE) {
            return TeamTypeEnum.TTE_CourageTrial;
        } else if (teamNum.getNumber() >= TeamNumEnum.TNE_Mine_1_VALUE && teamNum.getNumber() <= TeamNumEnum.TNE_Mine_3_VALUE) {
            return TeamTypeEnum.TTE_Mine;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_FriendHelp_VALUE) {
            return TeamTypeEnum.TTE_FriendHelp;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Patrol_1_VALUE) {
            return TeamTypeEnum.TTE_Patrol;
        } else if (teamNum.getNumber() >= TeamNumEnum.TNE_Arena_Attack_1_VALUE
                && teamNum.getNumber() <= TeamNumEnum.TNE_Arena_Defense_3_VALUE) {
            return TeamTypeEnum.TTE_Arena;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Boss_1_VALUE) {
            return TeamTypeEnum.TTE_Boss;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_MistForest_1_VALUE) {
            return TeamTypeEnum.TTE_MistForest;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_ForeignInvasion_1_VALUE) {
            return TeamTypeEnum.TTE_ForeignInvasion;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_GloryRoad_1_VALUE) {
            return TeamTypeEnum.TTE_GloryRoad;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_MatchArena_1_VALUE) {
            return TeamTypeEnum.TTE_MatchArena;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Training_1_VALUE) {
            return TeamTypeEnum.TTE_Training;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_Coupon_VALUE) {
            return TeamTypeEnum.TTE_Coup;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_MatchArenaRank_1_VALUE) {
            return TeamTypeEnum.TTE_MatchArenaRank;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_MatchArenaNormal_1_VALUE) {
            return TeamTypeEnum.TTE_MatchArenaNormal;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_MatchArenaLeiTai_1_VALUE) {
            return TeamTypeEnum.TTE_MatchArenaLeiTai;
        }else if (teamNum.getNumber() == TeamNumEnum.TNE_Magic_VALUE) {
            return TeamTypeEnum.TTE_MagicThron;
        }else if (teamNum.getNumber() == TeamNumEnum.TNE_LtCP_1_VALUE) {
            return TeamTypeEnum.TTE_LtCP;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_CrazyDuel_1_VALUE) {
            return TeamTypeEnum.TTE_CrazyDuel;
        }else if (teamNum.getNumber() == TeamNumEnum.TNE_QIECUO_VALUE) {
            return TeamTypeEnum.TTE_QIECUO;
        } else if (teamNum.getNumber() == TeamNumEnum.TNE_TopPlay_1_VALUE) {
            return TeamTypeEnum.TTE_TopPlay;
        }else if (teamNum.getNumber() == TeamNumEnum.TNE_OfferReward_VALUE) {
            return TeamTypeEnum.TTE_OfferReward;
        }else if (teamNum.getNumber() == TeamNumEnum.TNE_Episode_1_VALUE) {
            return TeamTypeEnum.TTE_Episode;
        }else if (teamNum.getNumber() == TeamNumEnum.TNE_FestivalBoss_1_VALUE) {
            return TeamTypeEnum.TTE_FestivalBoss;
        }


        return TeamTypeEnum.TTE_Null;
    }

    public static String builderTeamName(TeamNumEnum teamNum, String linkPlayerIdx) {
        String team = ServerStringRes.getContentByLanguage(7, PlayerUtil.queryPlayerLanguage(linkPlayerIdx));
        if (TeamsUtil.getTeamType(teamNum) == TeamTypeEnum.TTE_Mine) {
            int num = teamNum.getNumber() - TeamNumEnum.TNE_Mine_1_VALUE + 1;
            return team + num;
        }
        return team + teamNum.getNumber();
    }

    /**
     * 判断是否是竞技场进攻小队
     *
     * @return
     */
    public static boolean isArenaAttack(TeamNumEnum teamNumEnum) {
        return teamNumEnum != null && isArenaAttack(teamNumEnum.getNumber());
    }

    public static boolean isArenaAttack(int teamNumValue) {
        return teamNumValue < TeamNumEnum.TNE_Arena_Defense_1_VALUE
                && teamNumValue >= TeamNumEnum.TNE_Arena_Attack_1_VALUE;
    }

    /**
     * 玩家可以使用的竞技场小队位置数量
     *
     * @param playerIdx
     * @param teamNumValue
     * @return
     */
    public static int getArenaCanUsePositionCount(String playerIdx, int teamNumValue) {
        int dan = ArenaUtil.queryPlayerDan(playerIdx);
        ArenaLeagueObject leagueCfg = ArenaUtil.getArenaLeagueCfgByDanId(ArenaUtil.queryPlayerDan(playerIdx));
        if (leagueCfg == null) {
            LogUtil.error("TeamsUtil.getArenaCanUsePositionCount, dan cfg is not exist, dan:" + dan);
            return 0;
        }

        for (int[] ints : leagueCfg.getCanuseteamnumandpetcount()) {
            if (ints.length < 2) {
                LogUtil.error("ArenaLeagueObject Can use team num and pet count cfg error, id:" + leagueCfg.getId());
                continue;
            }

            if (ints[0] == teamNumValue) {
                return ints[1];
            }
        }
        return 0;
    }

    /**
     * 玩家更新队伍信息
     *
     * @param teams
     * @param req
     * @return
     */
    public static void updateTeamInfo(teamEntity teams, CS_UpdateTeam req) {
        //需要更新不在小队的宠物状态
        Set<String> removeFromTeam = new HashSet<>();
        //需要更新新加宠物状态
        Set<String> addToTeam = new HashSet<>();
        List<String> beforeUpdate = new ArrayList<>();

        TeamNumEnum teamNum = req.getTeamNum();
        SyncExecuteFunction.executeConsumer(teams, t -> {
            Team dbTeam = teams.getDBTeam(teamNum);

            beforeUpdate.addAll(dbTeam.getLinkPetMap().values());

            dbTeam.clearLinkPet();
            for (PositionPetMap positionPetMap : req.getMapsList()) {
                dbTeam.putLinkPet(positionPetMap.getPositionValue(), positionPetMap.getPetIdx());
                addToTeam.add(positionPetMap.getPetIdx());
            }

            dbTeam.clearLinkSkill();
            for (SkillMap skillMap : req.getSkillMapList()) {
                dbTeam.putLinkSkill(skillMap.getSkillPositionValue(), skillMap.getSkillCfgId());
            }

            teams.sendRefreshTeamsMsg(teamNum);

            Set<String> stillInTeam = new HashSet<>();
            for (String idx : beforeUpdate) {
                if (teams.petIdxInTeam(idx)) {
                    stillInTeam.add(idx);
                }
            }

            for (String idx : beforeUpdate) {
                if (!stillInTeam.contains(idx)) {
                    removeFromTeam.add(idx);
                }
            }
            addToTeam.removeAll(stillInTeam);
        });

        EventUtil.updatePetTeamState(teams.getLinkplayeridx(), removeFromTeam, false, true);
        EventUtil.updatePetTeamState(teams.getLinkplayeridx(), addToTeam, true, true);


        SC_UpdateTeam.Builder retBuilder = SC_UpdateTeam.newBuilder();
        retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(teams.getLinkplayeridx(), MsgIdEnum.SC_UpdateTeam_VALUE, retBuilder);

        teams.updateTeamInfoToCrossServer(teams.getLinkplayeridx(), teamNum);

        if (TeamNumEnum.TNE_Team_1 == teamNum) {
            Collection<String> petIds = teams.getDBTeam(teamNum).getLinkPetMap().values();
            //主线队伍1日志记录
            petCache.getInstance().statisticTeamUpdate(teams.getLinkplayeridx(), beforeUpdate, petIds);
            teamCache.settleAllPetUpdate(teams.getLinkplayeridx());
        } else if (TeamNumEnum.TNE_LtCP_1== teamNum){
            CpTeamManger.getInstance().uploadPlayerInfo(teams.getLinkplayeridx());
        } else if (TeamNumEnum.TNE_TopPlay_1== teamNum) {
        	CrossArenaTopManager.getInstance().updataTeamRef(teams.getLinkplayeridx());
        }  else if (TeamsUtil.getTeamType(teamNum) == TeamTypeEnum.TTE_Arena
                && !TeamsUtil.isArenaAttack(teamNum)) {
            teams.updateArenaTemFightAbility();
        }

    }

    public static void updateTeamInfoTrain(String playerIdx) {
        teamEntity teams = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (null == teams) {
            return;
        }
        SyncExecuteFunction.executeConsumer(teams, teamEntity -> {
            Team dbTeam = teamEntity.getDBTeam(TeamNumEnum.TNE_Training_1);
            dbTeam.clearLinkPet();
            if (dbTeam.getLinkPetMap() != null) {
                Map<Integer, String> canPet = new HashMap<Integer, String>();
                for (Map.Entry<Integer, String> ent : dbTeam.getLinkPetMap().entrySet()) {
                    if (teamEntity.petExist(TeamNumEnum.TNE_Training_1, ent.getValue())) {
                        canPet.put(ent.getKey(), ent.getValue());
                    }
                }
                dbTeam.putAllLinkPet(canPet);
            }
            teamEntity.sendRefreshTeamsMsg(TeamNumEnum.TNE_Training_1);
        });
    }

    public static int getPetRemainHp(String playerIdx, TeamTypeEnum teamType, String petIdx) {
        if (teamType == TeamTypeEnum.TTE_CourageTrial) {
            return bravechallengeCache.getInstance().getPetRemainHpRate(playerIdx, petIdx);
        } else if (teamType == TeamTypeEnum.TTE_ForeignInvasion) {
            return foreigninvasionCache.getInstance().getPlayerPetRemainHp(playerIdx, petIdx);
        }
        return GameConst.PetMaxHpRate;
    }


    public static int queryMainLineTeamMaxPetNum(String playerIdx) {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity == null) {
            return -1;
        }
        return teamEntity.getUnlockPosition();
    }


    public static RetCodeEnum checkCoupTeamByPetRemove(String playerId, List<String> materialPetsList) {
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerId);
        if (petEntity.peekAllPetByUnModify().size() - materialPetsList.size() < TeamsUtil.queryMainLineTeamMaxPetNum(playerId)) {
            return RCE_Pet_PetNumLessThanCoupTeamNeed;
        }
        return RCE_Success;
    }

    public static int queryCoupTeamLv(String playerIdx) {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity == null) {
            return 0;
        }
        return teamEntity.getDB_Builder().getCoupTeamLv();

    }
}
