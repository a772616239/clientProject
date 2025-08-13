package model.warpServer.battleServer.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaLTWinResult;

@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaLTWinResult_VALUE)
public class MatchArenaLTWinResultHandler extends AbstractHandler<BS_GS_MatchArenaLTWinResult> {
    @Override
    protected BS_GS_MatchArenaLTWinResult parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaLTWinResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaLTWinResult req, int i) {
        MatchArenaLTManager.getInstance().battleWin(req);
    }
}
