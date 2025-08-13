package server.handler.mistforest;

import cfg.MistItem;
import cfg.MistItemObject;
import cfg.ShopSell;
import cfg.ShopSellObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.mistforest.MistForestManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_BuyMistGoods;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistItemInfo;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_BuyMistGoods;
import protocol.ServerTransfer.GS_CS_BuyMistGoods;
import protocol.TransServerCommon.PlayerMistServerInfo;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_BuyMistGoods_VALUE)
public class BuyMistGoodsHandler extends AbstractBaseHandler<CS_BuyMistGoods> {
    @Override
    protected CS_BuyMistGoods parse(byte[] bytes) throws Exception {
        return CS_BuyMistGoods.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyMistGoods req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }

        MistRetCode retCode = req.getGoodsType() == 0 ? checkBuyMistGoods(player, req) : checkCommonGoods(player, req);
        if (retCode != MistRetCode.MRC_Success) {
            SC_BuyMistGoods.Builder retBuilder = SC_BuyMistGoods.newBuilder();
            retBuilder.setRet(retCode);
            GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_BuyMistGoods_VALUE, retBuilder);
            return;
        }

        GS_CS_BuyMistGoods.Builder builder = GS_CS_BuyMistGoods.newBuilder();
        builder.setPlayerIdx(playerId);
        builder.setGoodsId(req.getGoodsId());
        builder.setShopPos(req.getShopPos());
        builder.setGoodsType(req.getGoodsType());
        if (!CrossServerManager.getInstance().sendMsgToMistForest(playerId, MsgIdEnum.GS_CS_BuyMistGoods_VALUE, builder, true)) {
            SC_BuyMistGoods.Builder retBuilder = SC_BuyMistGoods.newBuilder();
            retBuilder.setRet(MistRetCode.MRC_InMistForest);
            GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_BuyMistGoods_VALUE, retBuilder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_BuyMistGoods_VALUE,
                SC_BuyMistGoods.newBuilder().setRet(MistRetCode.MRC_AbnormalMaintenance));
    }

    protected MistRetCode checkBuyMistGoods(playerEntity player, CS_BuyMistGoods req) {
        MistItemObject mistItem = MistItem.getByItemid(req.getGoodsId());
        if (mistItem == null || mistItem.getItemprice().length < 3) {
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

        if (!player.currencyIsEnough(RewardTypeEnum.forNumber(currencyType), price)) {
            return MistRetCode.MRC_CurrencyNotEnough;
        }

        PlayerMistServerInfo playerSvrData = CrossServerManager.getInstance().getMistForestPlayerServerInfo(player.getIdx());
        if (playerSvrData == null) {
            return MistRetCode.MRC_NotInMistForest;
        }
        if (playerSvrData.getMistRule() == EnumMistRuleKind.EMRK_Maze && !MistForestManager.getInstance().getMazeManager().isOpen()) {
            return MistRetCode.MRC_MazeActivityNotOpen;
        }
        return MistRetCode.MRC_Success;
    }

    protected MistRetCode checkCommonGoods(playerEntity player, CS_BuyMistGoods req) {
        ShopSellObject shopSellCfg = ShopSell.getById(req.getGoodsId());
        if (shopSellCfg == null) {
            return MistRetCode.MRC_ItemNotExist;
        }
        int buyCount = player.getMazeBuyGoodsTimes(req.getGoodsId());
        if (buyCount >= shopSellCfg.getBuylimit()) {
            return MistRetCode.MRC_GoodsSoldOut;
        }
        Consume consume = ConsumeUtil.parseAndMulti(shopSellCfg.getPrice(), 1);
        if (consume == null) {
            return MistRetCode.MRC_NotSellInThisShop;
        }
        if (!ConsumeManager.getInstance().materialIsEnough(player.getIdx(), consume)) {
            return MistRetCode.MRC_CurrencyNotEnough;
        }
        PlayerMistServerInfo playerSvrData = CrossServerManager.getInstance().getMistForestPlayerServerInfo(player.getIdx());
        if (playerSvrData == null) {
            return MistRetCode.MRC_NotInMistForest;
        }
        if (playerSvrData.getMistRule() == EnumMistRuleKind.EMRK_Maze && !MistForestManager.getInstance().getMazeManager().isOpen()) {
            return MistRetCode.MRC_MazeActivityNotOpen;
        }
        return MistRetCode.MRC_Success;
    }
}
