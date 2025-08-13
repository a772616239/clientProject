package server.handler.adsBonus;

import cfg.AdsConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.LogService;
import platform.logs.entity.WatchAdsLog;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_GetWheelBonusTimes;
import protocol.PlayerInfo.SC_GetWheelBonusTimes;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_GetWheelBonusTimes_VALUE)
public class GetWheelBonusTimesHandler extends AbstractBaseHandler<CS_GetWheelBonusTimes> {
    @Override
    protected CS_GetWheelBonusTimes parse(byte[] bytes) throws Exception {
        return CS_GetWheelBonusTimes.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GetWheelBonusTimes req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }
        SC_GetWheelBonusTimes.Builder builder = SC_GetWheelBonusTimes.newBuilder();
        long starDisplayDeltaTime = AdsConfig.getById(GameConst.CONFIG_ID).getWheeladsdisplaytime() * TimeUtil.MS_IN_A_MIN;
        if (GlobalTick.getInstance().getCurrentTime() - player.getCreatetime().getTime() < starDisplayDeltaTime) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AdsBonus_InvalidPlayer));
            gsChn.send(MsgIdEnum.SC_GetWheelBonusTimes_VALUE, builder);
            return;
        }
        if (player.getDb_data().getAdsBonusData().getRemainWatchBonusTimes() <= 0) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AdsBonus_NoWatchAdsTimes));
            gsChn.send(MsgIdEnum.SC_GetWheelBonusTimes_VALUE, builder);
            return;
        }
        SyncExecuteFunction.executeConsumer(player, ply -> {
            int oldWatchTime = ply.getDb_data().getAdsBonusData().getRemainWatchBonusTimes();
            ply.getDb_data().getAdsBonusDataBuilder().setRemainWatchBonusTimes(--oldWatchTime);

            int oldWheelBonusTime = ply.getDb_data().getAdsBonusData().getFreeWheelBonusTimes();
            ply.getDb_data().getAdsBonusDataBuilder().setFreeWheelBonusTimes(++oldWheelBonusTime);
        });
        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_GetWheelBonusTimes_VALUE, builder);

        LogService.getInstance().submit(new WatchAdsLog(playerIdx, "WheelBonusAds"));
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Ads;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_GetWheelBonusTimes_VALUE, SC_GetWheelBonusTimes.newBuilder().setRetCode(retCode));
    }
}
