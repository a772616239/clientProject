package platform.purchase;

import cfg.PrivilegedCardCfg;
import cfg.PrivilegedCardCfgObject;
import cfg.RechargeProductObject;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import platform.entity.PurchaseData;
import platform.entity.PurchaseHandler;
import platform.logs.LogService;
import platform.logs.entity.RechargeLog;
import protocol.Common;
import protocol.PlayerDB;
import server.handler.monthCard.MonthCardUtil;
import util.LogUtil;


/**
 * 点券充值
 */
@PurchaseHandler
public class PrivilegedCardPurchaseHandler extends BasePurchaseHandler {

    @Getter
    private GameConst.RechargeType rechargeType = GameConst.RechargeType.BetterItemCard;

    @Getter
    private static final PrivilegedCardPurchaseHandler instance = new PrivilegedCardPurchaseHandler();


    @Override
    public boolean settlePurchase(PurchaseData data) {
        String playerId = data.getPlayerIdx();

        RechargeProductObject cfg = data.getRechargeProduct();
        if (cfg == null) {
            LogUtil.error("playerIdx:{},PrivilegedCardPurchaseHandler settlePurchase error by RechargeProductObject is null", playerId);
            //充值失败log
            LogService.getInstance().submit(new RechargeLog(playerId, data.getOrderNo(), "未找到对应商品配置:" + data.getProductCode()));
            return false;
        }

        int cardId = cfg.getSubtype();

        PrivilegedCardCfgObject cardCfg = PrivilegedCardCfg.getById(cardId);
        if (cardCfg == null) {
            LogUtil.error("playerIdx:{},PrivilegedCardPurchaseHandler settlePurchase error by PrivilegedCardCfg is null,cardId:{}", playerId, cardId);
            //充值失败log
            LogService.getInstance().submit(new RechargeLog(playerId, data.getOrderNo(), "未找到对应商品配置:" + data.getProductCode()));
            return false;
        }

        playerEntity player = playerCache.getByIdx(playerId);

        if (player == null) {
            LogUtil.error("PrivilegedCardPurchaseHandler,player PrivilegedCard card buy error,player is null by playerIdx:{}", playerId);
            return false;
        }

        List<PlayerDB.DB_PrivilegedCard> monthCardListList = player.getDb_data().getRechargeCards().getPrivilegedCardList();
        PlayerDB.DB_PrivilegedCard cardInfo = monthCardListList.stream().filter(card -> card.getCarId() == cardId).findAny().orElse(null);
        if (cardInfo != null && cardInfo.getRemainDays() > 0) {
            LogUtil.error("PrivilegedCardPurchaseHandler,player PrivilegedCard card repeated buy,playerIdx:{},cardId:{},remainDays:{}", playerId, cardId, cardInfo.getRemainDays());
            return false;
        }

        //添加玩家月卡信息
        SyncExecuteFunction.executeConsumer(player, entity -> {
            List<PlayerDB.DB_PrivilegedCard> newCardList = player.getDb_data().getRechargeCardsBuilder().getPrivilegedCardList().stream()
                    .filter(card -> cardId != card.getCarId()).collect(Collectors.toList());

            PlayerDB.DB_PrivilegedCard.Builder card = buildNewCardInfo(cardId,cardCfg.getExpiredays());
            newCardList.add(card.build());
            player.getDb_data().getRechargeCardsBuilder().clearPrivilegedCard().addAllPrivilegedCard(newCardList);
        });

        List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(cardCfg.getInstantreward());
        PurchaseManager.getInstance().doPurchaseReward(playerId, data, rewards, null);
        player.sendRechargeCardUpdate();

        return true;
    }

    private PlayerDB.DB_PrivilegedCard.Builder buildNewCardInfo(int cardId, int expireDays) {
        PlayerDB.DB_PrivilegedCard.Builder card = PlayerDB.DB_PrivilegedCard.newBuilder();
        card.setCarId(cardId);
        //购买的时候就会消耗一次
        card.setRemainDays(expireDays - 1);
        return card;
    }

}
