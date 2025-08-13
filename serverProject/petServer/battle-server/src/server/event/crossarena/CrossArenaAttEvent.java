package server.event.crossarena;

import hyzNet.GameServerTcpChannel;
import model.crossarena.CrossArenaManager;
import protocol.ServerTransfer;

public class CrossArenaAttEvent extends CrossArenaEventAbstractCommand {

    private ServerTransfer.GS_BS_CrossArenaAtt req;
    private GameServerTcpChannel gsChn;

    public void setReq(ServerTransfer.GS_BS_CrossArenaAtt req) {
        this.req = req;
    }

    public void setGsChn(GameServerTcpChannel gsChn) {
        this.gsChn = gsChn;
    }

    @Override
    public void doAction() {
        if (req.getOper() == 0) {
            CrossArenaManager.getInstance().attTable(gsChn, req);
        } else if (req.getOper() == 9) {
            CrossArenaManager.getInstance().jionQue(gsChn, req);
        } else if (req.getOper() == 99) {
            CrossArenaManager.getInstance().quitQue(gsChn, req);
        }
    }
}
