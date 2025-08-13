package model.warpServer.battleServer.handler.matchArena;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaManager;
import protocol.MatchArena.SC_MathArenaStartMatch;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaStartMatch;
import util.GameUtil;


@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaStartNormalMatch_VALUE)
public class MatchArenaStartNormalMatchHandler extends AbstractHandler<BS_GS_MatchArenaStartMatch> {
    @Override
    protected BS_GS_MatchArenaStartMatch parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaStartMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaStartMatch req, int i) {
        SC_MathArenaStartMatch.Builder resultBuilder = SC_MathArenaStartMatch.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(req.getRetCode()));

        GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_MathArenaStartMatch_VALUE, resultBuilder);

        MatchArenaManager.getInstance().addMatchingPlayer(req.getPlayerIdx());
    }
}
