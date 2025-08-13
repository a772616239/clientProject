package model.targetsystem;

import cfg.FunctionOpenLvConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.ResourceCopy.ResourceCopyTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020.10.07
 */
public class TargetSystemUtil {

    public static TargetTypeEnum getTargetTypeByResCopyType(ResourceCopyTypeEnum copyType) {
        if (copyType == null || copyType == ResourceCopyTypeEnum.RCTE_Null) {
            return TargetTypeEnum.TTE_NULL;
        }
        return getTargetTypeByResCopyTypeNum(copyType.getNumber());
    }

    public static TargetTypeEnum getTargetTypeByResCopyTypeNum(int copyTypeNum) {
        if (copyTypeNum == ResourceCopyTypeEnum.RCTE_Gold_VALUE) {
            return TargetTypeEnum.TTE_CumuPassGoldResCopy;
        } else if (copyTypeNum == ResourceCopyTypeEnum.RCTE_Crystal_VALUE) {
            return TargetTypeEnum.TTE_CumuPassCrystalResCopy;
        } else if (copyTypeNum == ResourceCopyTypeEnum.RCTE_SoulStone_VALUE) {
            return TargetTypeEnum.TTE_CumuPassSoulResCopy;
        } else if (copyTypeNum == ResourceCopyTypeEnum.RCTE_Rune_VALUE) {
            return TargetTypeEnum.TTE_CumuPassRuneResCopy;
        } else if (copyTypeNum == ResourceCopyTypeEnum.RCTE_Awaken_VALUE) {
            return TargetTypeEnum.TTE_CumuPassAwakeResCopy;
        }
        return TargetTypeEnum.TTE_NULL;
    }

    /**
     * 没有对应系统解锁的枚举使用EnumFunction.NullFuntion(默认解锁)
     */
    private static final Map<TargetTypeEnum, EnumFunction> TARGET_TYPE_FUNCTION_MAP;

