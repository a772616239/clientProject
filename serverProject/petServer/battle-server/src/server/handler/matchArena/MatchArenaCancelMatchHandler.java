package server.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matchArena.MatchArenaNormalManager;
import model.matchArena.MatchArenaRankManager;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaCancelMatch;
import protocol.ServerTransfer.GS_BS_MatchArenaCancelMatch;

/**
 * @author huhan
 * @date 2021/05/25
 */
@MsgId(msgId = MsgIdEnum.GS_BS_MatchArenaCancelMatch_VALUE)
public class MatchArenaCancelMatchHandler extends AbstractHandler<GS_BS_MatchArenaCancelMatch> {
    @Override
    protected GS_BS_MatchArenaCancelMatch parse(byte[] bytes) throws Exception {
        return GS_BS_MatchArenaCancelMatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_MatchArenaCancelMatch req, int i) {
        RetCodeEnum codeEnum = doCancelRankMatch(req);
        BS_GS_MatchArenaCancelMatch.Builder resultBuilder = BS_GS_MatchArenaCancelMatch.newBuilder();
        resultBuilder.setPlayerIdx(req.getPlayerIdx());
        resultBuilder.setRetCode(codeEnum);
        gsChn.send(MsgIdEnum.BS_GS_MatchArenaCancelMatch_VALUE, resultBuilder);
    }

    private RetCodeEnum doCancelRankMatch(GS_BS_MatchArenaCancelMatch req) {
        switch (req.getArenaType()) {
            //取消匹配赛
            case 1:
                return cancelNormalMatch(req);
            //取消排位赛
            case 2:
                return cancelRankMatch(req);
            default:
                cancelNormalMatch(req);
                cancelRankMatch(req);
                return RetCodeEnum.RCE_Success;
        }
    }

    private RetCodeEnum cancelRankMatch(GS_BS_MatchArenaCancelMatch req) {
       return MatchArenaRankManager.getInstance().cancelMatch(req.getPlayerIdx());
    }

    private RetCodeEnum cancelNormalMatch(GS_BS_MatchArenaCancelMatch req) {
        return  MatchArenaNormalManager.getInstance().cancelMatch(req.getPlayerIdx());
    }
}
