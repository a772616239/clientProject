package model.battle.pve;

import cfg.ActivityBossReward;
import common.GameConst;
import common.GameConst.EventType;
import common.HttpRequestUtil;
import common.entity.HttpRankingResponse;
import common.entity.RankingQueryRequest;
import common.entity.RankingQuerySingleResult;
import common.entity.RankingUpdateRequest;
import common.load.ServerConfig;
import model.activityboss.ActivityBossManager;
import model.activityboss.entity.ActivityBossEnterResult;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.battle.PreBattleCheckRet;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PetBuffData;
import protocol.Battle.SC_BattleResult;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

import java.util.List;

/**
 * @author huhan
 * @date 2020/04/26
 */
public class ActivityBossPveBattleController extends AbstractPveBattleController {

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        ActivityBossEnterResult result = ActivityBossManager.getInstance().getFightMakeId(getPlayerIdx());
        if (result.isSuccess()) {
            // buff id默认不为0
            if (result.getEnemyBuffId() != 0) {
                ExtendProperty.Builder extendProperty = ExtendProperty.newBuilder();
                // debuff或者怪物增强阵营为2
                extendProperty.setCamp(2);
                // 获取buffId，默认设置buff层数为1
                extendProperty.addBuffData(PetBuffData.newBuilder().setBuffCfgId(result.getEnemyBuffId()).setBuffCount(1));
                addExtendProp(extendProperty.build());
            }
            addFightParams(FightParamTypeEnum.FPTE_PM_BossDamage);
            setFightMakeId(result.getFightMakeId());
            return RetCodeEnum.RCE_Success;
        } else {
            return result.getCode();
        }
    }

    @Override
    protected void initSuccess() {
        LogService.getInstance().submit(new GamePlayLog(getPlayerIdx(), EnumFunction.ActivityBoss));
    }

    @Override
    public int getPointId() {
        return 0;
    }

    @Override
    protected void tailSettle(CS_BattleResult realResult, List<Reward> rewardListList, SC_BattleResult.Builder resultBuilder) {
        if (realResult.getWinnerCamp() != getCamp()) {
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(getPlayerIdx());
        if (entity == null) {
            return;
        }
       /* BattleMono.FightParamDict fightParamDict = realResult.getFightParamsList().stream()
                .filter(e -> e.getKey() == FightParamTypeEnum.FPTE_PM_BossDamage).findAny().orElse(null);
        if (fightParamDict != null) {
            long value = fightParamDict.getValue();
            //更新boss伤害
            refreshRanking(value);
        }*/
        // 更新挑战次数
        Event event = Event.valueOf(EventType.ET_UpdateBossActivityTime, GameUtil.getDefaultEventSource(), entity);
        event.pushParam(1);
        EventManager.getInstance().dispatchEvent(event);
    }


    private synchronized void refreshRanking(long damage) {
        try {
            playerEntity player = playerCache.getByIdx(getPlayerIdx());
            if (player == null) {
                return;
            }
            RankingQueryRequest query = new RankingQueryRequest();
            query.setRank(GameConst.RankingName.RN_ActivityBoss_Damage);
            query.setServerIndex(ServerConfig.getInstance().getServer());
            query.setAssignPrimaryKey(getPlayerIdx());
            query.setSize(ServerConfig.getInstance().getMaxRankingSize());
            query.setPage(1);
            HttpRankingResponse response = HttpRequestUtil.queryRanking(query);
            if (response == null) {
                return;
            }
            RankingQuerySingleResult queryResult = response.getData().getAssignInfo();
            long newDamage;
            if (queryResult != null) {
                //
                long primaryScore = queryResult.getPrimaryScore();
                newDamage = primaryScore + damage;
            } else {
                //第一次伤害
                newDamage = damage;
            }
            RankingUpdateRequest rankingUpdateRequest = new RankingUpdateRequest(GameConst.RankingName.RN_ActivityBoss_Damage);
            rankingUpdateRequest.addScore(getPlayerIdx(), newDamage);
            HttpRequestUtil.asyncUpdateRanking(rankingUpdateRequest);

        } catch (Exception e) {
            LogUtil.error("update activityBossDamage Ranking error");
            LogUtil.printStackTrace(e);
        }
    }


    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_ActivityBoss;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_ActivityBoss;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_Boss;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (battleResult == null || battleResult.getWinnerCamp() != getCamp()) {
            return null;
        }

        long bossDamage = BattleUtil.getFightParamsValue(battleResult.getFightParamsList(),
                FightParamTypeEnum.FPTE_PM_BossDamage);
        List<Reward> rewards = ActivityBossReward.getInstance().getRewardListByDamage(PlayerUtil.queryPlayerLv(getPlayerIdx()), bossDamage);

        RewardManager.getInstance().doRewardByList(getPlayerIdx(), rewards,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ActivityBoss), false);

        return rewards;
    }

    @Override
    public PreBattleCheckRet preBaseCheck(CS_BattleResult clientResult) {
        PreBattleCheckRet preRet = super.preBaseCheck(clientResult);
        if (clientResult.getWinnerCamp() == 3) {
            LogUtil.info("PreBaseCheck ActivityBoss giveUp playerId={},battleId={},subBattleType={},fightMakeId={}",getPlayerIdx(),getBattleId(),getSubBattleType(),getFightMakeId());
            preRet.setNoNeedToCheck(true);
        }
        return preRet;
    }

    @Override
    public boolean checkFightPower() {
        return false;
    }
}
