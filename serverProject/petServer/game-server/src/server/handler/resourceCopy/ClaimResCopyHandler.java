package server.handler.resourceCopy;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.ResourceCopy.CS_ClaimResCopy;
import protocol.ResourceCopy.ResCopy;
import protocol.ResourceCopy.SC_ClaimResCopy;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimResCopy_VALUE)
public class ClaimResCopyHandler extends AbstractBaseHandler<CS_ClaimResCopy> {
    @Override
    protected CS_ClaimResCopy parse(byte[] bytes) throws Exception {
        return CS_ClaimResCopy.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimResCopy req, int i) {
        SC_ClaimResCopy.Builder resultBuilder = SC_ClaimResCopy.newBuilder();

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("ClaimResCopyHandler, playerIdx = " + playerIdx + ", entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimResCopy_VALUE, resultBuilder);
            return;
        }

        if (!player.functionUnLock(EnumFunction.ResCopy)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gsChn.send(MsgIdEnum.SC_ClaimResCopy_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            player.checkResCopy();

            List<ResCopy> copies = player.buildResCopies();
            if (copies == null || copies.isEmpty()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            } else {
                resultBuilder.addAllResCopyData(copies);
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            }
            gsChn.send(MsgIdEnum.SC_ClaimResCopy_VALUE, resultBuilder);
        });
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ResCopy;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimResCopy_VALUE, SC_ClaimResCopy.newBuilder().setRetCode(retCode));
    }
}
