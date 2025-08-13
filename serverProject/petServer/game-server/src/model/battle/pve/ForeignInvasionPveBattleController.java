//package model.battle.pve;
//
//import cfg.*;
//import common.GameConst.EventType;
//import java.util.List;
//import java.util.Objects;
//import model.battle.AbstractPveBattleController;
//import model.battle.BattleUtil;
//import model.foreigninvasion.oldVersion.ForeignInvasionManager;
//import model.player.util.PlayerUtil;
//import model.reward.RewardManager;
//import model.reward.RewardUtil;
//import platform.logs.LogService;
//import platform.logs.ReasonManager;
//import platform.logs.entity.GamePlayLog;
//import protocol.Battle.BattleSubTypeEnum;
//import protocol.Battle.CS_BattleResult;
//import protocol.Battle.ExtendProperty;
//import protocol.Battle.SC_BattleResult;
//import protocol.BattleMono.FightParamTypeEnum;
//import protocol.Common.EnumFunction;
//import protocol.Common.Reward;
//import protocol.Common.RewardSourceEnum;
//import protocol.Gameplay.ForeignInvasionStatusEnum;
//import protocol.Gameplay.MonsterInfo;
//import protocol.PrepareWar.TeamTypeEnum;
//import protocol.RetCodeId.RetCodeEnum;
//import protocol.TargetSystem.TargetTypeEnum;
//import server.event.Event;
//import server.event.EventManager;
//import util.EventUtil;
//import util.GameUtil;
//
///**
// * @author huhan
// * @date 2020/04/26
// */
//public class ForeignInvasionPveBattleController extends AbstractPveBattleController {
//
//    /**
//     * 进入参数
//     */
//    public final String MONSTER_IDX = "monsterIdx";
//
//
//    @Override
//    public boolean enterParamsSettle(List<String> enterParams) {
//        if (GameUtil.collectionIsEmpty(enterParams)) {
//            return false;
//        }
//        putEnterParam(MONSTER_IDX, enterParams.get(0));
//        return true;
//    }
//
//    @Override
//    protected RetCodeEnum initFightInfo() {
//        if (PlayerUtil.queryPlayerLv(getPlayerIdx())
//                < FunctionOpenLvConfig.getOpenLv(EnumFunction.ForeignInvasion)) {
//            return RetCodeEnum.RCE_LvNotEnough;
//        }
//        ForeignInvasionStatusEnum status = ForeignInvasionManager.getInstance().getStatus();
//
//        if (status == ForeignInvasionStatusEnum.FISE_FirstStage) {
//            MonsterInfo monsterCfg = ForeignInvasionManager.getInstance().getMonsterInfo(getPlayerIdx(), getEnterParam(MONSTER_IDX));
//            if (monsterCfg == null) {
//                return RetCodeEnum.RCE_ForInv_MonsterIdxNotExist;
//            } else {
//                setFightMakeId(monsterCfg.getFightMakeId());
//                //标记战斗中小怪
//                ForeignInvasionManager.getInstance().markMonsterInBattle(getPlayerIdx(), monsterCfg);
//            }
//
//        } else if (status == ForeignInvasionStatusEnum.FISE_SecondStage) {
//            int bossCfgId = ForeignInvasionManager.getInstance().getBossCloneCfgId(getPlayerIdx(), getEnterParam(MONSTER_IDX));
//            ForInvBossCloneCfgObject byId = ForInvBossCloneCfg.getById(bossCfgId);
//            if (byId == null) {
//                return RetCodeEnum.RCE_ForInv_BossCloneNotExist;
//            } else {
//                ExtendProperty.Builder property = BattleUtil.builderMonsterExtendProperty(2, byId.getProperties());
//                if (property != null) {
//                    addExtendProp(property.build());
//                }
//                setFightMakeId(ForeignInvasionManager.getInstance().getBossFightMakeId(getPlayerIdx()));
//                addFightParams(FightParamTypeEnum.FPTE_PM_BossDamage);
//            }
//        }
//        return RetCodeEnum.RCE_Success;
//    }
//
//    @Override
//    protected void initSuccess() {
//        //目标：累积参加外敌入侵(战斗即算,不论胜负)
//        EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TEE_Foreign_CumuJoin, 1, 0);
//        LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.ForeignInvasion));
//    }
//
//    @Override
//    public int getPointId() {
//        return 0;
//    }
//
//    @Override
//    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
//        Event event = Event.valueOf(EventType.ET_FOREIGN_INVASION_BATTLE_SETTLE,
//                GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
//        event.pushParam(getPlayerIdx(), realResult, getEnterParam(MONSTER_IDX));
//        EventManager.getInstance().dispatchEvent(event);
//    }
//
//    @Override
//    public BattleSubTypeEnum getSubBattleType() {
//        return BattleSubTypeEnum.BSTE_ForeignInvasion;
//    }
//
//    @Override
//    public RewardSourceEnum getRewardSourceType() {
//        return RewardSourceEnum.RSE_ForeignInvasion;
//    }
//
//    @Override
//    public String getLogExInfo() {
//        return null;
//    }
//
//    @Override
//    public TeamTypeEnum getUseTeamType() {
//        return TeamTypeEnum.TTE_Common;
//    }
//
//    @Override
//    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
//        if (battleResult.getWinnerCamp() != getCamp()) {
//            return null;
//        }
//
//        List<Reward> rewards = null;
//        ForeignInvasionStatusEnum status = ForeignInvasionManager.getInstance().getStatus();
//        if (status == ForeignInvasionStatusEnum.FISE_FirstStage) {
//            String monsterIdx = getEnterParam(MONSTER_IDX);
//            MonsterInfo monster = ForeignInvasionManager.getInstance().getPlayerCurBattleMonster(getPlayerIdx());
//            if (monster == null || !Objects.equals(monster.getMonsterIdx(), monsterIdx)) {
//                return null;
//            }
//            rewards = getMonsterReward(monster.getMonsterType());
//        } else if (status == ForeignInvasionStatusEnum.FISE_SecondStage) {
//            rewards = getBossReward();
//        }
//
//        if (!GameUtil.collectionIsEmpty(rewards)) {
//            RewardManager.getInstance().doRewardByList(getPlayerIdx(), rewards,
//                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ForeignInvasion), false);
//        }
//
//
//        return rewards;
//    }
//
//    /**
//     * @param type 1 or 2
//     * @return
//     */
//    public List<Reward> getMonsterReward(int type) {
//        MonsterDifficultyObject diffCfg = MonsterDifficulty.getByPlayerIdx(getPlayerIdx());
//        if (diffCfg == null) {
//            return null;
//        }
//
//        if (type == 1) {
//            return RewardUtil.parseRewardIntArrayToRewardList(diffCfg.getForeigninvasioncommonmonsterreward());
//        } else if (type == 2) {
//            return RewardUtil.parseRewardIntArrayToRewardList(diffCfg.getForeigninvasionelitemonsterreward());
//        }
//        return null;
//    }
//
//    public List<Reward> getBossReward() {
//        MonsterDifficultyObject diffCfg = MonsterDifficulty.getByPlayerIdx(getPlayerIdx());
//        int bossCloneCfgId = ForeignInvasionManager.getInstance().getBossCloneCfgId(getPlayerIdx(), getEnterParam(MONSTER_IDX));
//        ForInvBossCloneCfgObject cloneCfg = ForInvBossCloneCfg.getById(bossCloneCfgId);
//        if (diffCfg == null || cloneCfg == null) {
//            return null;
//        }
//
//        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(diffCfg.getForeigninvasionbossreward());
//        return RewardUtil.multiRewardByPerThousand(rewards, cloneCfg.getBoosaddition());
//    }
//
//    @Override
//    public EnumFunction getFunctionEnum() {
//        return EnumFunction.ForeignInvasion;
//    }
//}
