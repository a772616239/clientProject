package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_ClearReceiveCpInviteList;
import protocol.CpFunction.SC_ClearReceiveCpInviteList;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 清空组队邀请
 */
@MsgId(msgId = MsgIdEnum.CS_ClearReceiveCpInviteList_VALUE)
public class ClearReceiveCpInviteHandler extends AbstractBaseHandler<CS_ClearReceiveCpInviteList> {
    @Override
    protected CS_ClearReceiveCpInviteList parse(byte[] bytes) throws Exception {
        return CS_ClearReceiveCpInviteList.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClearReceiveCpInviteList req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().clearReceiveCpInvite(playerIdx);
        SC_ClearReceiveCpInviteList.Builder msg = SC_ClearReceiveCpInviteList.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ClearReceiveCpInviteList_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClearReceiveCpInviteList_VALUE, SC_ClearReceiveCpInviteList.newBuilder().setRetCode(retCode));
    }
}
