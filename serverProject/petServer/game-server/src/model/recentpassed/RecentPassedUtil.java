package model.recentpassed;

import common.load.ServerConfig;
import model.arena.ArenaManager;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import model.petrune.dbCache.petruneCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.player.util.PlayerUtil;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Common.EnumFunction;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;
import protocol.PlayerInfo.NewTitle;
import protocol.PlayerInfo.NewTitleInfo;
import protocol.PrepareWar.TeamNumEnum;
import protocol.PrepareWar.TeamSkillPositionEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RecentPassedDB.DB_RecentPlayerInfo;
import protocol.RecentPassedOuterClass.PetDict;
import protocol.RecentPassedOuterClass.RecentPassed;
import protocol.RecentPassedOuterClass.SkillDict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author huhan
 * @date 2020.10.19
 */
public class RecentPassedUtil {
    private RecentPassedUtil() {
    }

    private static final Map<EnumFunction, TeamTypeEnum> FUNCTION_TEAM_TYPE_MAP;

    static {
        Map<EnumFunction, TeamTypeEnum> tempMap = new EnumMap<>(EnumFunction.class);

        tempMap.put(EnumFunction.MainLine, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.Endless, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.ResCopy, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.ForeignInvasion, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.MistForest, TeamTypeEnum.TTE_MistForest);
        tempMap.put(EnumFunction.Patrol, TeamTypeEnum.TTE_Patrol);
        tempMap.put(EnumFunction.CourageTrial, TeamTypeEnum.TTE_CourageTrial);
        tempMap.put(EnumFunction.MiningArea, TeamTypeEnum.TTE_Mine);
        tempMap.put(EnumFunction.Arena, TeamTypeEnum.TTE_Arena);
        tempMap.put(EnumFunction.BossTower, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.TheWar, TeamTypeEnum.TTE_TheWar);
        tempMap.put(EnumFunction.ArtifactRes, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.SoulRes, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.RuinsRes, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.RelicsRes, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.GoldenRes, TeamTypeEnum.TTE_Common);
        tempMap.put(EnumFunction.ActivityBoss, TeamTypeEnum.TTE_Boss);
        tempMap.put(EnumFunction.NewForeignInvasion, TeamTypeEnum.TTE_ForeignInvasion);
        tempMap.put(EnumFunction.Training, TeamTypeEnum.TTE_Training);
        tempMap.put(EnumFunction.MagicThron, TeamTypeEnum.TTE_MagicThron);
        tempMap.put(EnumFunction.OfferReward, TeamTypeEnum.TTE_OfferReward);
        tempMap.put(EnumFunction.QIECUO, TeamTypeEnum.TTE_QIECUO);
        tempMap.put(EnumFunction.LtCp, TeamTypeEnum.TTE_LtCP);
        tempMap.put(EnumFunction.LtCrazyDuel, TeamTypeEnum.TTE_CrazyDuel);

        FUNCTION_TEAM_TYPE_MAP = Collections.unmodifiableMap(tempMap);
    }

    public static TeamTypeEnum getFunctionMapTeamType(EnumFunction function) {
        if (function == null || function == EnumFunction.NullFuntion) {
            return null;
        }
        return FUNCTION_TEAM_TYPE_MAP.get(function);
    }


    /**
     * 最近通关允许使用的功能枚举
     */
    private static final Set<EnumFunction> RECENT_PASSED_ALLOW_FUNCTION;

    static {
        Set<EnumFunction> tempSet = new HashSet<>();
        tempSet.add(EnumFunction.MainLine);
        tempSet.add(EnumFunction.Endless);

        RECENT_PASSED_ALLOW_FUNCTION = Collections.unmodifiableSet(tempSet);
    }

    public static boolean allowFunction(EnumFunction function) {
        if (function == null || function == EnumFunction.NullFuntion) {
            return false;
        }
        return RECENT_PASSED_ALLOW_FUNCTION.contains(function);
    }


    /**
     * 玩家排行榜通关允许的功能枚举
     */

    private static final Set<EnumFunction> RANKING_RECENT_PASS_ALLOW_FUNCTION;

