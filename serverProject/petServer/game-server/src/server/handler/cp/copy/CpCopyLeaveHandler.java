package server.handler.cp.copy;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpCopyManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CpCopyLeaveOut;
import protocol.CpFunction.SC_CpCopyLeaveOut;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 玩家离开组队
 */
@MsgId(msgId = MsgIdEnum.CS_CpCopyLeaveOut_VALUE)
public class CpCopyLeaveHandler extends AbstractBaseHandler<CS_CpCopyLeaveOut> {
    @Override
    protected CS_CpCopyLeaveOut parse(byte[] bytes) throws Exception {
        return CS_CpCopyLeaveOut.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpCopyLeaveOut req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpCopyManger.getInstance().playerLeaveOutCopy(playerIdx);
        SC_CpCopyLeaveOut.Builder msg = SC_CpCopyLeaveOut.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_CpCopyLeaveOut_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpCopyLeaveOut_VALUE, SC_CpCopyLeaveOut.newBuilder().setRetCode(retCode));
    }
}
