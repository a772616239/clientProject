package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_ClearApplyJoinTeamList;
import protocol.CpFunction.SC_ClearApplyJoinTeamList;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 队长清除申请加入组队消息
 */
@MsgId(msgId = MsgIdEnum.CS_ClearApplyJoinTeamList_VALUE)
public class ClearApplyJoinTeamHandler extends AbstractBaseHandler<CS_ClearApplyJoinTeamList> {
    @Override
    protected CS_ClearApplyJoinTeamList parse(byte[] bytes) throws Exception {
        return CS_ClearApplyJoinTeamList.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClearApplyJoinTeamList req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().clearApplyJoinTeam(playerIdx);
        SC_ClearApplyJoinTeamList.Builder msg = SC_ClearApplyJoinTeamList.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ClearApplyJoinTeamList_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClearApplyJoinTeamList_VALUE, SC_ClearApplyJoinTeamList.newBuilder().setRetCode(retCode));
    }
}
