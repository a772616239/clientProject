package server.handler.gift;

import protocol.Common.EnumFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.exchangehistory.dbCache.service.ExchangeHistoryServiceImpl;
import protocol.ExchangeHistory.CS_GiftPurchase;
import protocol.ExchangeHistory.SC_GiftPurchase;
import protocol.ExchangeHistoryDB.ExchangeStoreEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import common.AbstractBaseHandler;
import util.LogUtil;
import static protocol.MessageId.MsgIdEnum.CS_GiftPurchase_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_GiftPurchase_VALUE;

/**
 * 购买礼包处理消息
 *
 * @author xiao_FL
 * @date 2019/11/12
 */
@MsgId(msgId = CS_GiftPurchase_VALUE)
public class GiftHandler extends AbstractBaseHandler<CS_GiftPurchase> {
    @Override
    protected CS_GiftPurchase parse(byte[] bytes) throws Exception {
        return CS_GiftPurchase.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_GiftPurchase csGiftPurchase, int i) {
        LogUtil.info("recv petBagEnlarge msg:" + csGiftPurchase.toString());
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            RetCodeEnum operate;
            if (csGiftPurchase.getType() == 1) {
                operate = ExchangeHistoryServiceImpl.getInstance().exchangeGift(ExchangeStoreEnum.WORTHY_GIFT, playerId, csGiftPurchase.getGoodId(), 1);
            } else {
                operate = ExchangeHistoryServiceImpl.getInstance().exchangeGift(ExchangeStoreEnum.LIMIT_GIFT, playerId, csGiftPurchase.getGoodId(), 1);
            }
            RetCode retCode;
            if (operate == RetCodeEnum.RCE_Success) {
                retCode = RetCode.newBuilder().setRetCode(RetCodeEnum.RCE_Success).build();
            } else {
                retCode = RetCode.newBuilder().setRetCode(operate).build();
            }
            SC_GiftPurchase.Builder result = SC_GiftPurchase.newBuilder().setResult(retCode);
            gameServerTcpChannel.send(SC_GiftPurchase_VALUE, result);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
