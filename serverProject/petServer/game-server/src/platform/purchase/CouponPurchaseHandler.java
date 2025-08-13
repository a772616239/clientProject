package platform.purchase;

import cfg.Recharge;
import cfg.RechargeObject;
import cfg.RechargeProduct;
import cfg.RechargeProductObject;
import common.GameConst;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import model.activity.ActivityManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.lang.StringUtils;
import platform.entity.PurchaseData;
import platform.entity.PurchaseHandler;
import platform.logs.LogService;
import platform.logs.entity.RechargeLog;
import protocol.Common;
import server.handler.recharge.service.RechargeService;
import util.EventUtil;
import util.LogUtil;


/**
 * 点券充值
 */
@PurchaseHandler
public class CouponPurchaseHandler extends BasePurchaseHandler {

    @Getter
    private GameConst.RechargeType rechargeType = GameConst.RechargeType.Coupon;
    @Getter
    private static final CouponPurchaseHandler instance = new CouponPurchaseHandler();

    private static final Map<String, RechargeObject> productRechargeMap = new HashMap<>();
    private static final Map<Integer, RechargeObject> priceRechargeMap = new HashMap<>();
    private static final Map<Integer, RechargeObject> productIdRechargeMap = new HashMap<>();


    @Override
    public void init() {
        for (RechargeObject rechargeObject : Recharge._ix_id.values()) {
            if (rechargeObject.getId() <= 0) {
                continue;
            }
            priceRechargeMap.put(rechargeObject.getRechargeamount2(), rechargeObject);
            RechargeProductObject productObject = RechargeProduct.getById(rechargeObject.getProductid());
            if (productObject == null) {
                LogUtil.error("CouponPurchaseHandler init,RechargeProductObject is null by rechargeId:{}", rechargeObject.getId());
                continue;
            }
            productRechargeMap.put(productObject.getGoogleproductid(), rechargeObject);
            productRechargeMap.put(productObject.getIosproductid(), rechargeObject);
            productRechargeMap.put(productObject.getHyzproductid(), rechargeObject);
            productIdRechargeMap.put(productObject.getId(), rechargeObject);
        }

    }

    @Override
    public boolean settlePurchase(PurchaseData data) {
        String playerId = data.getPlayerIdx();

        RechargeObject cfg = findRechargeByPurchaseData(data);
        if (cfg == null) {
            LogUtil.error("playerIdx:{},CouponPurchaseHandler settlePurchase error by cfg is null", playerId);
            //充值失败log
            LogService.getInstance().submit(new RechargeLog(playerId, data.getOrderNo(), "未找到对应商品配置:" + data.getProductCode()));
            return false;
        }
        int numberOfDiamonds = cfg.getNumberofdiamonds();
        int giftDiamonds = RechargeService.instance.getGiftDiamonds(playerId, cfg.getId());

        int rebateRate = queryRebateRate(playerId);

        int totalRewardDiamonds = numberOfDiamonds + giftDiamonds + (int) Math.ceil(numberOfDiamonds * rebateRate / 100.0);
        List<Common.Reward> rewards = new ArrayList<>();
        rewards.add(RewardUtil.parseReward(Common.RewardTypeEnum.RTE_Coupon, 0, totalRewardDiamonds));

        PurchaseManager.getInstance().doPurchaseReward(playerId, data, rewards, null);
        //如果限购

        if (cfg.getPurchaselimit() || cfg.getFirstrechargetype()) {
            EventUtil.triggerAddLimitPurchaseId(playerId, cfg.getId());
        }

        RechargeService.instance.sendSuccessMsg(playerId, cfg.getId());

        clearExpireRechargeRebateAddition(playerId);

        return true;
    }

    private void clearExpireRechargeRebateAddition(String playerId) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (target == null) {
            return;
        }
        if (target.queryRichManRebate() > 0) {
            target.getDb_Builder().getSpecialInfoBuilder().getRichManBuilder().clearDischargeRebate();
            target.sendRichManInfoUpdate();
        }

    }

    private int queryRebateRate(String playerId) {
        int rebateRate1 = ActivityManager.getInstance().queryRechargeRebateRate();

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        int rebateRate2 = 0;
        if (target != null) {
            rebateRate2 = target.queryRichManRebate();
        }
        int total = rebateRate1 + rebateRate2;
        LogUtil.info("player:{} recharge rebate, total rate:{} %,made up of recharge rebate Activity:{} %," +
                "  rich man activity:{}% ", playerId, total, rebateRate1, rebateRate2);

        return total;
    }


    public RechargeObject findProductCode(PurchaseData data) {
        if (StringUtils.isNotBlank(data.getProductCode())) {
            return productRechargeMap.get(data.getProductCode());
        }
        if (data.getPayPrice() != null) {
            return priceRechargeMap.get(data.getPayPrice().intValue());
        }
        return null;

    }


    public RechargeObject getRechargeByPrice(BigDecimal payPrice) {
        if (payPrice == null) {
            return null;
        }
        return priceRechargeMap.get(payPrice.intValue());
    }


    private RechargeObject findRechargeByPurchaseData(PurchaseData data) {
        if (StringUtils.isNotBlank(data.getProductCode())) {
            return productRechargeMap.get(data.getProductCode());
        }
        if (data.getPayPrice() != null) {
            return priceRechargeMap.get(data.getPayPrice().intValue());
        }
        if (data.getRechargeProduct() != null) {
            return productIdRechargeMap.get(data.getRechargeProduct().getId());
        }

        return null;
    }


}
