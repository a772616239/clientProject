package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_ApplyJoinCPTeam;
import protocol.CpFunction.SC_ApplyJoinCPTeam;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 申请加入组队
 */
@MsgId(msgId = MsgIdEnum.CS_ApplyJoinCPTeam_VALUE)
public class ApplyJoinTeamHandler extends AbstractBaseHandler<CS_ApplyJoinCPTeam> {
    @Override
    protected CS_ApplyJoinCPTeam parse(byte[] bytes) throws Exception {
        return CS_ApplyJoinCPTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ApplyJoinCPTeam req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().checkAndApplyJoin(playerIdx, req);
        SC_ApplyJoinCPTeam.Builder msg = SC_ApplyJoinCPTeam.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ApplyJoinCPTeam_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ApplyJoinCPTeam_VALUE, SC_ApplyJoinCPTeam.newBuilder().setRetCode(retCode));
    }
}
