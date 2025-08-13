package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_InvitePlayerJoinTeam;
import protocol.CpFunction.SC_InvitePlayerJoinTeam;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 邀请玩家加入队伍
 */
@MsgId(msgId = MsgIdEnum.CS_InvitePlayerJoinTeam_VALUE)
public class InvitePlayerJoinTeamHandler extends AbstractBaseHandler<CS_InvitePlayerJoinTeam> {
    @Override
    protected CS_InvitePlayerJoinTeam parse(byte[] bytes) throws Exception {
        return CS_InvitePlayerJoinTeam.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_InvitePlayerJoinTeam req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().invitePlayerJoinTeam(playerIdx,req.getPlayerIdx());
        SC_InvitePlayerJoinTeam.Builder msg = SC_InvitePlayerJoinTeam.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_InvitePlayerJoinTeam_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_InvitePlayerJoinTeam_VALUE, SC_InvitePlayerJoinTeam.newBuilder().setRetCode(retCode));
    }
}
