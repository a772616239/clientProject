package server.handler.mistforest;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_ClientEventInvoke;
import util.GameUtil;


@MsgId(msgId = MsgIdEnum.CS_ClientEventInvoke_VALUE)
public class ClientMistEventHandler extends AbstractBaseHandler<CS_ClientEventInvoke> {
    @Override
    protected CS_ClientEventInvoke parse(byte[] bytes) throws Exception {
        return CS_ClientEventInvoke.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClientEventInvoke req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_ClientEventInvoke_VALUE, req.toByteString(), true);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
//        gsChn.send(MsgIdEnum.SC_,
//                SC_ApplyExchangeMistRoom.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }
}
