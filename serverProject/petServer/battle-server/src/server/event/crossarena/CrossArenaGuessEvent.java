package server.event.crossarena;

import hyzNet.GameServerTcpChannel;
import model.crossarena.CrossArenaManager;
import protocol.ServerTransfer;

public class CrossArenaGuessEvent extends CrossArenaEventAbstractCommand {

    private ServerTransfer.GS_BS_CrossArenaGuess req;
    private GameServerTcpChannel gsChn;

    public void setReq(ServerTransfer.GS_BS_CrossArenaGuess req) {
        this.req = req;
    }

    public void setGsChn(GameServerTcpChannel gsChn) {
        this.gsChn = gsChn;
    }

    @Override
    public void doAction() {
        CrossArenaManager.getInstance().guess(gsChn, req.getPlayerId(), req.getLeitaiId(), req.getIsWin(), req.getSvrIndex());
    }
}
