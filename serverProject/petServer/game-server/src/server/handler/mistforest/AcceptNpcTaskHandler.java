package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_AcceptNpcTask;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_AcceptNpcTask;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_AcceptNpcTask_VALUE)
public class AcceptNpcTaskHandler extends AbstractBaseHandler<CS_AcceptNpcTask> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_AcceptNpcTask_VALUE, SC_AcceptNpcTask.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }

    @Override
    protected CS_AcceptNpcTask parse(byte[] bytes) throws Exception {
        return CS_AcceptNpcTask.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_AcceptNpcTask req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_AcceptNpcTask_VALUE, req.toByteString(), true);
    }
}
