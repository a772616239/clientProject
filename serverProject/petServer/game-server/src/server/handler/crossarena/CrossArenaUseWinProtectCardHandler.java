package server.handler.crossarena;

import common.AbstractBaseHandler;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_UseWinProtectCard;
import protocol.CrossArena.SC_UseWinProtectCard;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_UseWinProtectCard_VALUE;

@MsgId(msgId = MsgIdEnum.CS_UseWinProtectCard_VALUE)
public class CrossArenaUseWinProtectCardHandler extends AbstractBaseHandler<CS_UseWinProtectCard> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_UseWinProtectCard.Builder resultBuilder = SC_UseWinProtectCard.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(SC_UseWinProtectCard_VALUE, resultBuilder);
    }

    @Override
    protected CS_UseWinProtectCard parse(byte[] bytes) throws Exception {
        return CS_UseWinProtectCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UseWinProtectCard req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        RetCodeEnum retCodeEnum = CrossArenaManager.getInstance().useProtectCard(playerIdx);
        SC_UseWinProtectCard.Builder msg = SC_UseWinProtectCard.newBuilder().setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(SC_UseWinProtectCard_VALUE, msg);
    }
}
