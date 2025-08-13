package server.event.crossarena;

import hyzNet.GameServerTcpChannel;
import model.crossarena.CrossArenaManager;
import protocol.ServerTransfer;

public class CrossArenaPosEvent extends CrossArenaEventAbstractCommand {

    private ServerTransfer.GS_BS_CrossArenaPos req;
    private GameServerTcpChannel gsChn;

    public void setReq(ServerTransfer.GS_BS_CrossArenaPos req) {
        this.req = req;
    }

    public void setGsChn(GameServerTcpChannel gsChn) {
        this.gsChn = gsChn;
    }

    @Override
    public void doAction() {
        if (req.getOper() == 0) {
            CrossArenaManager.getInstance().posMoveCache(gsChn, req);
        } else {
            CrossArenaManager.getInstance().quitCache(gsChn, req);
        }
    }
}
