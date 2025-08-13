package server.handler.recharge;

import protocol.Common.EnumFunction;
import cfg.RechargeObject;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import static protocol.MessageId.MsgIdEnum.CS_GetRechargeList_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_GetRechargeList_VALUE;
import protocol.Recharge;
import protocol.RetCodeId;
import util.GameUtil;

import java.util.List;

/**
 * @Description
 * @Author hanx
 * @Date2020/5/7 0007 15:59
 **/
@MsgId(msgId = CS_GetRechargeList_VALUE)
public class GetRechargeListHandler extends AbstractBaseHandler<Recharge.CS_GetRechargeList> {

    protected Recharge.CS_GetRechargeList parse(byte[] bytes) throws Exception {
        return Recharge.CS_GetRechargeList.parseFrom(bytes);
    }


    @Override
    protected void execute(GameServerTcpChannel gsChn, Recharge.CS_GetRechargeList req, int i) {
        Recharge.SC_GetRechargeList.Builder result = Recharge.SC_GetRechargeList.newBuilder();
        String playerId = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (target == null) {
            return;
        }
        List<Integer> limitPurchaseRechargeIds = target.getDb_Builder().getLimitPurchaseRechargeIdsList();
        for (RechargeObject rechargeObject : cfg.Recharge._ix_id.values()) {
            if (isRechargeActive(rechargeObject)) {
                Recharge.RechargeInfo.Builder builder = toRechargeInfo(rechargeObject, limitPurchaseRechargeIds);
                result.addRecharges(builder);
            }
        }
        result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(SC_GetRechargeList_VALUE, result);
    }

    /**
     * 当前充值是否激活
     * @param config
     * @return
     */
    private static boolean isRechargeActive(RechargeObject config) {
        if (config == null) {
            return false;
        }
        //固定充值不限时间
        if (config.getRechargetype() == 0) {
            return true;
        }
        //活动充值必须在活动时间内
        boolean rechargeNotLimitTime = config.getBegintime() == 0 && config.getEndtime() == 0;
        boolean rechargeIsOnTime = config.getBegintime() < System.currentTimeMillis() / 1000 &&
                (config.getEndtime() != 0 && config.getEndtime() > System.currentTimeMillis() / 1000);

        return rechargeNotLimitTime || rechargeIsOnTime;
    }

    public static Recharge.RechargeInfo.Builder toRechargeInfo(RechargeObject recharge,  List<Integer> limitPurchaseRechargeIds) {
        if (recharge == null) {
            return null;
        }
        Recharge.RechargeInfo.Builder rechargeInfo = Recharge.RechargeInfo.newBuilder();
        rechargeInfo.setId(recharge.getId());
        rechargeInfo.setRechargeAmount(recharge.getRechargeamount2());
        rechargeInfo.setNumberOfDiamonds(recharge.getNumberofdiamonds());
        rechargeInfo.setGiveVipExp(recharge.getGivevipexp());
        rechargeInfo.setRecommend(recharge.getRecommend());
        rechargeInfo.setPurchaseLimit(recharge.getPurchaselimit());
        if (recharge.getRechargetype() != 0) {
            rechargeInfo.setBeginTime(recharge.getBegintime());
            rechargeInfo.setEndTime(recharge.getEndtime());
        }
        rechargeInfo.setRechargeType(recharge.getRechargetype());

        if (recharge.getPurchaselimit() && limitPurchaseRechargeIds.contains(recharge.getId())) {
            rechargeInfo.setSoldOut(true);
        }

        if (!(recharge.getFirstrechargetype()&&limitPurchaseRechargeIds.contains(recharge.getId()))) {
            rechargeInfo.setGiftDiamonds(recharge.getGiftdiamonds());
        }
        return rechargeInfo;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
