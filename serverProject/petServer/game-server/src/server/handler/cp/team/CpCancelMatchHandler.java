package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CpCancelMatch;
import protocol.CpFunction.SC_CpCancelMatch;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 取消匹配
 */
@MsgId(msgId = MsgIdEnum.CS_CpCancelMatch_VALUE)
public class CpCancelMatchHandler extends AbstractBaseHandler<CS_CpCancelMatch> {
    @Override
    protected CS_CpCancelMatch parse(byte[] bytes) throws Exception {
        return CS_CpCancelMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpCancelMatch req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().cancelMatchPlayer(playerIdx);
        SC_CpCancelMatch.Builder msg = SC_CpCancelMatch.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_CpCancelMatch_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpCancelMatch_VALUE, SC_CpCancelMatch.newBuilder().setRetCode(retCode));
    }
}
