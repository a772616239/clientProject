package model.battle.pool;

import model.battle.AbstractBattleController;
import model.battle.AbstractControllerPool;
import model.battle.pve.*;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.BattleTypeEnum;
import util.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author huhan
 * @date 2020/04/28
 */
public class PveControllerPool extends AbstractControllerPool {

    public static final Map<BattleSubTypeEnum, Supplier<AbstractBattleController>> SUPPLIER_MAP;

    static {
        Map<BattleSubTypeEnum, Supplier<AbstractBattleController>> tempMap = new HashMap<>();

        tempMap.put(BattleSubTypeEnum.BSTE_MainLineCheckPoint, MainLinePveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_EndlessSpire, EndlessSpirePveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_Patrol, PatrolPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_BreaveChallenge, BraveChallengePveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_ResourceCopy, ResourceCopyBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_PointCopy, PointCopyBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_ActivityBoss, ActivityBossPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_Arena, ArenaPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_MistForest, MistPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_BossTower, BossTowerPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_TheWar, TheWarPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_NewForeignInvasion, NewForeignInvasionPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_ChallengePlayer, ChallengePlayerPveBattleController::new);
//        tempMap.put(BattleSubTypeEnum.BSTE_ForeignInvasion, ForeignInvasionPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_GloryRoad, GloryRoadPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_Training, TrainingPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_ArenaMatchNormal, MatchArenaNormalPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_MatchArenaRanking, MatchArenaRankPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_magicthron, MagicThronPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_OfferReward, OfferRewardpveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_CrossArenaEvent, CrossArenaEventPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_FestivalBoss, FestivalBossPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_LTCpTeam, CpPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_CrazyDuel, CrazyDuelPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_EpisodeGeneral, EpisodeNormalPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_EpisodeSpecial, EpisodeSpecialPveBattleController::new);
        tempMap.put(BattleSubTypeEnum.BSTE_CrossArenaLeiTaiBoss, CrossArenaBossPveBattleController::new);
        SUPPLIER_MAP = Collections.unmodifiableMap(tempMap);
    }

    @Override
    public BattleTypeEnum getBattleType() {
        return BattleTypeEnum.BTE_PVE;
    }

    @Override
    public AbstractBattleController createController(BattleSubTypeEnum subType) {
        Supplier<AbstractBattleController> supplier = SUPPLIER_MAP.get(subType);
        if (supplier == null) {
            LogUtil.error("PveControllerPool.createController, battle sub type not have match supplier:" + subType);
            return null;
        }
        return supplier.get();
    }
}