    static {
        Set<EnumFunction> tempSet = new HashSet<>();
        tempSet.add(EnumFunction.MainLine);
        tempSet.add(EnumFunction.Endless);

        RANKING_RECENT_PASS_ALLOW_FUNCTION = Collections.unmodifiableSet(tempSet);
    }

    public static boolean playerRecentAllowFunction(EnumFunction function) {
        if (function == null || function == EnumFunction.NullFuntion) {
            return false;
        }
        return RANKING_RECENT_PASS_ALLOW_FUNCTION.contains(function);
    }

    /**
     * @param function
     * @param param    使用对应模块的唯一主键
     * @return
     */
    public static String buildIdx(EnumFunction function, int param) {
        if (!allowFunction(function)) {
            return null;
        }

        long baseId = ((long) function.getNumber()) << 32;
        return String.valueOf(baseId + param);
    }

    /**
     * 主线闯关最近通关信息
     *
     * @param playerIdx
     * @return
     */
    public static DB_RecentPlayerInfo.Builder buildPlayerRecentInfo(String playerIdx, EnumFunction function) {
        if (StringUtils.isBlank(playerIdx) || function == null || function == EnumFunction.NullFuntion) {
            return null;
        }

        Team dbTeam = teamCache.getInstance().getPlayerNowUsedTeams(playerIdx, getFunctionMapTeamType(function));
        if (dbTeam == null) {
            return null;
        }

        DB_RecentPlayerInfo.Builder recentPassedBuilder = DB_RecentPlayerInfo.newBuilder();
        recentPassedBuilder.setPlayerIdx(playerIdx);
        recentPassedBuilder.setVipLv(PlayerUtil.queryPlayerVipLv(playerIdx));
        recentPassedBuilder.setTotalAbility(petCache.getInstance().totalAbility(playerIdx));
        recentPassedBuilder.setTitleId(PlayerUtil.queryPlayerTitleId(playerIdx));

        List<PetDict> petDicts = buildPetDictList(playerIdx, dbTeam.getLinkPetMap());
        if (CollectionUtils.isNotEmpty(petDicts)) {
            recentPassedBuilder.addAllPets(petDicts);
        }

        List<SkillDict> skillDicts = buildSkillDictList(playerIdx, dbTeam.getLinkSkillMap());
        if (CollectionUtils.isNotEmpty(skillDicts)) {
            recentPassedBuilder.addAllSkills(skillDicts);
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player != null) {
            //神器
            recentPassedBuilder.addAllArtifact(player.getSimpleArtifact());
            Map<Integer, Integer> artifactAdditionMap = player.getDb_data().getGlobalAddition().getArtifactAdditionMap();
            recentPassedBuilder.addAllArtifactAdditionKeys(artifactAdditionMap.keySet());
            recentPassedBuilder.addAllArtifactAdditionValues(artifactAdditionMap.values());

            //新称号
            NewTitle titleInfo = player.getDb_data().getNewTitle();
            for (NewTitleInfo titleData : titleInfo.getInfoList()) {
                recentPassedBuilder.addNewTitleId(titleData.getCfgId());
            }
            recentPassedBuilder.setCurEquipNewTitleId(titleInfo.getCurEquip());
        }
        return recentPassedBuilder;
    }

    public static RecentPassed buildRecentPassedInfo(String playerIdx, TeamTypeEnum teamType) {
        return buildRecentPassedInfo(playerIdx, teamCache.getInstance().getCurUsedTeamNum(playerIdx, teamType));
    }

    public static RecentPassed buildRecentPassedInfo(String playerIdx, TeamNumEnum teamNum) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        teamEntity team = teamCache.getInstance().getTeamEntityByPlayerId(playerIdx);
        if (player == null || team == null) {
            return null;
        }

        RecentPassed.Builder result = RecentPassed.newBuilder();
        result.setPlayerIdx(playerIdx);
        result.setPlayerName(player.getName());
        result.setPlayerLv(player.getLevel());
        result.setAvatarId(player.getAvatar());
        result.setSex(player.getSex());

