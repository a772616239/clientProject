
package platform.purchase;


import cfg.MonthlyCardConfig;
import cfg.MonthlyCardConfigObject;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import platform.entity.PurchaseData;
import protocol.Common;
import protocol.Common.Reward;
import protocol.PlayerDB;
import server.handler.monthCard.MonthCardUtil;
import util.LogUtil;


@Getter
public abstract class MonthCardPurchaseHandler extends BasePurchaseHandler {

    private int cardId;

    private static final Map<Integer, Reward> showRewardMap = new HashMap<>(2);

    @Override
    public void init() {
        showRewardMap.put(getCardId(), Reward.newBuilder().setCount(1).setRewardType(Common.RewardTypeEnum.RTE_MonthCard).setId(getCardId()).build());
    }

    @Override
    public boolean settlePurchase(PurchaseData data) {
        String playerIdx = data.getPlayerIdx();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("BuyMonthCardHandler,playerEntity is null by playerIdx:{}", playerIdx);
            return false;
        }

        MonthlyCardConfigObject config = MonthlyCardConfig.getById(getCardId());
        if (config == null) {
            LogUtil.error("BuyMonthCardHandler,player:{} cant`t find month card config by cardId", playerIdx, getCardId());
            return false;
        }

        List<PlayerDB.DB_MonthCardInfo> monthCardListList = player.getDb_data().getRechargeCards().getMonthCardListList();
        PlayerDB.DB_MonthCardInfo cardInfo = monthCardListList.stream().filter(card -> card.getCarId() == getCardId()).findAny().orElse(null);
        if (cardInfo != null && cardInfo.getRemainDays() > 0) {
            LogUtil.error("BuyMonthCardHandler,player monthly card repeated buy,playerIdx:{},cardId:{},remainDays:{}", playerIdx, getCardId(), cardInfo.getRemainDays());
            return false;
        }

        //添加玩家月卡信息
        SyncExecuteFunction.executeConsumer(player, entity -> {
            List<PlayerDB.DB_MonthCardInfo> newCardList = player.getDb_data().getRechargeCardsBuilder().getMonthCardListList().stream()
                    .filter(card -> getCardId() != card.getCarId()).collect(Collectors.toList());

            PlayerDB.DB_MonthCardInfo.Builder card = buildNewCardInfo(getCardId());
            newCardList.add(card.build());
            player.getDb_data().getRechargeCardsBuilder().clearMonthCardList().addAllMonthCardList(newCardList);

            //新卡立马获得邮件奖励和购买奖励

            MonthCardUtil.doMonthCardDailyReward(playerIdx, getCardId(), card.getRemainDays());
        });
        LogUtil.info("BuyMonthCardHandler,playerEntity by monthCard success by playerIdx:{}", playerIdx);
        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(config.getInstantrewards());
        PurchaseManager.getInstance().doPurchaseReward(playerIdx, data, rewards, showRewardMap.get(getCardId()));

        player.sendMonthCardUpdate();
        return true;
    }

    private PlayerDB.DB_MonthCardInfo.Builder buildNewCardInfo(int buyCardId) {
        PlayerDB.DB_MonthCardInfo.Builder card = PlayerDB.DB_MonthCardInfo.newBuilder();
        card.setCarId(buyCardId);
        //购买的时候就会消耗一次
        card.setRemainDays(GameConst.ONE_MONTH_CARD_USE_DAY - 1);
        return card;
    }


}