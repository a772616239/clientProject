package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_ClaimMyCpTeam;
import protocol.CpFunction.SC_ClaimMyCpTeam;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 获取我的组队信息
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimMyCpTeam_VALUE)
public class ClaimMyCpTeamHandler extends AbstractBaseHandler<CS_ClaimMyCpTeam> {
    @Override
    protected CS_ClaimMyCpTeam parse(byte[] bytes) throws Exception {
        return CS_ClaimMyCpTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMyCpTeam req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        CpTeamManger.getInstance().sendMyTeamInfo(playerIdx);

    }
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimMyCpTeam_VALUE, SC_ClaimMyCpTeam.newBuilder());
    }
}