    static {
        Map<TargetTypeEnum, EnumFunction> tempMap = new HashMap<>();
        tempMap.put(TargetTypeEnum.TTE_NULL, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_FinishedDailyMission, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_CumuSettleOnHookReward, EnumFunction.MainLine);
        tempMap.put(TargetTypeEnum.TTE_CumuMineFightOrOccpy, EnumFunction.MiningArea);
        tempMap.put(TargetTypeEnum.TTE_CumuLevelUpPet, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_CumuAwakePet, EnumFunction.PetEvolution);
        tempMap.put(TargetTypeEnum.TTE_CumuMistBattleVictory, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_CumuFinishedPetEntrust, EnumFunction.PetDelegate);
        tempMap.put(TargetTypeEnum.TTE_CumuJoinResCopy, EnumFunction.ResCopy);
        tempMap.put(TargetTypeEnum.TTE_CumuJoinPatrol, EnumFunction.Patrol);
        tempMap.put(TargetTypeEnum.TTE_CumuJoinCourageBattle, EnumFunction.CourageTrial);
        tempMap.put(TargetTypeEnum.TTE_PlayerLvReach, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_CumuPetLevelReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_RuneIntensifyLv, EnumFunction.MainEquipment);
        tempMap.put(TargetTypeEnum.TTE_CumuGainPet, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_CumuGainRune, EnumFunction.MainEquipment);
        tempMap.put(TargetTypeEnum.TTE_PassMianLineChapter, EnumFunction.MainLine);
        tempMap.put(TargetTypeEnum.TTE_PassSpireLv, EnumFunction.Endless);
        tempMap.put(TargetTypeEnum.TTE_MistLevel, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_CumuOpenMistBox, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_CumuMistKillPlayer, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_MistContinuousKillPlayer, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_CumuMistKillBoss, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_CumuPassGoldResCopy, EnumFunction.GoldenRes);
        tempMap.put(TargetTypeEnum.TTE_CumuPassCrystalResCopy, EnumFunction.RelicsRes);
        tempMap.put(TargetTypeEnum.TTE_CumuPassSoulResCopy, EnumFunction.SoulRes);
        tempMap.put(TargetTypeEnum.TTE_CumuPassRuneResCopy, EnumFunction.ArtifactRes);
        tempMap.put(TargetTypeEnum.TTE_CumuCommonDrawCard, EnumFunction.DrawCard);
        tempMap.put(TargetTypeEnum.TTE_CumuHighDrwaCard, EnumFunction.DrawCard);
        tempMap.put(TargetTypeEnum.TTE_CumuCallAncient, EnumFunction.DrawCard_AncientCall);
        tempMap.put(TargetTypeEnum.TTE_CumuPetTransfer, EnumFunction.AncientCall);
        tempMap.put(TargetTypeEnum.TTE_CumuUpPetStar, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_CumuCompoundPet, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_CumuManualRefreshBlack, EnumFunction.BlackMarket);
        tempMap.put(TargetTypeEnum.TTE_CumuPassCouragePoint, EnumFunction.CourageTrial);
        tempMap.put(TargetTypeEnum.TTE_CumuOccupyMine, EnumFunction.MiningArea);
        tempMap.put(TargetTypeEnum.TTE_CumuKillPatrolBoss, EnumFunction.Patrol);
        tempMap.put(TargetTypeEnum.TTE_CumuKillForInvMonster, EnumFunction.ForeignInvasion);
        tempMap.put(TargetTypeEnum.TTE_CumuKillForInvBoss, EnumFunction.ForeignInvasion);
        tempMap.put(TargetTypeEnum.TTE_CumuConsumeDiamond, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_CumuExchangeGold, EnumFunction.GoldExchange);
        tempMap.put(TargetTypeEnum.TTE_CumuRuneIntensify, EnumFunction.MainEquipment);
        tempMap.put(TargetTypeEnum.TTE_CumuBuyGoods, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_CumuPetAwakeRech, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_CumuLogin, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_CumuPassAwakeResCopy, EnumFunction.RuinsRes);
        tempMap.put(TargetTypeEnum.TTE_CumuSignIn, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_PlayerFriendReach, EnumFunction.Friend);
        tempMap.put(TargetTypeEnum.TTE_PetDischarge, EnumFunction.PetDischarge);
        tempMap.put(TargetTypeEnum.TTE_PetRestore, EnumFunction.PetDischarge);
        tempMap.put(TargetTypeEnum.TTE_Pet_CumuGainSpecifyPet, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_Pet_CumuPetStarReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_Pet_SpecifyPetLvUpReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_Pet_SpecifyPetStarUpReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_Pet_SpecifyPetAwakeUpReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TTE_Mist_CumuKillMonster, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_Mist_CumuEnterMist, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_MistSeasonTask_GainBagCount, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_MistSeasonTask_OpenBoxCount, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_MistSeasonTask_UseItemCount, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_MistSeasonTask_KillBossCount, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TTE_Mist_CumuFormATeam, EnumFunction.MistForest);
        tempMap.put(TargetTypeEnum.TEE_Patrol_OpenBox, EnumFunction.Patrol);
        tempMap.put(TargetTypeEnum.TEE_Patrol_SpecialGreedFinish, EnumFunction.Patrol);
        tempMap.put(TargetTypeEnum.TEE_Foreign_CumuJoin, EnumFunction.ForeignInvasion);
        tempMap.put(TargetTypeEnum.TEE_Foreign_CumuBossDamage, EnumFunction.ForeignInvasion);
        tempMap.put(TargetTypeEnum.TEE_PointCopy_CumuJoin, EnumFunction.PointCopy);
        tempMap.put(TargetTypeEnum.TEE_PointCopy_CumuPoint, EnumFunction.PointCopy);
        tempMap.put(TargetTypeEnum.TEE_Player_CumuRechargeCoupon, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TEE_Mine_FinishedFriendHelp, EnumFunction.MiningArea);
        tempMap.put(TargetTypeEnum.TEE_Arena_CumuBattle, EnumFunction.Arena);
        tempMap.put(TargetTypeEnum.TEE_Arena_CumuGainScore, EnumFunction.Arena);
        tempMap.put(TargetTypeEnum.TEE_Arena_DanReach, EnumFunction.Arena);
        tempMap.put(TargetTypeEnum.TEE_GrowthFund_Lvl, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TEE_OnlineTime, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TEE_Function_Unlock, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TEE_Feats_CumuGain, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TEE_Gem_CumuGain, EnumFunction.MainPetGem);
        tempMap.put(TargetTypeEnum.TEE_Gem_LvReach, EnumFunction.MainPetGem);
        tempMap.put(TargetTypeEnum.TEE_Artifact_Unlock, EnumFunction.MainArtifact);
        tempMap.put(TargetTypeEnum.TEE_Artifact_LvReach, EnumFunction.MainArtifact);
        tempMap.put(TargetTypeEnum.TEE_Artifact_StarReach, EnumFunction.MainArtifact);
        tempMap.put(TargetTypeEnum.TEE_PetAwake_AttackReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TEE_PetAwake_DefenseReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TEE_PetAwake_HpReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TEE_PetAwake_TotalReach, EnumFunction.PetSystem);
        tempMap.put(TargetTypeEnum.TEE_Mine_CumuGainVipExp, EnumFunction.MiningArea);
        tempMap.put(TargetTypeEnum.TEE_Mine_CumuGainDiamond, EnumFunction.MiningArea);
        tempMap.put(TargetTypeEnum.TEE_Mine_CumuGainAncientEssence, EnumFunction.MiningArea);
        tempMap.put(TargetTypeEnum.TEE_BossTower_UnlockLvReach, EnumFunction.BossTower);
        tempMap.put(TargetTypeEnum.TEE_BossTower_DefeatBoss, EnumFunction.BossTower);
        tempMap.put(TargetTypeEnum.TEE_ApocalypseBlessing_CumuGainScore, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TEE_Player_RechargeCoupon, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_CumuConsumeCoupon, EnumFunction.NullFuntion);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuSubmitDp, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuCollectTheWarGold, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuCollectDP, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuCollectTech, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuComposeTech, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuGainTech, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_Common, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_WarGold, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_HolyWater, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid_DpResource, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuStationTroops_BossGrid, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_Common, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_WarGold, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_HolyWater, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_DpResource, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_FootHoldGrid_BossGrid, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_WarGold, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_HolyWater, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_DpResource, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_BossGrid, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_KillMonsterCount, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_JobTileLvReach, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuStationTroops_FootHoldGrid, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_TheWar_CumuOccupy_Enemy_FootHoldGrid_Common, EnumFunction.TheWar);
        tempMap.put(TargetTypeEnum.TTE_Patrol_FightAboveGreed, EnumFunction.Patrol);
        tempMap.put(TargetTypeEnum.TTE_Patrol_BuySaleManGoods, EnumFunction.Patrol);
        tempMap.put(TargetTypeEnum.TEE_MainLine_QuickOnHook, EnumFunction.MainLine);
        tempMap.put(TargetTypeEnum.TEE_BossTower_CumuJoin, EnumFunction.BossTower);
        tempMap.put(TargetTypeEnum.TTE_TrainScore, EnumFunction.Training);

        TARGET_TYPE_FUNCTION_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * 玩家是否解锁指定任务目标类型
     *
     * @param playerIdx
     * @param targetType
     * @return
     */
    public static boolean targetMissionIsUnlock(String playerIdx, TargetTypeEnum targetType) {
        if (targetType == null) {
            return false;
        }
        EnumFunction function = TARGET_TYPE_FUNCTION_MAP.get(targetType);
        if (function == null) {
            LogUtil.error("TargetSystemUtil.targetMissionUnlockLv, target system is not have match function, targetType:" + targetType);
            return false;
        }
        return PlayerUtil.queryFunctionUnlock(playerIdx, function);
    }
}
