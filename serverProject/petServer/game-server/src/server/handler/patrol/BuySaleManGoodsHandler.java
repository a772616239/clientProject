package server.handler.patrol;

import cfg.SaleManGoodsCfg;
import cfg.SaleManGoodsCfgObject;
import cfg.ShopSell;
import cfg.ShopSellObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.patrol.dbCache.patrolCache;
import model.patrol.entity.patrolEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.Patrol.CS_BuyPatrolGoods;
import protocol.Patrol.PatrolSaleMan;
import protocol.Patrol.SC_BuyPatrolGoods;
import protocol.Patrol.SC_BuyPatrolGoods.Builder;
import protocol.Patrol.SaleManGoods;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import util.EventUtil;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_BuyPatrolGoods_VALUE)
public class BuySaleManGoodsHandler extends AbstractBaseHandler<CS_BuyPatrolGoods> {
    @Override
    protected CS_BuyPatrolGoods parse(byte[] bytes) throws Exception {
        return CS_BuyPatrolGoods.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyPatrolGoods req, int i) {
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
        patrolEntity cache = patrolCache.getInstance().getCacheByPlayer(playerId);
        Builder result = SC_BuyPatrolGoods.newBuilder();

        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            PatrolSaleMan.Builder saleMan = cache.getPatrolStatusEntity().getSaleMan().toBuilder();
            if (!saleMan.getOpen()) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gsChn.send(MsgIdEnum.SC_BuyPatrolGoods_VALUE, result);
                return;
            }
            SaleManGoods.Builder goodsInDb = saleMan.getGoodsListBuilderList().stream().filter(good -> good.getGoodsId() == req.getGoodsId()).findAny().orElse(null);
            SaleManGoodsCfgObject config = SaleManGoodsCfg.getById(req.getGoodsId());
            ShopSellObject shopShell;
            if (goodsInDb == null || config == null || (shopShell = ShopSell.getById(config.getShopshellid())) == null) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Store_GoodsIdNotExist));
                gsChn.send(MsgIdEnum.SC_BuyPatrolGoods_VALUE, result);
                return;
            }
            if (goodsInDb.getSaleOut()) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Store_GoodsBuyUpperLimit));
                gsChn.send(MsgIdEnum.SC_BuyPatrolGoods_VALUE, result);
                return;
            }
            //消耗
            Consume consume = ConsumeUtil.parseDisCountConsume(shopShell.getPrice(), goodsInDb.getDiscount());
            if (consume == null) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_BuyPatrolGoods_VALUE, result);
                return;
            }
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Buy);
            if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
                result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
                gsChn.send(MsgIdEnum.SC_BuyPatrolGoods_VALUE, result);
                return;
            }
            goodsInDb.setSaleOut(true);
            cache.setPatrolStatusEntity(cache.getPatrolStatusEntity().toBuilder().setSaleMan(saleMan.build()).build());
            Reward reward = RewardUtil.parseReward(shopShell.getCargo());
            RewardManager.getInstance().doReward(playerId, reward, reason, true);
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_BuyPatrolGoods_VALUE, result);

            //目标：购买x次虚空商品
            EventUtil.triggerUpdateTargetProgress(playerId, TargetSystem.TargetTypeEnum.TTE_Patrol_BuySaleManGoods, 1, 0);

        });
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Patrol;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyPatrolGoods_VALUE, SC_BuyPatrolGoods.newBuilder().setResult(retCode));
    }
}
