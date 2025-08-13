package server.event.leitai;

import hyzNet.GameServerTcpChannel;
import model.matchArena.MatchArenaLTManager;
import protocol.ServerTransfer;

public class LeitaiGuessEvent extends LeitaiEventAbstractCommand {

    private ServerTransfer.GS_BS_MatchArenaLTGuess req;
    private GameServerTcpChannel gsChn;

    public void setReq(ServerTransfer.GS_BS_MatchArenaLTGuess req) {
        this.req = req;
    }

    public void setGsChn(GameServerTcpChannel gsChn) {
        this.gsChn = gsChn;
    }

    @Override
    public void doAction() {
        MatchArenaLTManager.getInstance().guess(gsChn, req.getPlayerId(), req.getLeitaiId(), req.getIsWin(), req.getSvrIndex());
    }
}
