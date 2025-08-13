package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CpKickOutTeammate;
import protocol.CpFunction.SC_CpKickOutTeammate;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 踢人
 */
@MsgId(msgId = MsgIdEnum.CS_CpKickOutTeammate_VALUE)
public class KickOutPlayerHandler extends AbstractBaseHandler<CS_CpKickOutTeammate> {
    @Override
    protected CS_CpKickOutTeammate parse(byte[] bytes) throws Exception {
        return CS_CpKickOutTeammate.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpKickOutTeammate req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().kickOutTeammate(playerIdx,req.getPlayerIdx());
        SC_CpKickOutTeammate.Builder msg = SC_CpKickOutTeammate.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_CpKickOutTeammate_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpKickOutTeammate_VALUE, SC_CpKickOutTeammate.newBuilder().setRetCode(retCode));
    }
}
