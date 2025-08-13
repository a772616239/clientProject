package server.handler.shop;

import cfg.ShopConfig;
import cfg.ShopRefreshSpend;
import cfg.VIPConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.player.util.PlayerUtil;
import model.shop.StoreManager;
import model.shop.dbCache.shopCache;
import model.shop.entity.shopEntity;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Shop.CS_RefreshShop;
import protocol.Shop.SC_RefreshShop;
import protocol.Shop.ShopTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_RefreshShop_VALUE)
public class RefreshShopHandler extends AbstractBaseHandler<CS_RefreshShop> {
    @Override
    protected CS_RefreshShop parse(byte[] bytes) throws Exception {
        return CS_RefreshShop.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RefreshShop req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        ShopTypeEnum shopType = req.getShopType();

        SC_RefreshShop.Builder resultBuilder = SC_RefreshShop.newBuilder();
        //判断是否支持手动刷新
        if (!StoreManager.getInstance().supportManualRefresh(shopType)) {
            resultBuilder.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_Store_UnsupportManualRefresh));
            gsChn.send(MsgIdEnum.SC_RefreshShop_VALUE, resultBuilder);
            return;
        }

        shopEntity entity = shopCache.getInstance().getEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_RefreshShop_VALUE, resultBuilder);
            return;
        }

        int manualRefreshTimes = entity.getManualRefreshTimes(req.getShopType());
        int shopManualRefreshLimit = getShopRefreshLimit(playerIdx, shopType);
        if (shopManualRefreshLimit != -1 && manualRefreshTimes >= shopManualRefreshLimit) {
            resultBuilder.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_Store_RefreshTimesLimit));
            gsChn.send(MsgIdEnum.SC_RefreshShop_VALUE, resultBuilder);
            return;
        }

        Consume consume = ShopRefreshSpend.getInstance().getRefreshSpend(shopType, manualRefreshTimes + 1);
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume,
                ReasonManager.getInstance().borrowReason(StoreManager.getRewardSourceTypeByShopType(shopType), "刷新"))) {

            resultBuilder.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_CurrencysNotEnought));
            gsChn.send(MsgIdEnum.SC_RefreshShop_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> entity.manualRefreshAndAddTimes(shopType));


        resultBuilder.setRetcode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setShopInfo(entity.buildPlayerShowShopInfo(shopType));
        gsChn.send(MsgIdEnum.SC_RefreshShop_VALUE, resultBuilder);

        if (shopType == ShopTypeEnum.STE_BlackMarket) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuManualRefreshBlack, 1, 0);
        }
    }

    /**
     * 获取商店刷新次数上限
     *
     * @return
     */
    public int getShopRefreshLimit(String playerIdx, ShopTypeEnum shopType) {
        if (shopType == ShopTypeEnum.STE_BlackMarket) {
            return VIPConfig.getBoutiqueManualRefreshLimit(PlayerUtil.queryPlayerVipLv(playerIdx));
        }
        return ShopConfig.getShopManualRefreshLimit(shopType);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Shop;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_RefreshShop_VALUE, SC_RefreshShop.newBuilder());
    }
}
