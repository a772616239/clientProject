package server.handler.shop;

import cfg.CrossArenaLvCfg;
import cfg.ShopSell;
import cfg.ShopSellObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.crossarena.CrossArenaManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.shop.StoreManager;
import model.shop.dbCache.shopCache;
import model.shop.entity.shopEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.ShopLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.CrossArena;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Shop.CS_BuyGoods;
import protocol.Shop.SC_BuyGoods;
import protocol.Shop.ShopTypeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;

import static common.GameConst.commonMagnification;

@MsgId(msgId = MsgIdEnum.CS_BuyGoods_VALUE)
public class BuyGoodsHandler extends AbstractBaseHandler<CS_BuyGoods> {
    @Override
    protected CS_BuyGoods parse(byte[] bytes) throws Exception {
        return CS_BuyGoods.parseFrom(bytes);
    }

    /**
     * 原价折扣
     */
    private static final int ORIGINAL_PRICE = 1000;


    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyGoods req, int i) {
        ShopTypeEnum shopType = req.getShopType();
        int buyGoodsId = req.getBuyGoodsId();
        int buyCount = req.getBuyCount();

        SC_BuyGoods.Builder resultBuilder = SC_BuyGoods.newBuilder();

        if (buyCount <= 0 || buyGoodsId <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_BuyGoods_VALUE, resultBuilder);
            return;
        }

        ShopSellObject buyCfg = ShopSell.getById(buyGoodsId);
        if (buyCfg == null || buyCfg.getShopid() != shopType.getNumber()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ShopTypeMissMatch));
            gsChn.send(MsgIdEnum.SC_BuyGoods_VALUE, resultBuilder);
            return;
        }
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        if (!checkShopSellMissionStatus(buyCfg, playerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
            gsChn.send(MsgIdEnum.SC_BuyGoods_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum retCodeEnum;
        if (req.getShopType() == ShopTypeEnum.STE_null) {
            retCodeEnum = buySpecialGoods(playerIdx, buyGoodsId, buyCount);
        } else {
            retCodeEnum = buyGoods(playerIdx, shopType, buyGoodsId, buyCount);
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_BuyGoods_VALUE, resultBuilder);

        //统计：商店购买
        if (retCodeEnum == RetCodeEnum.RCE_Success) {
            //目标：商店购买次数
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuBuyGoods, buyCount, 0);

            LogService.getInstance().submit(new ShopLog(playerIdx, buyGoodsId, buyCount, shopType));
        }
    }

    private boolean checkShopSellMissionStatus(ShopSellObject buyCfg, String playerIdx) {
        if (buyCfg.getUnlockcondtion() <= 0) {
            return true;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            return false;
        }
        return target.getCompleteMissionIds().contains(buyCfg.getUnlockcondtion());
    }

    /**
     * 购买商店类型为0的商品,该类型商店购买无上限
     *
     * @param playerIdx
     * @param buyGoodsId
     * @param buyCount
     * @return
     */
    private RetCodeEnum buySpecialGoods(String playerIdx, int buyGoodsId, int buyCount) {
        ShopSellObject goodsCfg = ShopSell.getById(buyGoodsId);
        if (goodsCfg == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        Consume consume = parseConsume(playerIdx, buyCount, goodsCfg);
        if (ConsumeManager.getInstance().consumeMaterial(playerIdx, consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Buy))) {

            RewardManager.getInstance().doReward(playerIdx, RewardUtil.parseAndMulti(goodsCfg.getCargo(), buyCount),
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Buy, buyCount), true);
        }
        return RetCodeEnum.RCE_Success;
    }

    private RetCodeEnum buyGoods(String playerIdx, ShopTypeEnum shopType, int buyGoodsId, int buyCount) {
        shopEntity entity = shopCache.getInstance().getEntityByPlayerIdx(playerIdx);
        ShopSellObject goodsCfg = ShopSell.getById(buyGoodsId);
        if (entity == null || null == goodsCfg) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        int alreadyBuyTimes = entity.getAlreadyBuyTimes(shopType, buyGoodsId);
        int buyLimit = goodsCfg.getBuylimit();
        if (-1 != buyLimit && buyCount > (buyLimit - alreadyBuyTimes)) {
            return RetCodeEnum.RCE_Store_GoodsBuyUpperLimit;
        }

        //消耗
        Consume consume = parseConsume(playerIdx, buyCount, goodsCfg);
        if (consume == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(StoreManager.getRewardSourceTypeByShopType(shopType), "购买", buyCount);
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            return RetCodeEnum.RCE_Player_CurrencysNotEnought;
        }

        Reward reward = RewardUtil.parseAndMulti(goodsCfg.getCargo(), buyCount);
        RewardManager.getInstance().doReward(playerIdx, reward, reason, true);

        SyncExecuteFunction.executeConsumer(entity, e -> entity.addBuyTimes(shopType, buyGoodsId, buyCount));

        return RetCodeEnum.RCE_Success;
    }

    private Consume parseConsume(String playerIdx, int buyCount, ShopSellObject goodsCfg) {
        int discount = parseDiscount(playerIdx, goodsCfg);
        Consume consume = ConsumeUtil.parseConsume(goodsCfg.getPrice());
        if (discount != ORIGINAL_PRICE && consume != null) {
            int count = (int) (consume.getCount() * (discount * 1.0 / commonMagnification));
            consume = consume.toBuilder().setCount(count).build();
        }
        return ConsumeUtil.multiConsume(consume, buyCount);
    }

    private int parseDiscount(String playerIdx, ShopSellObject goodsCfg) {
        if (ShopTypeEnum.STE_CrossArenaVip_VALUE == goodsCfg.getShopid()) {
            return CrossArenaLvCfg.queryDiscountByLvAndDiscountType(
                    CrossArenaManager.getInstance().getPlayerDBInfo(playerIdx, CrossArena.CrossArenaDBKey.LT_GRADELV)
                    , goodsCfg.getDiscounttype());
        }
        return GameConst.SELL_DEFAULT_DISCOUNT;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Shop;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyGoods_VALUE, SC_BuyGoods.newBuilder().setRetCode(retCode));
    }
}
