package model.warpServer.battleServer.handler.matchArena;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaManager;
import protocol.MatchArena.SC_MatchArenaCancelMatch;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaCancelMatch;
import util.GameUtil;

/**
 * @author huhan
 * @date 2021/05/25
 */
@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaCancelMatch_VALUE)
public class MatchArenaCancelMatchHandler extends AbstractHandler<BS_GS_MatchArenaCancelMatch> {
    @Override
    protected BS_GS_MatchArenaCancelMatch parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaCancelMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaCancelMatch req, int i) {
        SC_MatchArenaCancelMatch.Builder resultBuilder = SC_MatchArenaCancelMatch.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(req.getRetCode()));

        GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_MatchArenaCancelMatch_VALUE, resultBuilder);


    }
}
