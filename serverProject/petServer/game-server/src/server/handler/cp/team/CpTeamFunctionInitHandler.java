package server.handler.cp.team;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpCopyManger;
import model.player.util.PlayerUtil;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.CpFunction;
import protocol.CpFunction.CS_CpTeamFunctionInit;
import protocol.CpFunction.SC_CpTeamFunctionInit;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 组队初始化消息
 */
@MsgId(msgId = MsgIdEnum.CS_CpTeamFunctionInit_VALUE)
public class CpTeamFunctionInitHandler extends AbstractBaseHandler<CS_CpTeamFunctionInit> {
    @Override
    protected CS_CpTeamFunctionInit parse(byte[] bytes) throws Exception {
        return CS_CpTeamFunctionInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpTeamFunctionInit req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        if (PlayerUtil.queryFunctionLock(playerIdx, Common.EnumFunction.LtCp)) {
            CpFunction.SC_CpTeamFunctionInit.Builder msg = CpFunction.SC_CpTeamFunctionInit.newBuilder();
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_FunctionIsLock));
            GlobalData.getInstance().sendMsg(playerIdx, MessageId.MsgIdEnum.SC_CpTeamFunctionInit_VALUE, msg);
            return;
        }
        CpCopyManger.getInstance().sendCopyInit(playerIdx);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpTeamFunctionInit_VALUE, SC_CpTeamFunctionInit.newBuilder().setRetCode(retCode));
    }
}
