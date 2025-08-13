/**
 * created by tool DAOGenerate
 */
package model.team.entity;

import cfg.GameConfig;
import cfg.MistCommonConfig;
import cfg.MistCommonConfigObject;
import cfg.PlayerSkillConfig;
import cfg.PlayerSkillConfigObject;
import cfg.TeamPosition;
import cfg.TeamPositionObject;
import cfg.TeamsConfig;
import cfg.TeamsConfigObject;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import model.arena.ArenaUtil;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.obj.BaseObj;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.team.dbCache.teamCache;
import model.team.util.TeamsUtil;
import model.training.TrainingManager;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Arena.ArenaPlayerTeamInfo;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.MessageId.MsgIdEnum;

import static protocol.MessageId.MsgIdEnum.SC_CoupTeamInfo_VALUE;

import protocol.MistForest.EnumMistRuleKind;
import protocol.PetMessage;
import protocol.PetMessage.Pet;
import protocol.PrepareWar;
import protocol.PrepareWar.PetMap;
import protocol.PrepareWar.SC_RefreashTeam;
import protocol.PrepareWar.SC_TeamsInfo;
import protocol.PrepareWar.SC_UpdateTeamSkill;
import protocol.PrepareWar.TeamInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamSkillState;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.GS_CS_UpdateArenaTeamsInfo;
import protocol.ServerTransfer.GS_CS_UpdatePetData;
import protocol.TransServerCommon.PlayerMistServerInfo;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class teamEntity extends BaseObj {

    public String getClassType() {
        return "teamEntity";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private String linkplayeridx;

    /**
     *
     */
    private byte[] teamsinfo;


    /**
     * 获得
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得
     */
    public String getLinkplayeridx() {
        return linkplayeridx;
    }

    /**
     * 设置
     */
    public void setLinkplayeridx(String linkplayeridx) {
        this.linkplayeridx = linkplayeridx;
    }

    /**
     * 获得
     */
    private byte[] getTeamsinfo() {
        return this.teamsinfo;
    }

    /**
     * 设置
     */
    private void setTeamsinfo(byte[] teamsInfo) {
        this.teamsinfo = teamsInfo;
    }


    public String getBaseIdx() {
        return idx;
    }

    private teamEntity() {
    }

    /**
     * =========================================================================
     */
    @Override
    public void putToCache() {
        teamCache.put(this);
    }

    private TeamsDB db_data;

    private TeamsDB getDBTeams() {
        try {
            if (this.teamsinfo != null) {
                return (TeamsDB) TeamsDB.parseFrom(teamsinfo);
            } else {
                return new TeamsDB();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public TeamsDB getDB_Builder() {
        if (this.db_data == null) {
            this.db_data = getDBTeams();
        }
        return db_data;
    }

    @Override
    public void transformDBData() {
        this.teamsinfo = getDB_Builder().toByteArray();
    }

    public teamEntity(String playerIdx) {
        this.idx = IdGenerator.getInstance().generateId();
        this.linkplayeridx = playerIdx;
    }

    public int getUnlockPosition() {
        int unlockPosition = 0;
        int playerLv = PlayerUtil.queryPlayerLv(getLinkplayeridx());
        Map<Integer, TeamPositionObject> ix_positionid = TeamPosition._ix_positionid;
        for (TeamPositionObject value : ix_positionid.values()) {
            if (playerLv >= value.getUnlocklv()) {
                unlockPosition++;
            }
        }
        return unlockPosition;
    }

    private List<TeamNumEnum> getUnlockTeamsNum() {
        List<TeamNumEnum> teams = new ArrayList<>();
        Map<Integer, TeamsConfigObject> ix_teamid = TeamsConfig._ix_teamid;
        int playerLv = PlayerUtil.queryPlayerLv(getLinkplayeridx());
        for (TeamsConfigObject value : ix_teamid.values()) {
            if (playerLv > value.getUnlockneedlv()) {
                TeamNumEnum teamNumEnum = TeamNumEnum.forNumber(value.getTeamid());
                if (TeamsUtil.getTeamType(teamNumEnum) == TeamTypeEnum.TTE_Common) {
                    teams.add(teamNumEnum);
                }
            }
        }
        return teams;
    }


    public void initTeamsInfo() {
        TeamsDB teamsBuilder = getDB_Builder();
        if (teamsBuilder == null) {
            LogUtil.error("playerIdx[" + linkplayeridx + "] DbData is null");
            return;
        }

        teamsBuilder.setUnlockPosition(getUnlockPosition());
        List<TeamNumEnum> unlockTeamsNum = getUnlockTeamsNum();
        teamsBuilder.setUnlockTeams(unlockTeamsNum.size());

        //初始化普通小队
        for (TeamNumEnum teamNumEnum : unlockTeamsNum) {
            teamsBuilder.putTeams(TeamNumEnum.TNE_Team_1_VALUE, initDBTeam(teamNumEnum, true));
        }

        teamsBuilder.putTeams(TeamNumEnum.TNE_Coupon_VALUE, initCoupTeam());
        transformDBData();
        LogUtil.info("[" + linkplayeridx + "] initTeamsInfo finished");
    }

    public void sendRefreshTeamsMsg(TeamTypeEnum typeEnum) {
        List<Team> teamType = getTeamsByTeamType(typeEnum);
        if (CollectionUtils.isEmpty(teamType)) {
            return;
        }
        for (Team Team : teamType) {
            sendRefreshTeamsMsg(Team.getTeamNum());
        }
    }

    private Team initDBTeam(TeamNumEnum teamNum, boolean setSkill) {
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            LogUtil.error("teamEntity.initDBTeam, error param " + teamNum);
            return null;
        }
        if (TeamNumEnum.TNE_Coupon == teamNum) {
            return initCoupTeam();
        }
        Team builder = new Team();
        builder.setTeamNum(teamNum);
        //不初始化名字
//        builder.setTeamName(TeamsUtilTeamName(teamNum, linkplayeridx));

        if (setSkill) {
            int defaultSkillCfgId = GameConfig.getById(GameConst.CONFIG_ID).getDefaultskillcfgid();
            PlayerSkillConfigObject skillCfg = PlayerSkillConfig.getBySkillIdAndStar(defaultSkillCfgId, GameConst.PlayerSkillDefaultStar);
            if (skillCfg == null || skillCfg.getPlayerlv() > PlayerUtil.queryPlayerLv(getLinkplayeridx())) {
                LogUtil.warn("gameCfg default skill id is not exist or not unlock, defaultSkillId =" + defaultSkillCfgId);
            } else {
                builder.putLinkSkill(1, defaultSkillCfgId);
            }
        }
        return builder;
    }

    public void sendRefreshTeamsMsg(TeamNumEnum teamNum) {
        if (teamNum == null) {
            return;
        }
        TeamsDB teamsInfo = getDB_Builder();
        if (teamsInfo == null) {
            return;
        }

        SC_RefreashTeam.Builder refreshTeam = SC_RefreashTeam.newBuilder();
        refreshTeam.setUnlockTeams(teamsInfo.getUnlockTeams());
        refreshTeam.setUnlockPosition(teamsInfo.getUnlockPosition());
        refreshTeam.setTeamInfo(buildTeamInfo(getDBTeam(teamNum)));
        refreshTeam.setTeamType(TeamsUtil.getTeamType(teamNum));
        GlobalData.getInstance().sendMsg(linkplayeridx, MsgIdEnum.SC_RefreashTeam_VALUE, refreshTeam);
    }

    /**
     * @param isBuy 解锁方式  是不是通过购买
     */
    public boolean unlockOneTeam(boolean isBuy) {
        TeamsDB teamsInfo = this.getDB_Builder();
        if (teamsInfo == null) {
            LogUtil.info("playerIdx[" + linkplayeridx + ", dbData is null");
            return false;
        }

        if (isBuy) {
            teamsInfo.setBuyTeamCount(teamsInfo.getBuyTeamCount() + 1);
        }

        //解锁的下一个队伍编号
        int unlockTeams = teamsInfo.getUnlockTeams() + 1;
        teamsInfo.setUnlockTeams(unlockTeams);
        teamsInfo.putTeams(unlockTeams, initDBTeam(TeamNumEnum.forNumber(unlockTeams), true));
        LogUtil.info("playerIdx[" + getLinkplayeridx() + "] unlock oneTeam :" + unlockTeams);
        return true;
    }

    /**
     * 检查特殊小队是否存在，不存在则需要创建
     */
    public void checkTeams() {
        for (TeamNumEnum teamNumEnum : needCheckTeamNum()) {
            if (getDBTeam(teamNumEnum) == null) {
                putDBTeam(initDBTeam(teamNumEnum, false));
            }
        }
    }

    /**
     * 需要检查是否存在的小队号
     */
    private static final List<TeamNumEnum> NEED_CHECK_TEAM_NUM;

    static {
        List<TeamNumEnum> tempList = new ArrayList<>();
        tempList.add(TeamNumEnum.TNE_Courge);
        tempList.add(TeamNumEnum.TNE_Mine_1);
        tempList.add(TeamNumEnum.TNE_Mine_2);
        tempList.add(TeamNumEnum.TNE_Mine_3);
        tempList.add(TeamNumEnum.TNE_FriendHelp);
        tempList.add(TeamNumEnum.TNE_Patrol_1);
        tempList.add(TeamNumEnum.TNE_Arena_Attack_1);
        tempList.add(TeamNumEnum.TNE_Arena_Attack_2);
        tempList.add(TeamNumEnum.TNE_Arena_Attack_3);
        tempList.add(TeamNumEnum.TNE_Arena_Defense_1);
        tempList.add(TeamNumEnum.TNE_Arena_Defense_2);
        tempList.add(TeamNumEnum.TNE_Arena_Defense_3);
        tempList.add(TeamNumEnum.TNE_Boss_1);
        tempList.add(TeamNumEnum.TNE_MistForest_1);
        tempList.add(TeamNumEnum.TNE_ForeignInvasion_1);
        tempList.add(TeamNumEnum.TNE_GloryRoad_1);
        tempList.add(TeamNumEnum.TNE_GloryRoad_1);
        tempList.add(TeamNumEnum.TNE_MatchArena_1);
        tempList.add(TeamNumEnum.TNE_Training_1);
        tempList.add(TeamNumEnum.TNE_MatchArenaNormal_1);
        tempList.add(TeamNumEnum.TNE_MatchArenaRank_1);
        tempList.add(TeamNumEnum.TNE_MatchArenaLeiTai_1);
        tempList.add(TeamNumEnum.TNE_Magic);
        tempList.add(TeamNumEnum.TNE_OfferReward);
        tempList.add(TeamNumEnum.TNE_QIECUO);
        tempList.add(TeamNumEnum.TNE_LtCP_1);
        tempList.add(TeamNumEnum.TNE_CrazyDuel_1);
        tempList.add(TeamNumEnum.TNE_TopPlay_1);
        tempList.add(TeamNumEnum.TNE_Episode_1);
        tempList.add(TeamNumEnum.TNE_FestivalBoss_1);

        NEED_CHECK_TEAM_NUM = Collections.unmodifiableList(tempList);
    }

    private List<TeamNumEnum> needCheckTeamNum() {
        return NEED_CHECK_TEAM_NUM;
//        List<TeamNumEnum> result = new ArrayList<>();
//        result.add(TeamNumEnum.TNE_Courge);
//        result.add(TeamNumEnum.TNE_Mine_1);
//        result.add(TeamNumEnum.TNE_Mine_2);
//        result.add(TeamNumEnum.TNE_Mine_3);
//        result.add(TeamNumEnum.TNE_FriendHelp);
//        result.add(TeamNumEnum.TNE_Patrol_1);
//        result.add(TeamNumEnum.TNE_Arena_Attack_1);
//        result.add(TeamNumEnum.TNE_Arena_Attack_2);
//        result.add(TeamNumEnum.TNE_Arena_Attack_3);
//        result.add(TeamNumEnum.TNE_Arena_Defense_1);
//        result.add(TeamNumEnum.TNE_Arena_Defense_2);
//        result.add(TeamNumEnum.TNE_Arena_Defense_3);
//        result.add(TeamNumEnum.TNE_Boss_1);
//        result.add(TeamNumEnum.TNE_MistForest_1);
//        result.add(TeamNumEnum.TNE_ForeignInvasion_1);
//        result.add(TeamNumEnum.TNE_GloryRoad_1);
//        result.add(TeamNumEnum.TNE_GloryRoad_1);
//        result.add(TeamNumEnum.TNE_MatchArena_1);
//        return result;
    }

    public void sendTeamsInfo() {
        checkTeams();
        SC_TeamsInfo.Builder teamsBuilder = SC_TeamsInfo.newBuilder();
        TeamsDB db_info = getDB_Builder();
        if (db_info == null) {
            LogUtil.error("playerIdx[" + getLinkplayeridx() + "] DBData is null");
            return;
        }

        teamsBuilder.setUnlockTeams(db_info.getUnlockTeams());
        teamsBuilder.setUnlockPosition(db_info.getUnlockPosition());

        teamsBuilder.setNowUsedMineTeam(getNowUsedTeamNum(TeamTypeEnum.TTE_Mine));
        teamsBuilder.setBuyTeamsCount(db_info.getBuyTeamCount());

        for (Team value : db_info.getTeamsMap().values()) {
            teamsBuilder.setNowUsedTeam(getNowUsedTeamNum(TeamTypeEnum.TTE_Common));
            TeamTypeEnum teamType = TeamsUtil.getTeamType(value.getTeamNum());
            if (teamType == TeamTypeEnum.TTE_Common) {
                teamsBuilder.addTeamsInfo(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_CourageTrial) {
                teamsBuilder.setCourgeTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_Mine) {
                teamsBuilder.addMineTeams(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_FriendHelp) {
                teamsBuilder.setFriendHelp(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_Patrol) {
                teamsBuilder.setPatrolTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_Arena) {
                teamsBuilder.addArenaTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_Boss) {
                teamsBuilder.setBossTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_MistForest) {
                teamsBuilder.setMistTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_ForeignInvasion) {
                teamsBuilder.setForeignInvasionTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_GloryRoad) {
                teamsBuilder.setGloryRoadTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_MatchArena) {
                teamsBuilder.setMatchArenaTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_Training) {
                teamsBuilder.setTrainingTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_Coup) {
                teamsBuilder.addTeamsInfo(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_MatchArenaRank) {
                teamsBuilder.setMatchArenaRank(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_MatchArenaNormal) {
                teamsBuilder.setMatchArenaNormal(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_MatchArenaLeiTai) {
                teamsBuilder.setMatchArenaLTTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_MagicThron) {
                teamsBuilder.setMagicThronTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_OfferReward) {
                teamsBuilder.setOfferRewardTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_LtCP) {
                teamsBuilder.setCpTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_CrazyDuel) {
                teamsBuilder.setCrazyDuelTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_TopPlay) {
                teamsBuilder.setTopPlayTeam(buildTeamInfo(value));
            } else if (teamType == TeamTypeEnum.TTE_Episode) {
                teamsBuilder.addTeamsInfo(buildTeamInfo(value));
            }else if (teamType == TeamTypeEnum.TTE_FestivalBoss) {
                teamsBuilder.addTeamsInfo(buildTeamInfo(value));
            }
        }
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_TeamsInfo_VALUE, teamsBuilder);
        LogUtil.info("playerIdx[" + getLinkplayeridx() + "] send teamsInfo");
    }

    public TeamInfo.Builder buildTeamInfo(Team Team) {
        TeamInfo.Builder teamBuilder = TeamInfo.newBuilder();
        if (Team == null) {
            return teamBuilder;
        }
        teamBuilder.setTeamNum(Team.getTeamNum());
        teamBuilder.setTeamName(Team.getTeamName());

        Map<Integer, String> linkPetMap = Team.getLinkPetMap();
        for (Entry<Integer, String> entry : linkPetMap.entrySet()) {
            PetMap.Builder petMap = PetMap.newBuilder();
            petMap.setPosition(entry.getKey());
            petMap.setPetIdx(entry.getValue());
            teamBuilder.addPetMap(petMap);
        }

        Map<Integer, Integer> linkSkillMap = Team.getLinkSkillMap();
        for (Entry<Integer, Integer> entry : linkSkillMap.entrySet()) {
            if (entry.getKey() == 1) {
                teamBuilder.setLinkSkillId1(entry.getValue());
            } else if (entry.getKey() == 2) {
                teamBuilder.setLinkSkillId2(entry.getValue());
            }
        }
        if (TeamsUtil.getTeamType(Team.getTeamNum()) == TeamTypeEnum.TTE_Mine
                || TeamsUtil.getTeamType(Team.getTeamNum()) == TeamTypeEnum.TTE_FriendHelp) {
            teamBuilder.setIsLock(Team.isLock());
        }
        return teamBuilder;
    }

    public boolean unlockOnePosition() {
        TeamsDB builder = getDB_Builder();
        if (builder == null) {
            LogUtil.error("playerIdx[" + getLinkplayeridx() + "] TeamsDB is null");
            return false;
        }

        if (builder.getUnlockPosition() > TeamPosition._ix_positionid.size()) {
            return true;
        }

        int unlockPosition = builder.getUnlockPosition();
        if (unlockPosition >= TeamPosition.totalPosition) {
            return true;
        }
        builder.setUnlockPosition(unlockPosition + 1);

        putToCache();
        LogUtil.info("playerIdx[" + getLinkplayeridx() + "] unlock onePosition :" + (unlockPosition + 1));
        return true;

    }

    public Team getNowUsedTeamInfo(TeamTypeEnum teamType) {
        return getDBTeam(getNowUsedTeamNum(teamType));
    }

    public TeamNumEnum getNowUsedTeamNum(TeamTypeEnum teamType) {
        TeamsDB db_data = getDB_Builder();
        if (teamType == null || db_data == null) {
            return TeamNumEnum.TNE_Team_Null;
        }
        Integer nowUsedTeamNum = db_data.getNowUsedTeamMap().get(teamType.getNumber());

        if (nowUsedTeamNum == null || nowUsedTeamNum == TeamNumEnum.TNE_Team_Null_VALUE
                || teamType != TeamsUtil.getTeamType(TeamNumEnum.forNumber(nowUsedTeamNum))) {

            TeamNumEnum nowUsedTeam = TeamNumEnum.TNE_Team_1;
            if (teamType == TeamTypeEnum.TTE_Common) {
                nowUsedTeam = TeamNumEnum.TNE_Team_1;
            } else if (teamType == TeamTypeEnum.TTE_CourageTrial) {
                nowUsedTeam = TeamNumEnum.TNE_Courge;
            } else if (teamType == TeamTypeEnum.TTE_Mine) {
                nowUsedTeam = TeamNumEnum.TNE_Mine_1;
            } else if (teamType == TeamTypeEnum.TTE_FriendHelp) {
                nowUsedTeam = TeamNumEnum.TNE_FriendHelp;
            } else if (teamType == TeamTypeEnum.TTE_Patrol) {
                nowUsedTeam = TeamNumEnum.TNE_Patrol_1;
            } else if (teamType == TeamTypeEnum.TTE_Arena) {
                nowUsedTeam = TeamNumEnum.TNE_Arena_Attack_1;
            } else if (teamType == TeamTypeEnum.TTE_Boss) {
                nowUsedTeam = TeamNumEnum.TNE_Boss_1;
            } else if (teamType == TeamTypeEnum.TTE_MistForest) {
                nowUsedTeam = TeamNumEnum.TNE_MistForest_1;
            } else if (teamType == TeamTypeEnum.TTE_ForeignInvasion) {
                nowUsedTeam = TeamNumEnum.TNE_ForeignInvasion_1;
            } else if (teamType == TeamTypeEnum.TTE_GloryRoad) {
                nowUsedTeam = TeamNumEnum.TNE_GloryRoad_1;
            } else if (teamType == TeamTypeEnum.TTE_MatchArena) {
                nowUsedTeam = TeamNumEnum.TNE_MatchArena_1;
            } else if (teamType == TeamTypeEnum.TTE_Training) {
                nowUsedTeam = TeamNumEnum.TNE_Training_1;

            } else if (teamType == TeamTypeEnum.TTE_MatchArenaNormal) {
                nowUsedTeam = TeamNumEnum.TNE_MatchArenaNormal_1;
            } else if (teamType == TeamTypeEnum.TTE_MatchArenaRank) {
                nowUsedTeam = TeamNumEnum.TNE_MatchArenaRank_1;
            } else if (teamType == TeamTypeEnum.TTE_MatchArenaLeiTai) {
                nowUsedTeam = TeamNumEnum.TNE_MatchArenaLeiTai_1;
            } else if (teamType == TeamTypeEnum.TTE_MagicThron) {
                nowUsedTeam = TeamNumEnum.TNE_Magic;
            } else if (teamType == TeamTypeEnum.TTE_OfferReward) {
                nowUsedTeam = TeamNumEnum.TNE_OfferReward;
            } else if (teamType == TeamTypeEnum.TTE_QIECUO) {
                nowUsedTeam = TeamNumEnum.TNE_QIECUO;
            } else if (teamType == TeamTypeEnum.TTE_LtCP) {
                nowUsedTeam = TeamNumEnum.TNE_LtCP_1;
            } else if (teamType == TeamTypeEnum.TTE_CrazyDuel) {
                nowUsedTeam = TeamNumEnum.TNE_CrazyDuel_1;
            } else if (teamType == TeamTypeEnum.TTE_TopPlay) {
                nowUsedTeam = TeamNumEnum.TNE_TopPlay_1;
            } else if (teamType == TeamTypeEnum.TTE_Episode) {
                nowUsedTeam = TeamNumEnum.TNE_Episode_1;
            } else if (teamType == TeamTypeEnum.TTE_FestivalBoss) {
                nowUsedTeam = TeamNumEnum.TNE_FestivalBoss_1;
            }


            db_data.putNowUsedTeam(teamType.getNumber(), nowUsedTeam.getNumber());
            return nowUsedTeam;
        }

        return TeamNumEnum.forNumber(nowUsedTeamNum);
    }

    public Team getDBTeam(TeamNumEnum teamNum) {
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            LogUtil.error("teamEntity.getDBTeam, error param");
            return null;
        }
        TeamsDB db_builder = getDB_Builder();
        if (db_builder == null) {
            LogUtil.error("teamEntity.getDBTeam, playerIdx[" + linkplayeridx + "] DbData is null");
            return null;
        }
        return db_builder.getTeamsMap().get(teamNum.getNumber());
    }

    public boolean putDBTeam(Team team) {
        if (team == null) {
            LogUtil.error("teamEntity.putDBTeam, error param");
            return false;
        }
        TeamsDB db_builder = getDB_Builder();
        if (db_builder == null) {
            LogUtil.error("teamEntity.getDBTeam, playerIdx[" + linkplayeridx + "] DbData is null");
            return false;
        }
        db_builder.putTeams(team.getTeamNum().getNumber(), team);
        return true;
    }

    /**
     * 获取指定类型的所有小队
     *
     * @return
     */
    public List<Team> getTeamsByTeamType(TeamTypeEnum teamType) {
        TeamsDB db_builder = getDB_Builder();
        if (db_builder == null) {
            LogUtil.error("teamEntity.petIsSetInMineTeams, error params");
            return null;
        }

        List<Team> resultTeams = new ArrayList<>();
        for (Team value : db_builder.getTeamsMap().values()) {
            if (TeamsUtil.getTeamType(value.getTeamNum()) == teamType) {
                resultTeams.add(value);
            }
        }
        return resultTeams;
    }

    /**
     * 锁定小队,锁定后无法编辑, 暂时只提供矿区小队和好友助阵上锁
     *
     * @param teamNum 小队编号
     * @param lock    true加锁, false解锁
     * @return
     */
    public boolean lockTeam(TeamNumEnum teamNum, boolean lock) {
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            LogUtil.error("teamEntity.lockMineTeam, error param");
            return false;
        }

        if (TeamsUtil.getTeamType(teamNum) != TeamTypeEnum.TTE_Mine && TeamsUtil.getTeamType(teamNum) != TeamTypeEnum.TTE_FriendHelp) {
            LogUtil.error("teamEntity.lockMineTeam, unSupported lock team Type");
            return false;
        }

        Team dbTeam = getDBTeam(teamNum);
        if (dbTeam == null) {
            LogUtil.error("teamEntity.lockMineTeam, teamNUm = " + teamNum + ", entity is null");
            return false;
        }

        Team builder = dbTeam;
        if (builder.isLock() == lock) {
            LogUtil.warn("teamEntity.lockMineTeam, curStatus is same as targetStatus = " + lock + "]");
        }
        builder.setLock(lock);
        putDBTeam(builder);
        sendRefreshTeamsMsg(teamNum);
        return true;
    }

    /**
     * 判断该宠物是否任然在所有的队伍中装备
     *
     * @return
     */
    public boolean petIdxInTeam(String petIdx) {
        if (petIdx == null) {
            return false;
        }

        TeamsDB db_builder = getDB_Builder();
        if (db_builder == null) {
            return false;
        }

        Map<Integer, Team> teamsMap = db_builder.getTeamsMap();
        if (teamsMap == null) {
            LogUtil.error("playerIdx[" + linkplayeridx + "], teams  is null");
            return false;
        }

        for (Team value : teamsMap.values()) {
            if (TeamNumEnum.TNE_Coupon == value.getTeamNum()) {
                continue;
            }
            Map<Integer, String> linkPetMap = value.getLinkPetMap();
            if (linkPetMap == null || linkPetMap.isEmpty()) {
                continue;
            }

            for (String idx : linkPetMap.values()) {
                if (petIdx.equalsIgnoreCase(idx)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查编队中的宠物idx状态,如果宠物不存在就删除该位置关联的idx
     */
/*    public void checkTeamPetIdx() {
        Builder db_builder = getDB_Builder();
        if (db_builder == null) {
            return;
        }

        Map<Integer, Team> teamsMap = db_builder.getTeamsMap();
        if (teamsMap == null || teamsMap.isEmpty()) {
            LogUtil.error("playerIdx[" + linkplayeridx + "], teams  is null");
            return;
        }

        for (Team value : teamsMap.values()) {
            if (value == null || value.getLinkPetCount() <= 0) {
                continue;
            }
            Team builder = value;
            Map<Integer, String> linkPetMap = builder.getLinkPet();

            Set<Integer> removeKey = new HashSet<>();
            for (Entry<Integer, String> entry : linkPetMap.entrySet()) {
                if (entry == null) {
                    break;
                }
                if (!petExist(value.getTeamNum(), entry.getValue())) {
                    removeKey.add(entry.getKey());
                }
            }

            if (!removeKey.isEmpty()) {
                for (Integer integer : removeKey) {
                    builder.removeLinkPet(integer);
                }
            }
            putDBTeam(builder);
        }
        LogUtil.info("check playerIdx[" + getLinkplayeridx() + "] teams finished");
    }*/

    /**
     * 检查宠物是否存在,宠物背包或者巡逻队助阵宠物
     */
    public boolean petExist(TeamNumEnum teamNum, String petIdx) {
        return getTeamPet(teamNum, petIdx) != null;
    }


    public Pet getTeamPet(TeamNumEnum teamNum, String petIdx) {
        if (teamNum == null || petIdx == null) {
            return null;
        }

        Pet pet = petCache.getInstance().getPetById(getLinkplayeridx(), petIdx);
        if (null == pet) {
            if (TeamsUtil.getTeamType(teamNum) == TeamTypeEnum.TTE_Patrol) {
                pet = PatrolServiceImpl.getInstance().getVirtualPet(getLinkplayeridx(), petIdx);
            } else if (TeamsUtil.getTeamType(teamNum) == TeamTypeEnum.TTE_Training) {
                pet = TrainingManager.getInstance().getVirtualPet(getLinkplayeridx(), petIdx);
            }
        }

        return pet;
    }


    /**
     * 检查宠物是否队伍中
     */
    public boolean petExistInTeam(TeamNumEnum teamNum, String petIdx) {
        if (teamNum == null || StringUtils.isEmpty(petIdx)) {
            return false;
        }

        Team dbTeam = getDBTeam(teamNum);
        if (dbTeam == null) {
            return false;
        }
        if (TeamsUtil.getTeamType(teamNum) == TeamTypeEnum.TTE_Patrol) {
            return PatrolServiceImpl.getInstance().getVirtualPet(getLinkplayeridx(), petIdx) != null;
        } else if (TeamsUtil.getTeamType(teamNum) == TeamTypeEnum.TTE_Training) {
            return TrainingManager.getInstance().getVirtualPet(getLinkplayeridx(), petIdx) != null;
        }
        return dbTeam.getLinkPetMap().containsValue(petIdx);


    }


    /**
     * 获得指定小队的战斗力
     *
     * @param teamNum
     * @return
     */
    public long getTeamFightAbility(TeamNumEnum teamNum) {
        Team dbTeam = getDBTeam(teamNum);
        if (dbTeam == null || dbTeam.getLinkPetCount() <= 0) {
            return 0L;
        }

        return petCache.getInstance().calculateTeamAbility(getLinkplayeridx(), dbTeam.getLinkPetMap().values());
    }

    public void clearTeam(TeamNumEnum teamNum, boolean sendMsg) {
        Team dbTeam = getDBTeam(teamNum);
        if (dbTeam == null || (dbTeam.getLinkPetCount() <= 0 && dbTeam.getLinkSkillCount() <= 0)) {
            return;
        }

        Team builder = dbTeam;
        List<String> removePet = new ArrayList<>(builder.getLinkPetMap().values());

        builder.clearLinkPet();
        builder.clearLinkSkill();
        putDBTeam(builder);
        if (sendMsg) {
            sendRefreshTeamsMsg(teamNum);
        }

        List<String> needUpdateRemove = new ArrayList<>();
        for (String idx : removePet) {
            if (!petIdxInTeam(idx)) {
                needUpdateRemove.add(idx);
            }
        }

        if (needUpdateRemove.isEmpty()) {
            return;
        }

        EventUtil.updatePetTeamState(getLinkplayeridx(), needUpdateRemove, false, sendMsg);
    }

    /**
     * 将指定宠物从队列中移除
     */
    public void removePetFromTeam(Set<String> petIdxSet) {
        if (GameUtil.collectionIsEmpty(petIdxSet)) {
            return;
        }

        TeamsDB dbTeams = getDBTeams();
        if (null == dbTeams) {
            return;
        }

        for (Team value : dbTeams.getTeamsMap().values()) {
            if (TeamNumEnum.TNE_Coupon == value.getTeamNum()) {
                continue;
            }
            Team teamBuilder = value;
            Set<Integer> needRemoveKey = new HashSet<>();
            for (Entry<Integer, String> entry : teamBuilder.getLinkPetMap().entrySet()) {
                if (petIdxSet.contains(entry.getValue())) {
                    needRemoveKey.add(entry.getKey());
                }
            }

            if (!needRemoveKey.isEmpty()) {
                for (Integer removeKey : needRemoveKey) {
                    teamBuilder.removeLinkPet(removeKey);
                }

                putDBTeam(teamBuilder);
                sendRefreshTeamsMsg(teamBuilder.getTeamNum());
            }

            if (TeamNumEnum.TNE_Team_1 == value.getTeamNum()) {
                petCache.getInstance().statisticTeamUpdate(getLinkplayeridx(), value.getLinkPetMap().values(), teamBuilder.getLinkPetMap().values());
            }
        }

        //更新编队信息
        EventUtil.updatePetTeamState(getLinkplayeridx(), petIdxSet, false, true);
    }

    public List<String> getTeamPetIdxList(TeamNumEnum teamNumEnum) {
        if (teamNumEnum == null || teamNumEnum == TeamNumEnum.TNE_Team_Null) {
            return null;
        }

        Team dbTeam = getDBTeam(teamNumEnum);
        if (dbTeam == null) {
            return null;
        }

        return new ArrayList<>(dbTeam.getLinkPetMap().values());
    }

    public List<Integer> getTeamSkillList(TeamNumEnum teamNum) {
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            return null;
        }

        Team dbTeam = getDBTeam(teamNum);
        if (dbTeam == null) {
            return null;
        }

        return new ArrayList<>(dbTeam.getLinkSkillMap().values());
    }

    public ArenaPlayerTeamInfo.Builder buildArenaTeamInfo(TeamNumEnum teamNum) {
        if (teamNum == null || teamNum == TeamNumEnum.TNE_Team_Null) {
            return null;
        }
        Team dbTeam = getDBTeam(teamNum);
        if (dbTeam == null) {
            return null;
        }

        ArenaPlayerTeamInfo.Builder result = ArenaPlayerTeamInfo.newBuilder();
        result.setTeanNum(teamNum);

        if (dbTeam.getLinkPetMap().size() >= 0) {
            List<BattlePetData> petBattleData = petCache.getInstance().getPetBattleData(getLinkplayeridx()
                    , new ArrayList<>(dbTeam.getLinkPetMap().values()), BattleSubTypeEnum.BSTE_Arena);
            if (CollectionUtils.isNotEmpty(petBattleData)) {
                result.addAllPets(petBattleData);
            }
        }

        result.addAllSkills(dbTeam.getLinkSkillMap().values());
        return result;
    }

    public void updateArenaTemFightAbility() {
        arenaEntity arenaEntity = arenaCache.getInstance().getEntity(getLinkplayeridx());
        if (arenaEntity != null) {
            arenaEntity.getDbBuilder().setFightAbility(ArenaUtil.getArenaDefinesBattleTotalAbility(getLinkplayeridx()
                    , arenaEntity.getDbBuilder().getDan()));
        }
    }

    public void updateTeamInfoToCrossServer(String playerIdx, TeamNumEnum teamNum) {
        //更新到矿区
        TeamTypeEnum teamType = TeamsUtil.getTeamType(teamNum);

        //更新到竞技场
        if (teamType == TeamTypeEnum.TTE_Arena
                && !TeamsUtil.isArenaAttack(teamNum)) {
            ArenaPlayerTeamInfo.Builder teamInfo = buildArenaTeamInfo(teamNum);
            if (teamInfo == null) {
                return;
            }
            GS_CS_UpdateArenaTeamsInfo.Builder builder = GS_CS_UpdateArenaTeamsInfo.newBuilder();
            builder.setTeamsInfo(teamInfo);
            builder.setPlayerIdx(playerIdx);
//            builder.setNewAblity(ArenaUtil.getArenaDefinesTotalAbility(playerIdx));
            if (!CrossServerManager.getInstance().sendMsgToArena(playerIdx, MsgIdEnum.GS_CS_UpdateArenaTeamsInfo_VALUE, builder, false)) {
                LogUtil.error("Update player:" + playerIdx + " arena team info to cross server failed, can not find arena server");
            }
//            LogUtil.debug("update player " + playerIdx + ",arena info:" + builder.toString());
        }

        //更新到迷雾森林
        if (teamType != TeamTypeEnum.TTE_MistForest) {
            return;
        }
        PlayerMistServerInfo plyMistServerInfo = CrossServerManager.getInstance().getMistForestPlayerServerInfo(playerIdx);
        if (plyMistServerInfo == null || plyMistServerInfo.getMistRule() != EnumMistRuleKind.EMRK_Common) {
            return;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return;
        }
        playerEntity owner = playerCache.getByIdx(playerIdx);
        if (owner == null) {
            return;
        }
        int mistLevel = target.getDb_Builder().getMistTaskData().getCurEnterLevel();
        MistCommonConfigObject cfg = MistCommonConfig.getByMistlevel(mistLevel);
        if (cfg == null) {
            LogUtil.error("update mist team to CS error, mistCommonCfg is null");
            return;
        }
        GS_CS_UpdatePetData.Builder builder = GS_CS_UpdatePetData.newBuilder();
        builder.setIdx(playerIdx);
        List<BattlePetData> petBattleData = teamCache.getInstance().buildBattlePetData(playerIdx, TeamNumEnum.TNE_MistForest_1, BattleSubTypeEnum.BSTE_MistForest);
        if (petBattleData != null) {
            builder.addAllPetData(petBattleData);
        }
        List<Integer> skills = teamCache.getInstance().getCurUsedTeamSkillList(playerIdx, TeamTypeEnum.TTE_MistForest);
        if (skills != null) {
            builder.addAllSkillData(owner.getSkillBattleDict(skills));
        }
        builder.putAllBaseAdditions(owner.getDb_data().getPetPropertyAdditionMap());
        CrossServerManager.getInstance().sendMsgToMistForest(playerIdx, MsgIdEnum.GS_CS_UpdatePetData_VALUE, builder, false);
    }

    public void sendUpdateTeamSkill(TeamNumEnum teamNum, int skillId) {
        Team dbTeam = getDBTeam(teamNum);
        if (dbTeam == null) {
            return;
        }
        SC_UpdateTeamSkill.Builder result = SC_UpdateTeamSkill.newBuilder()
                .setSkillId(skillId).setTeamNum(teamNum).setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        Optional<Integer> skillInDb = dbTeam.getLinkSkillMap().keySet().stream()
                .filter(position -> skillId == dbTeam.getLinkSkillMap().get(position)).findAny();
        if (skillInDb.isPresent()) {
            result.setState(TeamSkillState.TSS_Equip).setSkillPositionValue(skillInDb.get());
        } else {
            result.setState(TeamSkillState.TSS_UnLoad);
        }
        GlobalData.getInstance().sendMsg(linkplayeridx, MsgIdEnum.SC_UpdateTeamSkill_VALUE, result);
    }

    public List<Team> getTeamsByTeamNum(Collection<Integer> teamNumList) {
        if (CollectionUtils.isEmpty(teamNumList)) {
            return null;
        }

        return getTeamsByTeamEnum(teamNumList.stream().map(TeamNumEnum::forNumber).collect(Collectors.toList()));
    }

    public List<Team> getTeamsByTeamEnum(Collection<TeamNumEnum> teamEnumList) {
        if (CollectionUtils.isEmpty(teamEnumList)) {
            return null;
        }
        return teamEnumList.stream().map(this::getDBTeam).collect(Collectors.toList());
    }

    private boolean supportedRecordPetHp(TeamTypeEnum teamType) {
        return teamType == TeamTypeEnum.TTE_CourageTrial
                || teamType == TeamTypeEnum.TTE_ForeignInvasion;
    }

    public void removeDeadPets(TeamTypeEnum teamType) {
        if (!supportedRecordPetHp(teamType)) {
            return;
        }

        List<Team> dbTeamList = getTeamsByTeamType(teamType);
        if (CollectionUtils.isEmpty(dbTeamList)) {
            return;
        }

        Set<String> removePetIdx = new HashSet<>();
        for (Team dbTeam : dbTeamList) {
            Team builder = dbTeam;
            Map<Integer, String> linkPetMap = builder.getLinkPetMap();

            List<Integer> removePetPosition = new ArrayList<>();
            for (Entry<Integer, String> entry : linkPetMap.entrySet()) {
                if (TeamsUtil.getPetRemainHp(getLinkplayeridx(), teamType, entry.getValue()) <= 0) {
                    removePetPosition.add(entry.getKey());
                    removePetIdx.add(entry.getValue());
                }
            }

            for (Integer position : removePetPosition) {
                builder.removeLinkPet(position);
            }
            putDBTeam(builder);
        }

        sendRefreshTeamsMsg(teamType);
        //移除宠物编队状态
        EventUtil.updatePetTeamState(getLinkplayeridx(),
                removePetIdx.stream().filter(e -> !this.petIdxInTeam(e)).collect(Collectors.toList()), false, true);
    }

    public void updateCoupTeam() {
        Collection<String> pets = getCoupPets();
        if (CollectionUtils.isEmpty(pets)) {
            return;
        }
        Map<Integer, String> selectPetsMap = selectCoupPets();

        if (MapUtils.isEmpty(selectPetsMap)) {
            return;
        }
        //更新编队等级
        updateCoupTeamLv(selectPetsMap);
        //更新编队阵容
        if (!needUpdateCoupTeam(pets, selectPetsMap.values())) {
            return;
        }
        Team builder = updateCoupTeam(selectPetsMap);
        getDB_Builder().putTeams(TeamNumEnum.TNE_Coupon_VALUE, builder);
        sendRefreshTeamsMsg(TeamNumEnum.TNE_Coupon);
    }

    private void updateCoupTeamLv(Map<Integer, String> selectPetsMap) {
        Pet teamFirstPet = petCache.getInstance().getPetById(getLinkplayeridx(), selectPetsMap.values().iterator().next());
        if (teamFirstPet == null) {
            return;
        }
        int nowLv = teamFirstPet.getPetLvl();
        int beforeLv = getDB_Builder().getCoupTeamLv();
        if (nowLv >= beforeLv) {
            getDB_Builder().setCoupTeamLv(nowLv);
            sendCoupTeamInfo();
        }
    }

    public void sendCoupTeamInfo() {
        PrepareWar.SC_CoupTeamInfo.Builder msg = PrepareWar.SC_CoupTeamInfo.newBuilder();
        msg.setTeamLv(getDB_Builder().getCoupTeamLv());
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_CoupTeamInfo_VALUE, msg);
    }

    private boolean needUpdateCoupTeam(Collection<String> before, Collection<String> update) {
        if (before.size() != update.size()) {
            return true;
        }
        Iterator<String> iterator1 = before.iterator();
        Iterator<String> iterator2 = update.iterator();
        while (iterator1.hasNext()) {
            if (!iterator1.next().equals(iterator2.next())) {
                return true;
            }
        }
        return false;
    }

    private Collection<String> getCoupPets() {
        Team dbTeam = getDBTeam(TeamNumEnum.TNE_Coupon);
        if (dbTeam == null) {
            return Collections.emptyList();
        }
        return dbTeam.getLinkPetMap().values();
    }

    public void checkCoupTeamInit() {
        if (getDBTeam(TeamNumEnum.TNE_Coupon) == null) {
            initCoupTeam();
        }
    }

    private Team initCoupTeam(Map<Integer, String> selectPetsMap) {
        return putCoupTeamPets(selectPetsMap, true);
    }

    private Team updateCoupTeam(Map<Integer, String> selectPetsMap) {
        return putCoupTeamPets(selectPetsMap, false);
    }

    public Team putCoupTeamPets(Map<Integer, String> selectPetsMap, boolean updateTeamLv) {
        if (MapUtils.isEmpty(selectPetsMap)) {
            return new Team();
        }
        Team dbTeam = getDBTeam(TeamNumEnum.TNE_Coupon);
        Team builder;
        if (dbTeam == null) {
            builder = new Team();
            builder.setTeamNum(TeamNumEnum.TNE_Coupon);
        } else {
            builder = dbTeam;
        }
        if (updateTeamLv) {
            Pet pet = petCache.getInstance().getPetById(getLinkplayeridx(), selectPetsMap.get(0));
            if (pet != null) {
                getDB_Builder().setCoupTeamLv(pet.getPetLvl());
            }
        }
        return builder.putAllLinkPet(selectPetsMap);
    }

    private Team initCoupTeam() {
        return initCoupTeam(selectCoupPets());
    }

    private Map<Integer, String> selectCoupPets() {
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(getLinkplayeridx());
        if (petEntity == null) {
            return Collections.emptyMap();
        }
        Map<Integer, String> selectPets = new HashMap<>();
        int unlockPosition = getUnlockPosition();
        List<PetMessage.Pet> allPet = petEntity.getAllPet();
        int needPet = Math.min(unlockPosition, allPet.size());
        if (needPet <= 0) {
            return Collections.emptyMap();
        }
        allPet.sort(Comparator.comparing(PetMessage.Pet::getPetLvl).thenComparing(PetMessage.Pet::getPetRarity).reversed());
        for (int i = 0; i < needPet; i++) {
            selectPets.put(i, allPet.get(i).getId());
        }

        return selectPets;
    }

    public void resetDb() {
        db_data = new TeamsDB();
        initTeamsInfo();
        transformDBData();
    }
}