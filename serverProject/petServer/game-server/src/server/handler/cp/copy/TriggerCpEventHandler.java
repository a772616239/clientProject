package server.handler.cp.copy;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.cp.CpCopyManger;
import protocol.Common.EnumFunction;
import protocol.CpFunction.CS_TriggerCpEvent;
import protocol.CpFunction.SC_TriggerCpEvent;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 申请加入组队
 */
@MsgId(msgId = MsgIdEnum.CS_TriggerCpEvent_VALUE)
public class TriggerCpEventHandler extends AbstractBaseHandler<CS_TriggerCpEvent> {
    @Override
    protected CS_TriggerCpEvent parse(byte[] bytes) throws Exception {
        return CS_TriggerCpEvent.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_TriggerCpEvent req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        SC_TriggerCpEvent.Builder msg = CpCopyManger.getInstance().triggerEvent(playerIdx, req.getPointId());
        gsChn.send(MsgIdEnum.SC_TriggerCpEvent_VALUE, msg);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.LtCp;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_TriggerCpEvent_VALUE, SC_TriggerCpEvent.newBuilder().setRetCode(retCode));
    }
}
