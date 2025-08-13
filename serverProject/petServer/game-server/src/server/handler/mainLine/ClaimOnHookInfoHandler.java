package server.handler.mainLine;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import protocol.Common.EnumFunction;
import protocol.MainLine.CS_ClaimOnHookInfo;
import protocol.MainLine.SC_ClaimOnHookInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimOnHookInfo_VALUE)
public class ClaimOnHookInfoHandler extends AbstractBaseHandler<CS_ClaimOnHookInfo> {
    @Override
    protected CS_ClaimOnHookInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimOnHookInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimOnHookInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimOnHookInfo.Builder resultBuilder = SC_ClaimOnHookInfo.newBuilder();
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("ClaimMainLineHandler, playerIdx[" + playerIdx + "] mainLineEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimOnHookInfo_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            resultBuilder.addAllGainReward(entity.getOnHookInCome(false));
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimOnHookInfo_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimOnHookInfo_VALUE, SC_ClaimOnHookInfo.newBuilder().setRetCode(retCode));
    }
}
