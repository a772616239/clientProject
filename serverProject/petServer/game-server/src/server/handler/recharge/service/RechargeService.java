package server.handler.recharge.service;


import cfg.RechargeObject;
import common.GlobalData;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.MessageId;
import protocol.Recharge;
import util.LogUtil;

import java.util.List;


/**
 * @Description
 * @Author hanx
 * @Date2020/5/13 0013 19:54
 **/
public class RechargeService {
    public static RechargeService instance = new RechargeService();

    public Recharge.SC_UpdateRechargeStatus.Builder getRechargeSuccessBuilder(String playerId, int rechargeId) {
        Recharge.SC_UpdateRechargeStatus.Builder result = Recharge.SC_UpdateRechargeStatus.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        RechargeObject config = cfg.Recharge.getById(rechargeId);
        if (target == null || config == null) {
            LogUtil.error("getRechargeStatusBuilder target or config is null by playerId+[" + playerId
                    + "],rechargeId[" + rechargeId + "]");
            return result;
        }
        result.setGiftDiamonds(getGiftDiamonds(rechargeId));
        result.setSoldOut(config.getPurchaselimit());
        result.setRechargeId(rechargeId);
        return result;
    }

    private int getGiftDiamonds(int rechargeId) {
        if (!isFirstRechargeType(rechargeId)) {
            return cfg.Recharge.getById(rechargeId).getGiftdiamonds();
        }
        return 0;
    }

    /**
     * 当前充值是否首充类型
     *
     * @param rechargeId
     * @return
     */
    private boolean isFirstRechargeType(int rechargeId) {
        return cfg.Recharge.getById(rechargeId).getFirstrechargetype();
    }

    /**
     * 是否售罄
     *
     * @param rechargeId 充值id
     * @param cache
     * @return
     */
    public boolean soldOut(int rechargeId, targetsystemEntity cache) {
        if (cache == null) {
            return true;
        }
        boolean purchaseLimit = cfg.Recharge.getById(rechargeId).getPurchaselimit();
        List<Integer> rechargeIdsList = cache.getDb_Builder().getLimitPurchaseRechargeIdsList();
        return purchaseLimit && rechargeIdsList.contains(rechargeId);
    }

    /**
     * 发送充值成功消息
     *
     * @param playerId   玩家id
     * @param rechargeId 充值id
     */
    public void sendSuccessMsg(String playerId, int rechargeId) {
        protocol.Recharge.SC_UpdateRechargeStatus.Builder result = RechargeService.instance.getRechargeSuccessBuilder(playerId, rechargeId);
        GlobalData.getInstance().sendMsg(playerId, MessageId.MsgIdEnum.SC_UpdateRechargeStatus_VALUE, result);
    }

    public int getGiftDiamonds(String playerId, int rechargeId) {
        RechargeObject config = cfg.Recharge.getById(rechargeId);
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (config == null || target == null) {
            return 0;
        }
        if (!target.getDb_Builder().getLimitPurchaseRechargeIdsList().contains(rechargeId)) {
            return config.getGiftdiamonds();
        }
        return 0;
    }
}
