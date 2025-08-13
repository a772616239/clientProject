package model.warpServer.crossServer.handler.mistforest;

import cfg.MistItem;
import cfg.MistItemObject;
import cfg.ShopSell;
import cfg.ShopSellObject;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MazeBuyGoodsTimes;
import protocol.MistForest.MistItemInfo;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_BuyMistGoods;
import protocol.MistForest.SC_MistMazeBuyGoodsTimes;
import protocol.ServerTransfer.CS_GS_BuyMistGoods;
import protocol.ServerTransfer.GS_CS_ProvideGoods;

@MsgId(msgId = MsgIdEnum.CS_GS_BuyMistGoods_VALUE)
public class BuyMistItemHandler extends AbstractHandler<CS_GS_BuyMistGoods> {
    @Override
    protected CS_GS_BuyMistGoods parse(byte[] bytes) throws Exception {
        return CS_GS_BuyMistGoods.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_BuyMistGoods req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        SC_BuyMistGoods.Builder retBuilder = SC_BuyMistGoods.newBuilder();
        if (req.getRetCode() != MistRetCode.MRC_Success) {
            retBuilder.setRet(req.getRetCode());
            GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        MistRetCode retCode;
        if (req.getGoodsType() == 0) {
            retCode = buyMistGoods(player, req.getGoodsId());
            if (retCode == MistRetCode.MRC_Success) {
                GS_CS_ProvideGoods.Builder builder = GS_CS_ProvideGoods.newBuilder();
                builder.setItemCfgId(req.getGoodsId());
                builder.setPlayerIdx(player.getIdx());
                gsChn.send(MsgIdEnum.GS_CS_ProvideGoods_VALUE, builder);
            }
        } else {
            retCode = buyCommonGoods(player, req.getGoodsId());
        }
        retBuilder.setRet(retCode);
        GlobalData.getInstance().sendMsg(player.getIdx(), MsgIdEnum.SC_BuyMistGoods_VALUE, retBuilder);
    }

    protected MistRetCode buyMistGoods(playerEntity player, int goodsId) {
        MistItemObject mistItem = MistItem.getByItemid(goodsId);
        if (mistItem == null) {
            return MistRetCode.MRC_ItemNotExist;
        }
        int itemCount = 0;
        for (MistItemInfo itemData : player.getDb_data().getMistForestData().getMistItemDataList()) {
            if (itemData.getItemCfgId() > 0) {
                itemCount++;
            }
        }

        if (itemCount >= GameConst.MistItemSkillMaxCount) {
            return MistRetCode.MRC_ItemFull;
        }
        int currencyType = mistItem.getItemprice()[0];
        int price = mistItem.getItemprice()[2];
        if (price < 0 || (currencyType != RewardTypeEnum.RTE_Gold_VALUE &&
                currencyType != RewardTypeEnum.RTE_Diamond_VALUE)) {
            return MistRetCode.MRC_ItemNotExist;
        }

        Consume consume = ConsumeUtil.parseConsume(currencyType, 0, price);
        if (ConsumeManager.getInstance().consumeMaterial(player.getIdx(), consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest))) {
            return MistRetCode.MRC_Success;
        } else {
            return MistRetCode.MRC_CurrencyNotEnough;
        }
    }

    protected MistRetCode buyCommonGoods(playerEntity player, int goodsId) {
        ShopSellObject shopSellCfg = ShopSell.getById(goodsId);
        if (shopSellCfg == null) {
            return MistRetCode.MRC_ItemNotExist;
        }
        int buyCount = player.getMazeBuyGoodsTimes(goodsId);
        if (buyCount >= shopSellCfg.getBuylimit()) {
            return MistRetCode.MRC_GoodsSoldOut;
        }
        Consume consume = ConsumeUtil.parseAndMulti(shopSellCfg.getPrice(), 1);
        if (consume == null) {
            return MistRetCode.MRC_NotSellInThisShop;
        }
        if (!ConsumeManager.getInstance().consumeMaterial(player.getIdx(), consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistMaze))) {
            return MistRetCode.MRC_CurrencyNotEnough;
        }
        RewardManager.getInstance().doReward(player.getIdx(), RewardUtil.parseAndMulti(shopSellCfg.getCargo(), 1),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistMaze, 1), true);
        SyncExecuteFunction.executeConsumer(player, entity -> {
            MazeBuyGoodsTimes.Builder timesBuilder = entity.getDb_data().getMazeDataBuilder().getBuyGoodsTimesBuilder();
            if (buyCount > 0) {
                boolean flag = false;
                for (int i = 0; i < timesBuilder.getGoodsIdCount(); i++) {
                    if (timesBuilder.getGoodsId(i) == goodsId) {
                        timesBuilder.setBuyTimes(i, buyCount + 1);
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    timesBuilder.addGoodsId(goodsId).addBuyTimes(1);
                }
            } else {
                timesBuilder.addGoodsId(goodsId).addBuyTimes(1);
            }
            SC_MistMazeBuyGoodsTimes.Builder builder = SC_MistMazeBuyGoodsTimes.newBuilder();
            builder.setMazeBuyGoodsTimes(timesBuilder);
            GlobalData.getInstance().sendMsg(entity.getIdx(), MsgIdEnum.SC_MistMazeBuyGoodsTimes_VALUE, builder);
        });
        return MistRetCode.MRC_Success;
    }
}
