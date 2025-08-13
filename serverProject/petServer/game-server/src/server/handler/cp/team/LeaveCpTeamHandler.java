package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_LeaveCPTeam;
import protocol.CpFunction.SC_LeaveCPTeam;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 离开队伍
 */
@MsgId(msgId = MsgIdEnum.CS_LeaveCPTeam_VALUE)
public class LeaveCpTeamHandler extends AbstractBaseHandler<CS_LeaveCPTeam> {
    @Override
    protected CS_LeaveCPTeam parse(byte[] bytes) throws Exception {
        return CS_LeaveCPTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_LeaveCPTeam req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().leaveTeam(playerIdx);
        SC_LeaveCPTeam.Builder msg = SC_LeaveCPTeam.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_LeaveCPTeam_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_LeaveCPTeam_VALUE, SC_LeaveCPTeam.newBuilder().setRetCode(retCode));
    }
}
