package server.handler.gift;

import protocol.Common.EnumFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.exchangehistory.dbCache.service.ExchangeHistoryServiceImpl;
import model.exchangehistory.entity.ExchangeHistoryResult;
import protocol.ExchangeHistory.CS_GiftQuery;
import protocol.ExchangeHistory.SC_GiftQuery;
import protocol.ExchangeHistoryDB.ExchangeStoreEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import common.AbstractBaseHandler;
import util.LogUtil;
import static protocol.MessageId.MsgIdEnum.CS_GiftQuery_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_GiftQuery_VALUE;

/**
 * 查询礼包购买记录消息
 *
 * @author xiao_FL
 * @date 2019/11/12
 */
@MsgId(msgId = CS_GiftQuery_VALUE)
public class GiftRecordHandler extends AbstractBaseHandler<CS_GiftQuery> {
    @Override
    protected CS_GiftQuery parse(byte[] bytes) throws Exception {
        return CS_GiftQuery.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_GiftQuery csGiftQuery, int i) {
        LogUtil.info("recv petBagEnlarge msg:" + csGiftQuery.toString());
        try {
            // 获取当前channel对应playerId
            String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
            ExchangeHistoryResult exchangeHistoryResult;
            if (csGiftQuery.getType() == 1) {
                exchangeHistoryResult = ExchangeHistoryServiceImpl.getInstance().queryStoreHistory(playerId, ExchangeStoreEnum.WORTHY_GIFT);
            } else {
                exchangeHistoryResult = ExchangeHistoryServiceImpl.getInstance().queryStoreHistory(playerId, ExchangeStoreEnum.LIMIT_GIFT);
            }
            SC_GiftQuery.Builder result = SC_GiftQuery.newBuilder();
            RetCode.Builder retCode = RetCode.newBuilder();
            if (exchangeHistoryResult.isSuccess()) {
                result.addAllRecord(exchangeHistoryResult.getGoodsInfoList());
                retCode.setRetCode(RetCodeEnum.RCE_Success);
            } else {
                retCode.setRetCode(exchangeHistoryResult.getCode());
            }
            result.setResult(retCode);
            gameServerTcpChannel.send(SC_GiftQuery_VALUE, result);
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
