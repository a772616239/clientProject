package server.handler.PurchaseCard;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common;
import protocol.MessageId;
import protocol.MonthCard;
import protocol.PlayerDB;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimPrivilegedCard_VALUE)
public class ClaimPrivilegedCardHandler extends AbstractBaseHandler<MonthCard.CS_ClaimPrivilegedCard> {
    @Override
    protected MonthCard.CS_ClaimPrivilegedCard parse(byte[] bytes) throws Exception {
        return MonthCard.CS_ClaimPrivilegedCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, MonthCard.CS_ClaimPrivilegedCard req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        MonthCard.SC_ClaimPrivilegedCard.Builder msg = MonthCard.SC_ClaimPrivilegedCard.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimPrivilegedCard_VALUE, msg);
            return;
        }
        for (PlayerDB.DB_PrivilegedCard rechargeCardInfo : player.getDb_data().getRechargeCards().getPrivilegedCardList()) {
            msg.addPrivilegedCards(MonthCard.PrivilegedCard.newBuilder().setCardId(rechargeCardInfo.getCarId())
                    .setRemainDays(rechargeCardInfo.getRemainDays()).build());
        }
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimPrivilegedCard_VALUE, msg);
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}