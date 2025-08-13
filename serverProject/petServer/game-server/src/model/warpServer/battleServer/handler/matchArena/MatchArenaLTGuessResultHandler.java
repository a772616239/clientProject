package model.warpServer.battleServer.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaLTGuessResult;

@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaLTGuessResult_VALUE)
public class MatchArenaLTGuessResultHandler extends AbstractHandler<BS_GS_MatchArenaLTGuessResult> {
    @Override
    protected BS_GS_MatchArenaLTGuessResult parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaLTGuessResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaLTGuessResult req, int i) {
        MatchArenaLTManager.getInstance().guessResult(req.getWinIdsList());
    }
}
