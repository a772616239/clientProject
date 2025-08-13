package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_StartAlchemy;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_StartAlchemy;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_StartAlchemy_VALUE)
public class StartAlchemyHandler extends AbstractBaseHandler<CS_StartAlchemy> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_StartAlchemy_VALUE, SC_StartAlchemy.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }

    @Override
    protected CS_StartAlchemy parse(byte[] bytes) throws Exception {
        return CS_StartAlchemy.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_StartAlchemy req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_StartAlchemy_VALUE, req.toByteString(), true);
    }
}
