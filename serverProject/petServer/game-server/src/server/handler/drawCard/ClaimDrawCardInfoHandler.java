package server.handler.drawCard;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.DrawCard.CS_ClaimDrawCardInfo;
import protocol.DrawCard.SC_ClaimDrawCardInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimDrawCardInfo_VALUE)
public class ClaimDrawCardInfoHandler extends AbstractBaseHandler<CS_ClaimDrawCardInfo> {
    @Override
    protected CS_ClaimDrawCardInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimDrawCardInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimDrawCardInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        playerEntity player = playerCache.getByIdx(playerIdx);
        SC_ClaimDrawCardInfo.Builder resultBuilder = SC_ClaimDrawCardInfo.newBuilder();
        if (player == null) {
            LogUtil.error("playerIdx [" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimDrawCardInfo_VALUE, resultBuilder);
            return;
        }

        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.DrawCard)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_ClaimDrawCardInfo_VALUE, resultBuilder);
            return;
        }

        player.sendDrawCardInfo();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NullFuntion;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        doAction(gsChn, codeNum);
    }
}
