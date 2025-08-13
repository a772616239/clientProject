package server.handler.cp.copy;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpCopyManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_CpRevive;
import protocol.CpFunction.SC_CpRevive;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 玩家复活
 */
@MsgId(msgId = MsgIdEnum.CS_CpRevive_VALUE)
public class PlayerReviveHandler extends AbstractBaseHandler<CS_CpRevive> {
    @Override
    protected CS_CpRevive parse(byte[] bytes) throws Exception {
        return CS_CpRevive.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CpRevive req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        RetCodeEnum retCodeEnum = CpCopyManger.getInstance().playerRevive(playerIdx,req.getPay());
        SC_CpRevive.Builder msg = SC_CpRevive.newBuilder();
        msg.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_CpRevive_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CpRevive_VALUE, SC_CpRevive.newBuilder().setRetCode(retCode));
    }
}
