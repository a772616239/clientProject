package server.handler.feats;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import protocol.TargetSystem.SC_GetFeatsInfo;
import util.GameUtil;


@MsgId(msgId = MsgIdEnum.CS_GetFeatsInfo_VALUE)
public class GetFeatsInfoHandler extends AbstractBaseHandler<TargetSystem.CS_GetFeatsInfo> {
    @Override
    protected TargetSystem.CS_GetFeatsInfo parse(byte[] bytes) throws Exception {
        return TargetSystem.CS_GetFeatsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, TargetSystem.CS_GetFeatsInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        TargetSystem.SC_GetFeatsInfo.Builder resultBuilder = TargetSystem.SC_GetFeatsInfo.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_GetFeatsInfo_VALUE, resultBuilder);
            return;
        }
        target.sendFeats();
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Feats;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_GetFeatsInfo_VALUE, SC_GetFeatsInfo.newBuilder().setRetCode(retCode));
    }
}
