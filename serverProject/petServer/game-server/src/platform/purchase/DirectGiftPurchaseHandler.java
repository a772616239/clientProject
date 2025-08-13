package platform.purchase;

import common.GameConst;
import common.SyncExecuteFunction;
import java.util.List;
import lombok.Getter;
import model.activity.ActivityManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.entity.PurchaseData;
import platform.entity.PurchaseHandler;
import protocol.Activity;
import protocol.Server;
import protocol.TargetSystemDB;
import util.LogUtil;

/**
 * 直购礼包购买
 */
@PurchaseHandler
public class DirectGiftPurchaseHandler extends BasePurchaseHandler {

    @Getter
    private GameConst.RechargeType rechargeType = GameConst.RechargeType.DirectGift;
    @Getter
    private static final DirectGiftPurchaseHandler instance = new DirectGiftPurchaseHandler();

    @Override
    public boolean settlePurchase(PurchaseData data) {
        String playerId = data.getPlayerIdx();
        Server.ServerActivity activity = ActivityManager.getInstance().findOneRecentActivityByType(Activity.ActivityTypeEnum.ATE_DirectPurchaseGift);

        if (activity == null) {
            LogUtil.warn("ActivityGiftPurchaseHandler settlePurchase error by activity closed by playerIdx:{},productCode:{},player only obtain part of rewards ", playerId, data.getProductCode());
            //如果充值活动找不到(关闭)只发放经验
            PurchaseManager.getInstance().doPurchaseReward(playerId, data, null, null);
            return false;
        }
        Activity.DirectPurchaseGift directPurchaseGift = queryGiftByProductCode(activity.getDirectPurchaseGiftList(), data.getRechargeProduct().getId());
        if (directPurchaseGift == null) {
            LogUtil.warn("ActivityGiftPurchaseHandler settlePurchase error by directPurchaseGift is null by playerIdx:{},productCode:{},player obtain part of rewards", playerId, data.getProductCode());
            //如果充值活动找不到(关闭)只发放经验
            PurchaseManager.getInstance().doPurchaseReward(playerId, data, null, null);
            return false;
        }

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);

        if (target == null) {
            LogUtil.error("ActivityGiftPurchaseHandler settlePurchase error by target is null by playerIdx:{}", playerId);
            return false;
        }
        saveDirectGiftBuyRecord(directPurchaseGift, target);

        PurchaseManager.getInstance().doPurchaseReward(playerId, data, directPurchaseGift.getRewardList(), null);

        target.sendDirectGiftPurchaseUpdate(directPurchaseGift.getGiftId());


        return true;
    }

    private void saveDirectGiftBuyRecord(Activity.DirectPurchaseGift directPurchaseGift, targetsystemEntity target) {
        SyncExecuteFunction.executeConsumer(target, e -> {
            TargetSystemDB.DB_SpecialActivity.Builder specialInfoBuilder = target.getDb_Builder().getSpecialInfoBuilder();
            Integer purchaseTimes = specialInfoBuilder.getDirectPurchaseGiftBuyRecordMap().get(directPurchaseGift.getGiftId());
            int nowPurchaseTimes = purchaseTimes == null ? 1 : purchaseTimes + 1;
            specialInfoBuilder.putDirectPurchaseGiftBuyRecord(directPurchaseGift.getGiftId(), nowPurchaseTimes);
        });
    }


    private Activity.DirectPurchaseGift queryGiftByProductCode(List<Activity.DirectPurchaseGift> gifts, int rechargeProductId) {
        return gifts.stream().filter(gift -> gift.getRechargeProductId()==rechargeProductId)
                .findFirst().orElse(null);

    }

    public Activity.DirectPurchaseGift queryGiftByGiftId(long giftId) {
        Server.ServerActivity activity = ActivityManager.getInstance().findOneRecentActivityByType(Activity.ActivityTypeEnum.ATE_DirectPurchaseGift);
        if (activity == null) {
            return null;
        }
        for (Activity.DirectPurchaseGift gift : activity.getDirectPurchaseGiftList()) {
            if (gift.getGiftId() == giftId) {
                return gift;
            }
        }
        LogUtil.warn("DirectGiftPurchaseHandler.queryGiftByGiftId is null by giftId:{}", giftId);
        return null;
    }
}
