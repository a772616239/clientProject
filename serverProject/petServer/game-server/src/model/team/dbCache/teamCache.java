/*CREATED BY TOOL*/

package model.team.dbCache;

import annotation.annationInit;
import cfg.TeamPosition;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.team.cache.teamUpdateCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.PetMessage;
import protocol.PetMessage.PetDisplayInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.ServerTransfer.PetFormData;
import protocol.ServerTransfer.PetFormData.Builder;
import util.EventUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@annationInit(value = "teamCache", methodname = "load")
public class teamCache extends baseCache<teamCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static teamCache instance = null;

    /*******************MUST HAVE END ********************************/

    public static teamCache getInstance() {

        if (instance == null) {
            instance = new teamCache();
        }
        return instance;

    }

    public static void put(teamEntity v) {
        getInstance().putBaseEntity(v);
    }

    public static teamEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (teamEntity) v;

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    public static void settlePetUpdate(String playerIdx, List<PetMessage.Pet> petList) {
        if (org.apache.commons.lang.StringUtils.isEmpty(playerIdx)) {
            return;
        }
        if (!teamContainsPet(playerIdx, petList)) {
            return;
        }
        settleTeam1Update(playerIdx);

    }

    public static void settleTeam1Update(String playerIdx) {
        long teamFightAbility = teamCache.getInstance().getTeamFightAbility(playerIdx, TeamNumEnum.TNE_Team_1);
        RankingManager.getInstance().updatePlayerRankingScore(playerIdx, protocol.Activity.EnumRankingType.ERT_Team1Ability, teamFightAbility);
    }

    private static boolean teamContainsPet(String playerIdx, List<PetMessage.Pet> petList) {
        List<String> teamPetIdx = teamCache.getInstance().getTeamPetIdxList(playerIdx, protocol.PrepareWar.TeamNumEnum.TNE_Team_1);
        if (petList.stream().map(PetMessage.Pet::getId).anyMatch(teamPetIdx::contains)) {
            return true;
        }
        return false;
    }

    public static void settleAllPetUpdate(String playerId) {
        if (StringUtils.isEmpty(playerId)) {
            return;
        }
        settleTeam1Update(playerId);
    }

    public String getDaoName() {

        return "teamDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("teamDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (teamCache) o;
        }
        super.loadAllFromDb();

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return teamUpdateCache.getInstance();

    }

    public void putToMem(BaseEntity v) {

        teamEntity t = (teamEntity) v;
        if (t != null) {
            String linkplayeridx = t.getLinkplayeridx();
            if (linkplayeridx != null) {
                teamsMap.put(linkplayeridx, t);
            }
        }

    }

    /**
     * ===================================================================
     */

    //linkplayerIdx , TeamEntity
    private static Map<String, teamEntity> teamsMap = new ConcurrentHashMap<>();

    /**
     * 获得当前玩家使用的小队信息（clone),此方法只能读
     *
     * @param playerIdx
     * @return
     */
    public Team getPlayerNowUsedTeams(String playerIdx, TeamTypeEnum typeEnum) {
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            LogUtil.error("teamCache.getNowUsedTeamPetIdxList, playerIdx[" + playerIdx + "] teamEntity is null");
            return null;
        }

        return entity.getNowUsedTeamInfo(typeEnum);
    }

    public TeamNumEnum getNowUsedTeamNum(String playerIdx, TeamTypeEnum typeEnum) {
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            LogUtil.error("teamCache.getNowUsedTeamPetIdxList, playerIdx[" + playerIdx + "] teamEntity is null");
            return null;
        }

        return entity.getNowUsedTeamNum(typeEnum);
    }

    public teamEntity getTeamEntityByPlayerId(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            LogUtil.info("model.util.dbCache.teamCache.getTeamEntityByPlayerId, playerIdx is null");
            return null;
        }

        teamEntity entity = teamsMap.get(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new teamEntity(playerIdx);
            entity.initTeamsInfo();
            put(entity);
            return entity;
        }

        return entity;
    }

    /**
     * 获得玩家当前使用的小队所有petIdx
     *
     * @param playerIdx
     * @return list<petIdx>
     */
    public List<String> getCurUsedTeamPetIdxList(String playerIdx, TeamTypeEnum teamTypeEnum) {
        if (playerIdx == null) {
            return null;
        }

        Team playerNowUsedTeams = getPlayerNowUsedTeams(playerIdx, teamTypeEnum);
        if (playerNowUsedTeams == null) {
            return null;
        }
        Map<Integer, String> linkPetMap = playerNowUsedTeams.getLinkPetMap();
        if (linkPetMap == null) {
            return null;
        }

        ArrayList<String> resultList = new ArrayList<>();
        for (int i = 0; i <= TeamPosition.totalPosition; i++) {
            if (linkPetMap.containsKey(i)) {
                resultList.add(linkPetMap.get(i));
            }
        }

        return resultList;
    }

    public List<String> getTeamPetIdxList(String playerIdx, TeamNumEnum teamNumEnum) {
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            LogUtil.error("teamCache.getNowUsedTeamPetIdxList, playerIdx[" + playerIdx + "] teamEntity is null");
            return null;
        }

        return entity.getTeamPetIdxList(teamNumEnum);
    }

    /**
     * 获取当前使用的类型队伍
     *
     * @param playerIdx    需要获取的玩家
     * @param teamTypeEnum 阵容类型
     * @param subTypeEnum  战斗类型：有需要特殊处理的小队类型，例如勇气试炼有当前生命
     * @return
     */
    public List<BattlePetData> buildBattlePetData(String playerIdx, TeamTypeEnum teamTypeEnum, BattleSubTypeEnum subTypeEnum) {
        return buildBattlePetData(playerIdx, getNowUsedTeamNum(playerIdx, teamTypeEnum), subTypeEnum);
    }

    public List<BattlePetData> buildBattlePetData(String playerIdx, TeamNumEnum teamNum, BattleSubTypeEnum subTypeEnum) {
        if (StringHelper.isNull(playerIdx)) {
            return null;
        }

        List<String> petIdxList = getTeamPetIdxList(playerIdx, teamNum);
        if (CollectionUtils.isEmpty(petIdxList)) {
            return null;
        }
        return petCache.getInstance().getPetBattleData(playerIdx, petIdxList, subTypeEnum);
    }

    public List<PetDisplayInfo> buildDisplayPetData(String playerIdx, TeamTypeEnum teamTypeEnum) {
        if (StringHelper.isNull(playerIdx)) {
            return null;
        }
        List<String> petList = getCurUsedTeamPetIdxList(playerIdx, teamTypeEnum);
        if (petList == null) {
            return null;
        }
        return petCache.getInstance().displayPetMsgList(playerIdx, petList);
    }

    // 包含技能和宠物信息
    public Map<Integer, PetFormData> buildMinePetFormData(String playerIdx) {
        if (StringHelper.isNull(playerIdx)) {
            return null;
        }
        teamEntity entity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            return null;
        }
        List<Team> teams = entity.getTeamsByTeamType(TeamTypeEnum.TTE_Mine);
        if (teams == null) {
            return null;
        }
        Map<Integer, PetFormData> petFormDataMap = new HashMap<>();
        for (Team team : teams) {
            if (team.getLinkPetCount() <= 0) {
                continue;
            }
            List<String> petIdList = new ArrayList<>(team.getLinkPetMap().values());
            List<BattlePetData> battlePetList = petCache.getInstance().getPetBattleData(playerIdx, petIdList, BattleSubTypeEnum.BSTE_MineFight);
            if (battlePetList == null) {
                continue;
            }
            PetFormData.Builder builder = PetFormData.newBuilder();
            builder.addAllPetData(battlePetList);
            if (team.getLinkPetCount() > 0) {
                buildPlayerSkillData(playerIdx, team, builder);
            }
            petFormDataMap.put(team.getTeamNumValue(), builder.build());
        }
        return petFormDataMap;
    }

    public void buildPlayerSkillData(String playerIdx, Team team, Builder builder) {
        if (CollectionUtils.isEmpty(team.getLinkSkillMap())) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        builder.addAllPlayerSkill(player.getSkillBattleDict(team.getLinkSkillMap().values()));
    }

    public PetFormData buildMinePetFormData(String playerIdx, int formIndex) {
        Map<Integer, PetFormData> petFormDataMap = buildMinePetFormData(playerIdx);
        return petFormDataMap != null ? petFormDataMap.get(formIndex) : null;
    }

    /**
     * 获得玩家当前使用的小队使用的技能CfgId
     *
     * @param playerIdx
     * @return
     */
    public List<Integer> getCurUsedTeamSkillList(String playerIdx, TeamTypeEnum teamTypeEnum) {
        Team dbTeams = getPlayerNowUsedTeams(playerIdx, teamTypeEnum);
        if (dbTeams == null) {
            LogUtil.error("teamCache.getNowUsedTeamPetIdxList, playerIdx[" + playerIdx + "] team dbData is null");
            return null;
        }

        return new ArrayList<>(dbTeams.getLinkSkillMap().values());
    }

    public List<Integer> getPlayerTeamSkillList(String playerIdx, TeamNumEnum teamNum) {
        teamEntity entity = getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            return null;
        }
        return entity.getTeamSkillList(teamNum);
    }

    /**
     * 获得指定小队的战斗力
     *
     * @param playerIdx
     * @param teamNum
     * @return
     */
    public long getTeamFightAbility(String playerIdx, TeamNumEnum teamNum) {
        if (playerIdx == null || teamNum == null) {
            return 0L;
        }

        teamEntity team = getTeamEntityByPlayerId(playerIdx);
        if (team == null) {
            return 0L;
        }

        return team.getTeamFightAbility(teamNum);
    }

    /**
     * 清除玩家指定的小队和技能阵容
     *
     * @param playerIdx
     * @param teamNum
     */
    public void clearTeamPetAndSkill(String playerIdx, TeamNumEnum teamNum, boolean sendMsg) {
        //目前只有巡逻队小队支持清空
        if (TeamNumEnum.TNE_Patrol_1 != teamNum) {
            return;
        }

        teamEntity playerTeam = getTeamEntityByPlayerId(playerIdx);
        if (playerTeam == null) {
            return;
        }

        playerTeam.clearTeam(teamNum, sendMsg);

        //需要更新宠物状态
        List<String> needUpdateRemove = SyncExecuteFunction.executeFunction(playerTeam, p -> {
            Team dbTeam = playerTeam.getDBTeam(teamNum);
            if (dbTeam == null || (dbTeam.getLinkPetCount() <= 0 && dbTeam.getLinkSkillCount() <= 0)) {
                return null;
            }

            List<String> removePet = new ArrayList<>(dbTeam.getLinkPetMap().values());

            dbTeam.clearLinkPet();
            dbTeam.clearLinkSkill();
            playerTeam.sendRefreshTeamsMsg(teamNum);

            List<String> needUpdate = new ArrayList<>();
            for (String idx : removePet) {
                if (!playerTeam.petIdxInTeam(idx)) {
                    needUpdate.add(idx);
                }
            }

            return needUpdate;
        });

        if (needUpdateRemove == null || needUpdateRemove.isEmpty()) {
            return;
        }
        EventUtil.updatePetTeamState(playerIdx, needUpdateRemove, false, true);
    }

    public TeamNumEnum getCurUsedTeamNum(String playerIdx, TeamTypeEnum typeEnum) {
        teamEntity entity = getTeamEntityByPlayerId(playerIdx);
        if (entity == null) {
            return null;
        }
        return entity.getNowUsedTeamNum(typeEnum);
    }

    public void onPlayerLogIn(String playerIdx) {
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (teamEntity == null) {
            return;
        }
        teamEntity.updateCoupTeam();
        teamEntity.sendCoupTeamInfo();
    }
}
