package server.handler.cp.team;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpTeamManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CpAutoMatch;
import protocol.CpFunction.SC_CpAutoMatch;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 自动匹配
 */
@MsgId(msgId = MsgIdEnum.CS_CpAutoMatch_VALUE)
public class CpAutoMatchHandler extends AbstractBaseHandler<CS_CpAutoMatch> {
    @Override
    protected CS_CpAutoMatch parse(byte[] bytes) throws Exception {
        return CS_CpAutoMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpAutoMatch req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpTeamManger.getInstance().autoMatchTeam(playerIdx);
        SC_CpAutoMatch.Builder msg = SC_CpAutoMatch.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_CpAutoMatch_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpAutoMatch_VALUE, SC_CpAutoMatch.newBuilder().setRetCode(retCode));
    }
}
