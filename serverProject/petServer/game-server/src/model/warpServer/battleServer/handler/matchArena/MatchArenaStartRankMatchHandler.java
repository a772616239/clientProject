package model.warpServer.battleServer.handler.matchArena;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaManager;
import protocol.MatchArena.SC_MathArenaStartMatch;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaStartRankMatch;
import util.GameUtil;


@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaStartRankMatch_VALUE)
public class MatchArenaStartRankMatchHandler extends AbstractHandler<BS_GS_MatchArenaStartRankMatch> {
    @Override
    protected BS_GS_MatchArenaStartRankMatch parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaStartRankMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaStartRankMatch req, int i) {
        SC_MathArenaStartMatch.Builder resultBuilder = SC_MathArenaStartMatch.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(req.getRetCode()));

        GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);

        MatchArenaManager.getInstance().addMatchingPlayer(req.getPlayerIdx());
    }
}
