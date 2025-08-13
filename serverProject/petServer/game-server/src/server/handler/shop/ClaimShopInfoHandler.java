package server.handler.shop;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.shop.StoreManager;
import model.shop.dbCache.shopCache;
import model.shop.entity.shopEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Shop.CS_ClaimShopInfo;
import protocol.Shop.SC_ClaimShopInfo;
import protocol.Shop.ShopInfo;
import protocol.Shop.ShopTypeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimShopInfo_VALUE)
public class ClaimShopInfoHandler extends AbstractBaseHandler<CS_ClaimShopInfo> {
    @Override
    protected CS_ClaimShopInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimShopInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimShopInfo req, int i) {
        SC_ClaimShopInfo.Builder resultBuilder = SC_ClaimShopInfo.newBuilder();
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        RetCodeEnum beforeResult = beforeCheck(playerIdx, req.getShopType());
        if (beforeResult != RetCodeEnum.RCE_Success) {
            LogUtil.info("function not open");
            resultBuilder.setRetCode(GameUtil.buildRetCode(beforeResult));
            gsChn.send(MsgIdEnum.SC_ClaimShopInfo_VALUE, resultBuilder);
            return;
        }

        shopEntity entity = shopCache.getInstance().getEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimShopInfo_VALUE, resultBuilder);
            return;
        }

        ShopInfo shopInfo = SyncExecuteFunction.executeFunction(entity, e -> entity.buildPlayerShowShopInfo(req.getShopType()));
        if (shopInfo == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimShopInfo_VALUE, resultBuilder);
        } else {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setShopInfo(shopInfo);
            gsChn.send(MsgIdEnum.SC_ClaimShopInfo_VALUE, resultBuilder);
        }
    }

    private RetCodeEnum beforeCheck(String playerIdx, ShopTypeEnum shopType) {
        if (playerIdx == null || shopType == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (!StoreManager.storeIsUnlock(playerIdx, shopType)) {
            return RetCodeEnum.RCE_FunctionIsLock;
        }

        return RetCodeEnum.RCE_Success;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Shop;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimShopInfo_VALUE, SC_ClaimShopInfo.newBuilder().setRetCode(retCode));
    }
}
