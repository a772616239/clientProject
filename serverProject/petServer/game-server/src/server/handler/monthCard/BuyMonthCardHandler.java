/*
package server.handler.monthCard;


import cfg.MonthlyCardConfig;
import cfg.MonthlyCardConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.PayActivityLog;
import platform.logs.entity.PayActivityLog.PayActivityEnum;
import protocol.Common;
import protocol.Common.Reward;
import protocol.MessageId.MsgIdEnum;
import protocol.MonthCard;
import protocol.PlayerDB;
import protocol.RetCodeId;
import util.GameUtil;

import java.util.List;
import java.util.stream.Collectors;

@MsgId(msgId = MsgIdEnum.CS_BuyMonthCard_VALUE)
public class BuyMonthCardHandler extends AbstractBaseHandler<MonthCard.CS_BuyMonthCard> {

    @Override
    protected MonthCard.CS_BuyMonthCard parse(byte[] bytes) throws Exception {
        return MonthCard.CS_BuyMonthCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, MonthCard.CS_BuyMonthCard req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        MonthCard.SC_BuyMonthCard.Builder result = MonthCard.SC_BuyMonthCard.newBuilder();

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, result);
            return;
        }

        int buyCardId = req.getMonthType();
        MonthCard.MonthTypeEnum monthTypeEnum = MonthCard.MonthTypeEnum.forNumber(req.getMonthType());
        MonthlyCardConfigObject config = MonthlyCardConfig.getById(buyCardId);
        if (config == null || monthTypeEnum == null || monthTypeEnum == MonthCard.MonthTypeEnum.MTE_Null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_BuyMonthCard_VALUE, result);
            return;
        }

        List<PlayerDB.MonthCardInfo> monthCardListList = player.getDb_data().getMonthCardListList();
        PlayerDB.MonthCardInfo cardInfo = monthCardListList.stream().filter(card -> card.getCarId() == buyCardId).findAny().orElse(null);
        if (cardInfo != null && cardInfo.getRemainDays() > 0) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MonthCard_LimitBuy));
            gsChn.send(MsgIdEnum.SC_BuyMonthCard_VALUE, result);
            return;
        }

        ReasonManager.Reason reason = MonthCardUtil.getReasonByCarType(buyCardId);
        Common.Consume consume = ConsumeUtil.parseAndMulti(config.getPrice(), 1);
        //购买消耗
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_BuyMonthCard_VALUE, result);
            return;
        }


        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(config.getInstantrewards());
        //添加玩家月卡信息
        SyncExecuteFunction.executeConsumer(player, entity -> {
            List<PlayerDB.MonthCardInfo> newCardList = player.getDb_data().getMonthCardListList().stream()
                    .filter(card -> buyCardId != card.getCarId()).collect(Collectors.toList());

            PlayerDB.MonthCardInfo.Builder card = buildNewCardInfo(buyCardId);
            newCardList.add(card.build());
            player.getDb_data().clearMonthCardList().addAllMonthCardList(newCardList);

            //新卡立马获得邮件奖励和购买奖励
            RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);
            MonthCardUtil.doMonthCardDailyReward(playerIdx, buyCardId, card.getRemainDays());

            //推送购买消息
            result.setRemainDays(card.getRemainDays());
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_BuyMonthCard_VALUE, result);
        });
        //日志埋点
        PayActivityEnum payActivityEnum = req.getMonthType() == MonthCard.MonthTypeEnum.MTE_Advanced_VALUE ?
                PayActivityEnum.AdvancedMonthlyCard : PayActivityEnum.NormalMonthlyCard;

        LogService.getInstance().submit(new PayActivityLog(playerIdx, consume, payActivityEnum));

    }

    private PlayerDB.MonthCardInfo.Builder buildNewCardInfo(int buyCardId) {
        PlayerDB.MonthCardInfo.Builder card = PlayerDB.MonthCardInfo.newBuilder();
        card.setCarId(buyCardId);
        //购买的时候就会消耗一次
        card.setRemainDays(GameConst.ONE_MONTH_CARD_USE_DAY - 1);
        return card;
    }


}
*/
