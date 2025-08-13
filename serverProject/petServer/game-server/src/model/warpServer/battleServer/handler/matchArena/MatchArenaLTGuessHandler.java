package model.warpServer.battleServer.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.ServerTransfer.BS_GS_MatchArenaLTGuess;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaLTGuess_VALUE)
public class MatchArenaLTGuessHandler extends AbstractHandler<BS_GS_MatchArenaLTGuess> {
    @Override
    protected BS_GS_MatchArenaLTGuess parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaLTGuess.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaLTGuess req, int i) {
        MatchArenaLTManager.getInstance().guessBSBack(req.getPlayerId(), req.getRetCode());
    }
}
