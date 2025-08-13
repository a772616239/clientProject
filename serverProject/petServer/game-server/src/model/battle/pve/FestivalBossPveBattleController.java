package model.battle.pve;

import cfg.ActivityBossReward;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.battle.AbstractPveBattleController;
import model.battle.BattleUtil;
import model.battle.PreBattleCheckRet;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.SC_BattleResult;
import protocol.BattleMono;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.TargetSystemDB;
import util.LogUtil;

import java.util.Collections;
import java.util.List;

/**
 * 节日boss
 */
public class FestivalBossPveBattleController extends AbstractPveBattleController {

    private long activityId;

    Server.ServerActivity activity ;

    @Override
    public boolean enterParamsSettle(List<String> enterParams) {
        String s = enterParams.get(0);
        try {
            activityId = Long.parseLong(s);
        } catch (Exception ex) {

            return false;
        }
        return true;
    }

    @Override
    protected RetCodeEnum initFightInfo() {
        activity = ActivityManager.getInstance().getActivityCfgById(activityId);

        if (!ActivityUtil.activityInOpen(activity)) {
            return RetCodeEnum.RCE_Activity_NotOpen;
        }

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(getPlayerIdx());
        if (target == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        TargetSystemDB.DB_FestivalBoss dbFestivalBoss = target.getDb_Builder().getFestivalBossInfoMap().getOrDefault(activityId, TargetSystemDB.DB_FestivalBoss.getDefaultInstance());

        if (dbFestivalBoss.getTodayChallengeTimes() >= activity.getFestivalBoss().getDailyChallengeTimes()) {
            return RetCodeEnum.RCE_ActivityBoss_UseUpTime;
        }

        addFightParams(FightParamTypeEnum.FPTE_PM_BossDamage);
        setFightMakeId(activity.getFestivalBoss().getFightMakeId());
        return RetCodeEnum.RCE_Success;
    }

    @Override
    protected void initSuccess() {
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
        long bossDamage = getBossDamage(realResult);
        SyncExecuteFunction.executeConsumer(entity, cache -> {
            TargetSystemDB.DB_FestivalBoss db_festivalBoss = entity.getDb_Builder().getFestivalBossInfoMap().getOrDefault(activityId, TargetSystemDB.DB_FestivalBoss.getDefaultInstance());
            TargetSystemDB.DB_FestivalBoss.Builder builder = db_festivalBoss.toBuilder().setTodayChallengeTimes(db_festivalBoss.getTodayChallengeTimes() + 1).setLastDamage(bossDamage);
            entity.getDb_Builder().putFestivalBossInfo(activityId,builder.build());
        });
        entity.sendFestivalBossInfoUpdate(getPlayerIdx(), activityId);
    }

    private long getBossDamage(CS_BattleResult realResult) {
        BattleMono.FightParamDict fightParamDict = realResult.getFightParamsList().stream()
                .filter(e -> e.getKey() == FightParamTypeEnum.FPTE_PM_BossDamage).findAny().orElse(null);
        long damage = 0;
        if (fightParamDict != null) {
            damage = fightParamDict.getValue();
        }
        return damage;
    }

    @Override
    public BattleSubTypeEnum getSubBattleType() {
        return BattleSubTypeEnum.BSTE_FestivalBoss;
    }

    @Override
    public RewardSourceEnum getRewardSourceType() {
        return RewardSourceEnum.RSE_FestivalBoss;
    }

    @Override
    public String getLogExInfo() {
        return null;
    }

    @Override
    public TeamTypeEnum getUseTeamType() {
        return TeamTypeEnum.TTE_FestivalBoss;
    }

    @Override
    public List<Reward> doBattleRewards(CS_BattleResult battleResult) {
        if (battleResult.getWinnerCamp() != getCamp()) {
            return Collections.emptyList();
        }
        if(activity ==null){
            return Collections.emptyList();
        }
        List<Activity.FestivalBossDamageReward> damageRewardList = activity.getFestivalBoss().getDamageRewardList();
        if (CollectionUtils.isEmpty(damageRewardList)){
            return Collections.emptyList();
        }

        long bossDamage = BattleUtil.getFightParamsValue(battleResult.getFightParamsList(),
                FightParamTypeEnum.FPTE_PM_BossDamage);
        Activity.FestivalBossDamageReward rewardCfg = damageRewardList.stream().filter(e -> e.getDamageStart() <= bossDamage && (e.getDamageEnd() == -1 || bossDamage < e.getDamageEnd())).findFirst().orElse(null);
        if (rewardCfg==null){
            return Collections.emptyList();
        }

        List<Reward> rewards =rewardCfg.getRewardsList();

        RewardManager.getInstance().doRewardByList(getPlayerIdx(), rewards,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_FestivalBoss, "活动Boss挑战"), false);
        return rewards;
    }

    @Override
    public PreBattleCheckRet preBaseCheck(CS_BattleResult clientResult) {
        PreBattleCheckRet preRet = super.preBaseCheck(clientResult);
        if (clientResult.getWinnerCamp() == 3) {
            LogUtil.info("PreBaseCheck ActivityBoss giveUp playerId={},battleId={},subBattleType={},fightMakeId={}", getPlayerIdx(), getBattleId(), getSubBattleType(), getFightMakeId());
            preRet.setNoNeedToCheck(true);
        }
        return preRet;
    }

    @Override
    public boolean checkFightPower() {
        return false;
    }
}
