package server.handler.adsBonus;

import cfg.AdsConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.HttpRequestUtil;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.WatchAdsLog;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_ClaimFreeAdsBonus;
import protocol.PlayerInfo.SC_ClaimFreeAdsBonus;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimFreeAdsBonus_VALUE)
public class ClaimFreeAdsBonusHandler extends AbstractBaseHandler<CS_ClaimFreeAdsBonus> {
    @Override
    protected CS_ClaimFreeAdsBonus parse(byte[] bytes) throws Exception {
        return CS_ClaimFreeAdsBonus.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimFreeAdsBonus req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        if (!player.canClaim(ActivityUtil.LocalActivityId.Ads, 0, 1, AdsConfig.getById(GameConst.CONFIG_ID).getFreeadsgifttimes())) {
            return;
        }
        SC_ClaimFreeAdsBonus.Builder builder = SC_ClaimFreeAdsBonus.newBuilder();
        long starDisplayDeltaTime = AdsConfig.getById(GameConst.CONFIG_ID).getFreeadsdisplaytime() * TimeUtil.MS_IN_A_MIN;
        if (GlobalTick.getInstance().getCurrentTime() - player.getCreatetime().getTime() < starDisplayDeltaTime) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AdsBonus_InvalidPlayer));
            gsChn.send(MsgIdEnum.SC_ClaimFreeAdsBonus_VALUE, builder);
            return;
        }
        if (player.getDb_data().getAdsBonusData().getFreeGiftTimes() <= 0) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AdsBonus_NoBonusTimes));
            gsChn.send(MsgIdEnum.SC_ClaimFreeAdsBonus_VALUE, builder);
            return;
        }
        int rewardId = AdsConfig.getById(GameConst.CONFIG_ID).getFreeadsgiftreward();
        if (rewardId <= 0) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MsgIdEnum.SC_ClaimFreeAdsBonus_VALUE, builder);
            return;
        }
        RewardManager.getInstance().doRewardByRewardId(playerIdx, rewardId,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Ads_FreeAdsBonus), true);
        SyncExecuteFunction.executeConsumer(player, ply -> {
            int oldTimes = ply.getDb_data().getAdsBonusData().getFreeGiftTimes();
            ply.getDb_data().getAdsBonusDataBuilder().setFreeGiftTimes(--oldTimes);
        });
        player.increasePlayerRewardRecord(ActivityUtil.LocalActivityId.Ads, 0);

        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimFreeAdsBonus_VALUE, builder);

        LogService.getInstance().submit(new WatchAdsLog(playerIdx, "FreeBonusAds"));
        HttpRequestUtil.platformAppsflyerWatchAdFinish(player);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Ads;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimFreeAdsBonus_VALUE, SC_ClaimFreeAdsBonus.newBuilder().setRetCode(retCode));
    }
}
