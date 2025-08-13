package server.handler.activity.festivalBoss;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimFestivalBossInfo;
import protocol.Activity.SC_ClaimFestivalBossInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB;
import util.GameUtil;

/**
 * 拉取节日boss信息
 */

@MsgId(msgId = MsgIdEnum.CS_ClaimFestivalBossInfo_VALUE)
public class ClaimFestivalBossInfoHandler extends AbstractBaseHandler<CS_ClaimFestivalBossInfo> {
    @Override
    protected CS_ClaimFestivalBossInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimFestivalBossInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimFestivalBossInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_ClaimFestivalBossInfo.Builder resultBuilder = SC_ClaimFestivalBossInfo.newBuilder();
        if (!ActivityUtil.activityNeedDis(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ClaimFestivalBossInfo_VALUE, resultBuilder);
            return;
        }

        if (activityCfg.getType() != ActivityTypeEnum.ATE_FestivalBoss) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimFestivalBossInfo_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimFestivalBossInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            buildClientMsg(resultBuilder, entity, activityCfg);
            gsChn.send(MsgIdEnum.SC_ClaimFestivalBossInfo_VALUE, resultBuilder);
        });
    }

    private void buildClientMsg(SC_ClaimFestivalBossInfo.Builder msg, targetsystemEntity entity, ServerActivity activityCfg) {
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        TargetSystemDB.DB_FestivalBoss dbFestivalBoss = entity.getDb_Builder().getFestivalBossInfoMap().get(activityCfg.getActivityId());
        if (dbFestivalBoss == null) {
            //这里打开后保存一份数据，避免开多个活动时候，自己的数据排行榜查询错误
            dbFestivalBoss = TargetSystemDB.DB_FestivalBoss.newBuilder().build();
            entity.getDb_Builder().putFestivalBossInfo(activityCfg.getActivityId(), dbFestivalBoss);
        }
        Server.ServerPlatformFestivalBoss festivalBossCfg = activityCfg.getFestivalBoss();
        msg.setFightMakeId(festivalBossCfg.getFightMakeId());
        msg.setPresentConsume(festivalBossCfg.getPresentConsume());
        msg.addAllPresentRewards(festivalBossCfg.getPresentRewardList());
        msg.addAllTreasure(festivalBossCfg.getTreasuresList());
        msg.setDailyChallengeTimes(festivalBossCfg.getDailyChallengeTimes());
        msg.setAlreadyChallengeTimes(dbFestivalBoss.getTodayChallengeTimes());
        msg.setCumuScore(dbFestivalBoss.getCumeScore());
        msg.setPresentTime(dbFestivalBoss.getPresentTimes());
        msg.setLastDamage(dbFestivalBoss.getLastDamage());
        msg.addAllDamageRewards(festivalBossCfg.getDamageRewardList());
        msg.addAllClaimedTreasureIds(dbFestivalBoss.getClaimedTreasureIdsList());
        msg.setPetCfgId(festivalBossCfg.getPetCfgId());
        msg.setExScoreRate(festivalBossCfg.getExScoreRate());
        msg.setShopCurrency(festivalBossCfg.getShopCurrency());
        msg.setShareLink(festivalBossCfg.getShareLink());
        msg.setLowestRankingScore(festivalBossCfg.getRankMinLimitScore());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.FestivalBoss;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimFestivalBossInfo_VALUE, Activity.SC_ClaimFestivalBossInfo.newBuilder().setRetCode(retCode));
    }
}
