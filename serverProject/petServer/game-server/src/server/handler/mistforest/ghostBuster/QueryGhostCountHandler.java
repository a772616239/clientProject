package server.handler.mistforest.ghostBuster;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_GhostBusterGhostCount;
import protocol.MistForest.SC_GhostBusterGhostCount;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_GhostBusterGhostCount_VALUE)
public class QueryGhostCountHandler extends AbstractBaseHandler<CS_GhostBusterGhostCount> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistGhostBuster;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_GhostBusterGhostCount_VALUE, SC_GhostBusterGhostCount.newBuilder());
    }

    @Override
    protected CS_GhostBusterGhostCount parse(byte[] bytes) throws Exception {
        return CS_GhostBusterGhostCount.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GhostBusterGhostCount req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_GhostBusterGhostCount_VALUE, req.toByteString(), true);
    }
}
