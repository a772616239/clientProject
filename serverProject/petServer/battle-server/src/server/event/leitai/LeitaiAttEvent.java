package server.event.leitai;

import hyzNet.GameServerTcpChannel;
import model.matchArena.MatchArenaLTManager;
import protocol.ServerTransfer;

public class LeitaiAttEvent extends LeitaiEventAbstractCommand {

    private ServerTransfer.GS_BS_MatchArenaLTAtt req;
    private GameServerTcpChannel gsChn;

    public void setReq(ServerTransfer.GS_BS_MatchArenaLTAtt req) {
        this.req = req;
    }

    public void setGsChn(GameServerTcpChannel gsChn) {
        this.gsChn = gsChn;
    }

    @Override
    public void doAction() {
        if (req.getOper() == 0) {
            MatchArenaLTManager.getInstance().attLeiTai(gsChn, req);
        } else {
            MatchArenaLTManager.getInstance().attLeiTaiAI(req.getOper(), req);
        }
    }
}
