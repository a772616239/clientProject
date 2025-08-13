package server.handler.activity.pointCopy;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.CS_ClaimPointCopyInfo;
import protocol.Activity.SC_ClaimPointCopyInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimPointCopyInfo_VALUE)
public class ClaimPointCopyInfoHandler extends AbstractBaseHandler<CS_ClaimPointCopyInfo> {
    @Override
    protected CS_ClaimPointCopyInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimPointCopyInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimPointCopyInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_ClaimPointCopyInfo.Builder resultBuilder = SC_ClaimPointCopyInfo.newBuilder();
        if (target == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimPointCopyInfo_VALUE, resultBuilder);
            return;
        }

        target.sendPointCopyInfo();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PointCopy;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimPointCopyInfo_VALUE, SC_ClaimPointCopyInfo.newBuilder().setRetCode(retCode));
    }
}
