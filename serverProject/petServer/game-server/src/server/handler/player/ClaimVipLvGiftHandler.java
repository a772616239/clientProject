package server.handler.player;

import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerInfo.CS_ClaimVipLvGift;
import protocol.PlayerInfo.SC_ClaimVipLvGift;
import protocol.PlayerInfo.SC_ClaimVipLvGift.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimVipLvGift_VALUE)
public class ClaimVipLvGiftHandler extends AbstractBaseHandler<CS_ClaimVipLvGift> {
    @Override
    protected CS_ClaimVipLvGift parse(byte[] bytes) throws Exception {
        return CS_ClaimVipLvGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimVipLvGift req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        Builder resultBuilder = SC_ClaimVipLvGift.newBuilder();
        LogUtil.info("receive player:{} claim vip gift ,req:{}", playerIdx, req);

        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimVipLvGift_VALUE, resultBuilder);
            return;
        }
        VIPConfigObject vipCfg = VIPConfig.getById(req.getVipLv());
        if (vipCfg == null||ArrayUtils.isEmpty(vipCfg.getLvgiftbag())) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimVipLvGift_VALUE, resultBuilder);
            return;
        }

        DB_PlayerData.Builder db_data = player.getDb_data();
        if (db_data == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] db data is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimVipLvGift_VALUE, resultBuilder);
            return;
        }

        if (db_data.getClaimedVipGiftList().contains(req.getVipLv())) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_VipGiftClaimed));
            gsChn.send(MsgIdEnum.SC_ClaimVipLvGift_VALUE, resultBuilder);
            return;
        }


        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_VIPLvUp, String.valueOf(req.getVipLv()));

        Common.Consume realConsume = ConsumeUtil.parseConsume(vipCfg.getBuylvlgiftprice()[0]);

        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, realConsume, reason)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_ClaimVipLvGift_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {

            player.getDb_data().addClaimedVipGift(req.getVipLv());

            RewardManager.getInstance().doRewardByList(playerIdx, RewardUtil.parseRewardIntArrayToRewardList(vipCfg.getLvgiftbag()), reason, true);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimVipLvGift_VALUE, resultBuilder);
        });

        LogUtil.info(" player:{} claim vip gift success,req:{}", playerIdx, req);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Vip;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimVipLvGift_VALUE, SC_ClaimVipLvGift.newBuilder().setRetCode(retCode));
    }
}
