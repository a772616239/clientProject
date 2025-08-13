package server.handler.function;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.FunctionManager;
import protocol.Common;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_QueryFunctionSwitch_VALUE)
public class QueryFunctionSwitchHandler extends AbstractBaseHandler<Common.CS_QueryFunctionSwitch> {
    @Override
    protected Common.CS_QueryFunctionSwitch parse(byte[] bytes) throws Exception {
        return Common.CS_QueryFunctionSwitch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Common.CS_QueryFunctionSwitch req, int i) {
        Common.SC_QueryFunctionSwitch.Builder result = Common.SC_QueryFunctionSwitch.newBuilder();

        boolean open = FunctionManager.getInstance().functionOpening(req.getFunction());
        result.setResult(GameUtil.buildRetCode(getSwitchCode(open)));

        gsChn.send(MessageId.MsgIdEnum.SC_QueryFunctionSwitch_VALUE, result);
    }

    private RetCodeId.RetCodeEnum getSwitchCode(boolean functionOpen) {
        return functionOpen ? RetCodeId.RetCodeEnum.RCE_Success : RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance;
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
