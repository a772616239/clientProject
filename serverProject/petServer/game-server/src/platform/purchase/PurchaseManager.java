package platform.purchase;


import cfg.RechargeObject;
import cfg.RechargeProduct;
import cfg.RechargeProductObject;
import common.GlobalData;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import platform.entity.PurchaseData;
import platform.entity.PurchaseHandler;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.RechargeLog;
import protocol.Common;
import protocol.MessageId;
import server.http.entity.PlatformPurchaseData;
import util.ClassUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static common.GameConst.RechargeProduct.AdvancedFeats;
import static common.GameConst.RechargeProduct.AdvancedMothCard;
import static common.GameConst.RechargeProduct.BaseMothCard;

public class PurchaseManager {

    private PurchaseManager() {
    }


    @Getter
    private static final PurchaseManager instance = new PurchaseManager();

    private static List<PurchaseInterface> handlerPools = new ArrayList<>();

    private static final ConcurrentHashMap<String, Common.SC_DisplayRewards.Builder> rewardMsgMap = new ConcurrentHashMap<>();


    private final Map<Integer, List<Common.Reward>> doRewardMap = new HashMap<>();


    public void putDoRewardMapMap(int rechargeProductId, List<Common.Reward> rewards) {
        doRewardMap.put(rechargeProductId, rewards);
    }


    public boolean init() {
        handlerPools = new ArrayList<>();
        List<Class<PurchaseHandler>> classByAnnotation = ClassUtil.getClassByAnnotation("platform.purchase", PurchaseHandler.class);
        for (Class<PurchaseHandler> clazz : classByAnnotation) {
            try {
                Method getInstance = clazz.getMethod("getInstance");
                Object instance = getInstance.invoke(null);
                Method init = clazz.getMethod("init");
                init.invoke(instance);
                handlerPools.add((PurchaseInterface) instance);
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
                return false;
            }

        }

        for (RechargeProductObject productCfg : RechargeProduct._ix_id.values()) {
            List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(productCfg.getReward());
            putDoRewardMapMap(productCfg.getId(), rewards);
            //月卡和功勋需要展示奖励
            if (BaseMothCard == productCfg.getId()) {
                rewards = new ArrayList<>(rewards);
                rewards.add(Common.Reward.newBuilder().setId(1).setRewardType(Common.RewardTypeEnum.RTE_MonthCard).setCount(1).build());
                continue;
            }
            if (AdvancedMothCard == productCfg.getId()) {
                rewards = new ArrayList<>(rewards);
                rewards.add(Common.Reward.newBuilder().setId(2).setRewardType(Common.RewardTypeEnum.RTE_MonthCard).setCount(1).build());
                continue;
            }
            if (AdvancedFeats == productCfg.getId()) {
                rewards = new ArrayList<>(rewards);
                rewards.add(Common.Reward.newBuilder().setRewardType(Common.RewardTypeEnum.RTE_AdvancedFeats).setCount(1).build());
            }

        }

        return !CollectionUtils.isEmpty(handlerPools);
    }


    public PurchaseInterface getHandlerByProductCode(PurchaseData data) {
        for (PurchaseInterface handler : handlerPools) {
            if (handler.containsProduct(data)) {
                return handler;
            }
        }

        return null;
    }


    public boolean settlePurchaseByPlatformPurchaseData(PlatformPurchaseData data) {
        PurchaseData purchaseData = PlatformPurchaseData2PurchaseData(data);
        if (purchaseData == null) {
            LogUtil.error("settlePurchaseByPlatformPurchaseData purchaseData is null");
            return false;
        }

        return settlePurchaseByPurchaseData(purchaseData);
    }

    private boolean settlePurchaseByPurchaseData(PurchaseData data) {
        PurchaseInterface handler = getHandlerByProductCode(data);
        if (handler == null) {
            LogUtil.error("settlePurchase,without purchaseHandler by productCode:{},orderNo:{},please check productCode", data.getProductCode(), data.getOrderNo());
            return false;
        }

        return handler.settlePurchase(data);
    }

    public static PurchaseData PlatformPurchaseData2PurchaseData(PlatformPurchaseData data) {
        if (data == null) {
            return null;
        }
        playerEntity player = playerCache.getInstance().getPlayerByUserId(data.getUserId());
        if (player == null) {
            LogUtil.error("PlatformPurchaseData2PurchaseData player is null,playerUid:{}", data.getUserId());
            return null;
        }
        RechargeProductObject product = getRechargeProductByPlatformPurchaseData(data);
        if (product == null) {
            LogUtil.error("PlatformPurchaseData2PurchaseData RechargeProductObject is null,playerUid:{}", data.getUserId());
            return null;
        }
        PurchaseData purchaseData = new PurchaseData();
        purchaseData.setPlayerIdx(player.getIdx());
        purchaseData.setOrderNo(data.getOrderNo());
        purchaseData.setPayPrice(data.getPayPrice());
        purchaseData.setProductCode(data.getProductCode());

        purchaseData.setRechargeProduct(product);
        purchaseData.setReason(getPlatformReasonFromProductId(product.getId()));
        LogUtil.info(" platform recharge playerUid:{} to playerIdx:{}", data.getUserId(), player.getIdx());
        return purchaseData;
    }

