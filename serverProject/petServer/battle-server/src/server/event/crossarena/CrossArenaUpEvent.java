package server.event.crossarena;

import hyzNet.GameServerTcpChannel;
import model.crossarena.CrossArenaManager;
import protocol.ServerTransfer;

public class CrossArenaUpEvent extends CrossArenaEventAbstractCommand {

    private ServerTransfer.GS_BS_CrossArenaUpTable req;
    private GameServerTcpChannel gsChn;

    public void setReq(ServerTransfer.GS_BS_CrossArenaUpTable req) {
        this.req = req;
    }

    public void setGsChn(GameServerTcpChannel gsChn) {
        this.gsChn = gsChn;
    }

    @Override
    public void doAction() {
//        CrossArenaManager.getInstance().upTable(gsChn, req);
    }
}
