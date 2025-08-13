package server.handler.mistforest.ghostBuster;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.warpServer.crossServer.CrossServerManager;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_GhostBusterRankData;
import protocol.MistForest.SC_GhostBusterRankData;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_GhostBusterRankData_VALUE)
public class QueryGhostScoreRankHandler extends AbstractBaseHandler<CS_GhostBusterRankData> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistGhostBuster;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_GhostBusterRankData_VALUE, SC_GhostBusterRankData.newBuilder());
    }

    @Override
    protected CS_GhostBusterRankData parse(byte[] bytes) throws Exception {
        return CS_GhostBusterRankData.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GhostBusterRankData req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        CrossServerManager.getInstance().transferMsgToMistForest(
                playerId, MsgIdEnum.CS_GhostBusterRankData_VALUE, req.toByteString(), true);
    }
}