    private static ReasonManager.Reason getPlatformReasonFromProductId(int productId) {
        if (productId == BaseMothCard || productId == AdvancedMothCard) {
            return ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_MonthCardPurchase);
        }
        if (productId == AdvancedFeats) {
            return ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_FeatsPurchase);
        }
        if (productId < 7) {
            ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_Recharge);
        }
        return ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_DirectPurchaseGift);
    }

    private static RechargeProductObject getRechargeProductByPlatformPurchaseData(PlatformPurchaseData data) {
        String productCode = data.getProductCode();
        if (!StringUtils.isEmpty(productCode)) {
            for (RechargeProductObject cfg : RechargeProduct._ix_id.values()) {
                if (cfg.getIosproductid().equals(productCode) || cfg.getGoogleproductid().equals(productCode) || cfg.getHyzproductid().equals(productCode)) {
                    return cfg;
                }
            }
        }
        if (data.getPayPrice() != null && data.getPayPrice().intValue() > 0) {
            RechargeObject rechargeByPrice = CouponPurchaseHandler.getInstance().getRechargeByPrice(data.getPayPrice());
            if (rechargeByPrice != null) {
                return RechargeProduct.getById(rechargeByPrice.getProductid());
            }
        }
        return null;
    }


    public void storeMsg(String playerIdx, List<Common.Reward> rewards, Common.RewardSourceEnum rewardSource) {
        Common.SC_DisplayRewards.Builder builder = Common.SC_DisplayRewards.newBuilder();
        builder.addAllRewardList(rewards);
        builder.setRewardSource(rewardSource);
        rewardMsgMap.put(playerIdx, builder);
    }

    public void onPlayerLogin(String playerIdx) {
        Common.SC_DisplayRewards.Builder msg = rewardMsgMap.get(playerIdx);
        if (msg == null) {
            return;
        }
        if (GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_DisplayRewards_VALUE, msg)) {
            rewardMsgMap.remove(playerIdx);
        }
    }

    public void settlePurchaseByRechargeProduct(String playerIdx, int productId, ReasonManager.Reason reason) {
        RechargeProductObject productCfg = RechargeProduct.getById(productId);
        if (productCfg == null) {
            LogUtil.error("playerIdx settlePurchaseByRechargeProduct productCfg is null by id:{}", productId);
            return;
        }
        PurchaseData purchaseData = new PurchaseData(playerIdx, productCfg, reason);
        settlePurchaseByPurchaseData(purchaseData);
    }

    public void doPurchaseReward(String playerId, PurchaseData purchaseData, List<Common.Reward> exRewards, Common.Reward exShowReward) {
        if (StringUtils.isEmpty(playerId) || purchaseData == null || purchaseData.getRechargeProduct() == null) {
            return;
        }
        LogUtil.info("doPurchaseReward playerIdx:{},purchaseData:{}", playerId, purchaseData);

        RechargeProductObject product = purchaseData.getRechargeProduct();

        List<Common.Reward> doRewards = RewardUtil.mergeRewardList(doRewardMap.get(product.getId()), exRewards);

        EventUtil.triggerReChargeActivity(playerId, purchaseData.getRechargeProduct().getRechargescore());

        RewardManager.getInstance().doRewardByList(playerId, doRewards, purchaseData.getReason(), false);

        List<Common.Reward> showRewards = doRewards;
        if (exShowReward != null) {
            showRewards = new ArrayList<>(doRewards);
            showRewards.add(exShowReward);
        }

        LogUtil.info("user" + ", playerId =" + playerId + " purchase finish, obtain rewards:" + GameUtil.collectionToString(showRewards) + ",orderNo " + purchaseData.getOrderNo());

        if (!GlobalData.getInstance().checkPlayerOnline(playerId)) {
            storeMsg(playerId, showRewards, purchaseData.getReason().getSourceEnum());
        } else {
            GlobalData.getInstance().sendDisRewardMsg(playerId, showRewards, purchaseData.getReason().getSourceEnum());
        }
        //充值成功
        LogService.getInstance().submit(new RechargeLog(playerId, purchaseData.getOrderNo(), showRewards));
    }
}
