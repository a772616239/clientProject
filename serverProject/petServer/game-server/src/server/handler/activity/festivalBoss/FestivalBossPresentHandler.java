package server.handler.activity.festivalBoss;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.pet.dbCache.petCache;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Activity.CS_FestivalBossPresent;
import protocol.Activity.SC_FestivalBossPresent;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

import java.util.List;


@MsgId(msgId = MsgIdEnum.CS_FestivalBossPresent_VALUE)
public class FestivalBossPresentHandler extends AbstractBaseHandler<CS_FestivalBossPresent> {
    @Override
    protected CS_FestivalBossPresent parse(byte[] bytes) throws Exception {
        return CS_FestivalBossPresent.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_FestivalBossPresent req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_FestivalBossPresent.Builder msg = SC_FestivalBossPresent.newBuilder();

        RetCodeEnum codeEnum = presentBoss(playerIdx, req.getActivityId(), req.getPresentTimes());

        msg.setRetCode(GameUtil.buildRetCode(codeEnum));

        gsChn.send(MsgIdEnum.SC_FestivalBossPresent_VALUE, msg);
    }

    private RetCodeEnum presentBoss(String playerIdx, long activityId, int presentTimes) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        if (presentTimes < 1 || presentTimes > 10) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        Server.ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(activityId);
        if (!ActivityUtil.activityInOpen(activity)) {
            return RetCodeEnum.RCE_Activity_NotOpen;
        }
        LogUtil.info("player:{} present  festivalBoss ,activityId:{} ,presentTimes:{}", playerIdx, activity.getActivityId(), presentTimes);

        Server.ServerPlatformFestivalBoss cfg = activity.getFestivalBoss();

        Common.Consume presentConsume = cfg.getPresentConsume();
        if (presentConsume.getCount() <= 0) {
            return RetCodeEnum.RSE_ConfigNotExist;
        }
        presentConsume = presentConsume.toBuilder().setCount(presentConsume.getCount() * presentTimes).build();

        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FestivalBoss, "赠送节日boss");

        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, presentConsume, reason)) {
            return RetCodeEnum.RCE_Itembag_ItemNotEnought;
        }
        List<Common.Reward> mustReward = getPresentRewards(presentTimes, cfg);

        RewardManager.getInstance().doRewardByList(playerIdx, mustReward, reason, true);

        int exScoreRate = getExScoreRate(playerIdx, cfg);

        SyncExecuteFunction.executeConsumer(entity, obj -> {
            TargetSystemDB.DB_FestivalBoss db_festivalBoss = entity.getDb_Builder().getFestivalBossInfoMap().getOrDefault(activityId, TargetSystemDB.DB_FestivalBoss.getDefaultInstance());
            int scoreAdd = (int) (cfg.getPresentScore() * presentTimes * (exScoreRate / 1000.0 + 1));
            int cumuScore = db_festivalBoss.getCumeScore() + scoreAdd;
            TargetSystemDB.DB_FestivalBoss.Builder builder = db_festivalBoss.toBuilder().setCumeScore(cumuScore)
                    .setPresentTimes(db_festivalBoss.getPresentTimes() + presentTimes);
            entity.getDb_Builder().putFestivalBossInfo(activityId, builder.build());

            if (cumuScore >= cfg.getRankMinLimitScore()) {
                RankingManager.getInstance().updatePlayerRankingScore(playerIdx, Activity.EnumRankingType.ERT_FestivalBoss, RankingUtils.getActivityRankingName(activity), cumuScore, 0);
            }
        });

        entity.sendFestivalBossInfoUpdate(playerIdx, activityId);

        return RetCodeEnum.RCE_Success;
    }

    private List<Common.Reward> getPresentRewards(int presentTimes, Server.ServerPlatformFestivalBoss cfg) {
        List<Common.Reward> rewardList = RewardUtil.multiReward(cfg.getPresentRewardList(), presentTimes);
        List<Common.Reward> randomReward = RewardUtil.drawMustRandomReward(cfg.getPresentRandomRewardList(), presentTimes);
        rewardList.addAll(randomReward);
        RewardUtil.mergeReward(rewardList);
        return rewardList;
    }

    private int getExScoreRate(String playerIdx, Server.ServerPlatformFestivalBoss cfg) {
        if (petCache.getInstance().isPlayerHavePetByCfgId(playerIdx, cfg.getPetCfgId())) {
            return cfg.getExScoreRate();
        }
        return 0;

    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.FestivalBoss;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_FestivalBossPresent_VALUE, SC_FestivalBossPresent.newBuilder().setRetCode(retCode));
    }
}