        Team dbTeam = team.getDBTeam(teamNum);
        if (dbTeam != null) {
            List<PetDict> petDicts = buildPetDictList(playerIdx, dbTeam.getLinkPetMap());
            if (CollectionUtils.isNotEmpty(petDicts)) {
                result.addAllPets(petDicts);
            }

            List<SkillDict> skillDicts = buildSkillDictList(playerIdx, dbTeam.getLinkSkillMap());
            if (CollectionUtils.isNotEmpty(skillDicts)) {
                result.addAllSkills(skillDicts);
            }
        }

        result.setVipLv(player.getVip());
        result.setTotalAbility(petCache.getInstance().totalAbility(playerIdx));
        result.setAvatarBorder(player.getAvatar());
        if (result.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            result.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(player.getIdx()));
        }
        result.setTitleId(player.getTitleId());

        //神器
        result.addAllArtifact(player.getSimpleArtifact());
        Map<Integer, Integer> artifactAdditionMap = player.getDb_data().getGlobalAddition().getArtifactAdditionMap();
        result.addAllArtifactAdditionKeys(artifactAdditionMap.keySet());
        result.addAllArtifactAdditionValues(artifactAdditionMap.values());

        NewTitle newTitleInfo = player.getDb_data().getNewTitle();
        for (NewTitleInfo titleData : newTitleInfo.getInfoList()) {
            result.addNewTitleId(titleData.getCfgId());
        }
        result.setCurEquipNewTitleId(newTitleInfo.getCurEquip());

        result.setShortId(player.getShortid());
        result.setServerIndex(ServerConfig.getInstance().getServer());
        return result.build();
    }

    /**
     * @param playerIdx
     * @param petIdxMap <TeamPetPositionEnum,PetIdx>
     * @return
     */
    public static List<PetDict> buildPetDictList(String playerIdx, Map<Integer, String> petIdxMap) {
        if (StringUtils.isEmpty(playerIdx) || MapUtils.isEmpty(petIdxMap)) {
            return null;
        }
        List<BattlePetData> petBattleDataList
                = petCache.getInstance().getPetBattleData(playerIdx, new ArrayList<>(petIdxMap.values()), BattleSubTypeEnum.BSTE_Null);
        if (CollectionUtils.isEmpty(petBattleDataList)) {
            return null;
        }
        Map<String, BattlePetData> petBattleDataMap
                = petBattleDataList.stream().collect(Collectors.toMap(BattlePetData::getPetId, e -> e));

        return petIdxMap.entrySet().stream()
                .map(entry -> {
                    BattlePetData battlePetData = petBattleDataMap.get(entry.getValue());
                    if (battlePetData == null) {
                        return null;
                    }
                    PetDict.Builder subBuilder = PetDict.newBuilder();
                    subBuilder.setPositionValue(entry.getKey());
                    subBuilder.setPet(battlePetData);

                    //宠物符文
                    List<Rune> petRune = petruneCache.getInstance().getPetRune(playerIdx, entry.getValue());
                    if (CollectionUtils.isNotEmpty(petRune)) {
                        for (Rune rune : petRune) {
                            subBuilder.addRunes(rune.toBuilder().clone());
                        }
                    }

                    //宠物宝石
                    Pet pet = petCache.getInstance().getPetById(playerIdx, entry.getValue());
                    if (pet != null) {
                        Gem gem = petgemCache.getInstance().getGemByGemIdx(playerIdx, pet.getGemId());
                        if (gem != null) {
                            subBuilder.setGem(gem);
                        }
                    }

                    return subBuilder.build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    public static List<SkillDict> buildSkillDictList(String playerIdx, Map<Integer, Integer> skillMap) {
        if (StringUtils.isEmpty(playerIdx) || MapUtils.isEmpty(skillMap)) {
            return null;
        }
        return skillMap.entrySet().stream()
                .map(entry -> {
                    SkillDict.Builder skillBuilder = SkillDict.newBuilder();
                    skillBuilder.setPosition(TeamSkillPositionEnum.forNumber(entry.getKey()));
                    skillBuilder.setSkillCfgId(entry.getValue());
                    skillBuilder.setSkillLevel(PlayerUtil.queryPlayerSkillLv(playerIdx, entry.getValue()));
                    return skillBuilder.build();
                })
                .collect(Collectors.toList());
    }
}
