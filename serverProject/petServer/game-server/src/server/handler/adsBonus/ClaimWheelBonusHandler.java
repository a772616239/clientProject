package server.handler.adsBonus;

import cfg.AdsConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.HttpRequestUtil;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.IndexReward;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_ClaimWheelBonus;
import protocol.PlayerInfo.SC_ClaimWheelBonus;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimWheelBonus_VALUE)
public class ClaimWheelBonusHandler extends AbstractBaseHandler<CS_ClaimWheelBonus> {
    @Override
    protected CS_ClaimWheelBonus parse(byte[] bytes) throws Exception {
        return CS_ClaimWheelBonus.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimWheelBonus req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_ClaimWheelBonus.Builder builder = SC_ClaimWheelBonus.newBuilder();
        long starDisplayDeltaTime = AdsConfig.getById(GameConst.CONFIG_ID).getWheeladsdisplaytime() * TimeUtil.MS_IN_A_MIN;
        if (GlobalTick.getInstance().getCurrentTime() - player.getCreatetime().getTime() < starDisplayDeltaTime) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AdsBonus_InvalidPlayer));
            gsChn.send(MsgIdEnum.SC_ClaimWheelBonus_VALUE, builder);
            return;
        }
        if (player.getDb_data().getAdsBonusData().getFreeWheelBonusTimes() <= 0) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AdsBonus_NoWatchAdsTimes));
            gsChn.send(MsgIdEnum.SC_ClaimWheelBonus_VALUE, builder);
            return;
        }

        int rewardId = AdsConfig.getById(GameConst.CONFIG_ID).getWheelbonuslist();
        List<IndexReward> rewardData = RewardUtil.drawMustRandomIndexAndRewardByRewardId(rewardId);
        if (rewardData == null || rewardData.isEmpty()) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MsgIdEnum.SC_ClaimWheelBonus_VALUE, builder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, ply -> {
            int oldWheelBonusTime = ply.getDb_data().getAdsBonusData().getFreeWheelBonusTimes();
            ply.getDb_data().getAdsBonusDataBuilder().setFreeWheelBonusTimes(--oldWheelBonusTime);
        });

        RewardManager.getInstance().doReward(playerIdx, rewardData.get(0).getReward(),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Ads_WheelBonus), false);

        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        builder.setRewardIndex(rewardData.get(0).getIndex());
        gsChn.send(MsgIdEnum.SC_ClaimWheelBonus_VALUE, builder);

        //广告统计
        HttpRequestUtil.platformAppsflyerWatchAdFinish(player);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Ads;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimWheelBonus_VALUE, SC_ClaimWheelBonus.newBuilder().setRetCode(retCode));
    }
}
