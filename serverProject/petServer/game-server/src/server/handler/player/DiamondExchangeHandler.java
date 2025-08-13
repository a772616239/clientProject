package server.handler.player;

import protocol.Common.EnumFunction;
import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @Description
 * @Author hanx
 * @Date2020/6/8 0008 14:59
 **/
@MsgId(msgId = protocol.MessageId.MsgIdEnum.CS_ExchangeDiamond_VALUE)
public class DiamondExchangeHandler extends AbstractBaseHandler<protocol.PlayerInfo.CS_ExchangeDiamond> {
    @Override
    protected protocol.PlayerInfo.CS_ExchangeDiamond parse(byte[] bytes) throws Exception {
        return protocol.PlayerInfo.CS_ExchangeDiamond.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, protocol.PlayerInfo.CS_ExchangeDiamond req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        protocol.PlayerInfo.SC_ExchangeDiamond.Builder resultBuilder = protocol.PlayerInfo.SC_ExchangeDiamond.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("DiamondExchangeHandler, playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(protocol.MessageId.MsgIdEnum.SC_ExchangeDiamond_VALUE, resultBuilder);
            return;
        }
        int targetExchangeTime = req.getTargetExchangeTime();
        if (targetExchangeTime <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_ErrorParam));
            gsChn.send(protocol.MessageId.MsgIdEnum.SC_ExchangeDiamond_VALUE, resultBuilder);
            return;
        }


        SyncExecuteFunction.executeConsumer(player, entity -> {
            protocol.PlayerDB.DB_PlayerData.Builder db_data = player.getDb_data();
            if (db_data == null) {
                LogUtil.error("DiamondExchangeHandler, playerIdx[" + playerIdx + "] dbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_UnknownError));
                gsChn.send(protocol.MessageId.MsgIdEnum.SC_ExchangeDiamond_VALUE, resultBuilder);
                return;
            }

            int priceDianQuan = GameConfig.getById(GameConst.CONFIG_ID).getPrice_dianquan();
            int priceDiamond = GameConfig.getById(GameConst.CONFIG_ID).getPrice_diamond();
            if (priceDianQuan < 0 || priceDiamond < 0) {
                LogUtil.error("game config priceDianQuan:{} or Price_diamond:{} error by less than 0", priceDianQuan, priceDiamond);
                return;
            }
            if (!player.consumeCurrency(RewardTypeEnum.RTE_Coupon, priceDianQuan * targetExchangeTime, ReasonManager.getInstance().borrowReason(protocol.Common.RewardSourceEnum.RSE_CouponExchange))) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_Player_DiamondNotEnought));
                gsChn.send(protocol.MessageId.MsgIdEnum.SC_ExchangeDiamond_VALUE, resultBuilder);
                return;
            }
            player.addCurrency(protocol.Common.RewardTypeEnum.RTE_Diamond, targetExchangeTime * priceDiamond, ReasonManager.getInstance().borrowReason(protocol.Common.RewardSourceEnum.RSE_CouponExchange));


            resultBuilder.setRetCode(GameUtil.buildRetCode(protocol.RetCodeId.RetCodeEnum.RCE_Success));
            gsChn.send(protocol.MessageId.MsgIdEnum.SC_ExchangeDiamond_VALUE, resultBuilder);

            GlobalData.getInstance().sendDisRewardMsg(playerIdx,
                    RewardUtil.parseReward(RewardTypeEnum.RTE_Diamond, 0, targetExchangeTime * priceDiamond), RewardSourceEnum.RSE_CouponExchange);
        });

    }


    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
