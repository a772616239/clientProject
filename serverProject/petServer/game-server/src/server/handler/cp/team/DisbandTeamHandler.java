package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_DisbandCPTeam;
import protocol.CpFunction.SC_DisbandCPTeam;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 解散编队
 */
@MsgId(msgId = MsgIdEnum.CS_DisbandCPTeam_VALUE)
public class DisbandTeamHandler extends AbstractBaseHandler<CS_DisbandCPTeam> {
    @Override
    protected CS_DisbandCPTeam parse(byte[] bytes) throws Exception {
        return CS_DisbandCPTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DisbandCPTeam req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().checkAndDisbandTeam(playerIdx);
        SC_DisbandCPTeam.Builder msg = SC_DisbandCPTeam.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_DisbandCPTeam_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_DisbandCPTeam_VALUE, SC_DisbandCPTeam.newBuilder().setRetCode(retCode));
    }
}
