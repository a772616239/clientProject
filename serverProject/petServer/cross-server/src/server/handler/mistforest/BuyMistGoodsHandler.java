package server.handler.mistforest;

import cfg.MistShop;
import cfg.MistShopObject;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.mistforest.MistConst;
import model.mistforest.map.grid.Grid;
import model.mistforest.map.grid.ShopGrid;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistRetCode;
import protocol.ServerTransfer.CS_GS_BuyMistGoods;
import protocol.ServerTransfer.GS_CS_BuyMistGoods;

@MsgId(msgId = MsgIdEnum.GS_CS_BuyMistGoods_VALUE)
public class BuyMistGoodsHandler extends AbstractHandler<GS_CS_BuyMistGoods> {
    @Override
    protected GS_CS_BuyMistGoods parse(byte[] bytes) throws Exception {
        return GS_CS_BuyMistGoods.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_BuyMistGoods req, int i) {
        CS_GS_BuyMistGoods.Builder retBuilder = CS_GS_BuyMistGoods.newBuilder();
        retBuilder.setPlayerIdx(req.getPlayerIdx());
        retBuilder.setGoodsId(req.getGoodsId());
        retBuilder.setGoodsType(req.getGoodsType());
        MistPlayer player = MistPlayerCache.getInstance().queryObject(req.getPlayerIdx());
        if (player == null) {
            retBuilder.setRetCode(MistRetCode.MRC_NotInMistForest);
            gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        MistRoom room = player.getMistRoom();
        if (room == null) {
            retBuilder.setRetCode(MistRetCode.MRC_NotInMistForest);
            gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        MistFighter fighter = room.getObjManager().getMistObj(player.getFighterId());
        if (fighter == null) {
            retBuilder.setRetCode(MistRetCode.MRC_NotInMistForest);
            gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        if (fighter.isBattling()) {
            retBuilder.setRetCode(MistRetCode.MRC_Battling);
            gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        if (fighter.getSkillMachine().isItemSkillFull()) {
            retBuilder.setRetCode(MistRetCode.MRC_ItemFull);
            gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        int targetPosX = req.getShopPos().getX() / 1000;
        int targetPosY = req.getShopPos().getY() / 1000;
        if (!MistConst.checkInRoughDistance(MistConst.MistTouchShopMaxDistance ,
                targetPosX, targetPosY, fighter.getPos().getX(), fighter.getPos().getY())) {
            retBuilder.setRetCode(MistRetCode.MRC_TooFarToShop);
            gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        Grid grid = room.getWorldMap().getGridByPos(targetPosX, targetPosY);
        if (!(grid instanceof ShopGrid)) {
            retBuilder.setRetCode(MistRetCode.MRC_NotFoundShop);
            gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        ShopGrid shopGrid = (ShopGrid) grid;
        MistShopObject shopCfg = MistShop.getById(shopGrid.getShopId());
        if (shopCfg == null || !shopCfg.checkSellingGoods(req.getGoodsType(), req.getGoodsId())) {
            retBuilder.setRetCode(MistRetCode.MRC_NotSellInThisShop);
            gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
            return;
        }
        retBuilder.setRetCode(MistRetCode.MRC_Success);
        gsChn.send(MsgIdEnum.CS_GS_BuyMistGoods_VALUE, retBuilder);
    }
}
