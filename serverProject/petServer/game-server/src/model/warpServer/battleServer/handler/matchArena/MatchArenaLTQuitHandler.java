package model.warpServer.battleServer.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.ServerTransfer.BS_GS_MatchArenaLTQuit;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaLTQuit_VALUE)
public class MatchArenaLTQuitHandler extends AbstractHandler<BS_GS_MatchArenaLTQuit> {
    @Override
    protected BS_GS_MatchArenaLTQuit parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaLTQuit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaLTQuit req, int i) {
        MatchArenaLTManager.getInstance().quitLeiTaiBSBack(req.getPlayerId(), req.getDefTime(), req.getRetCode());
    }
}
