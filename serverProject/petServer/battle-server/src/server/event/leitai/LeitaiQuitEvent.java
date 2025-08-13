package server.event.leitai;

import hyzNet.GameServerTcpChannel;
import model.matchArena.MatchArenaLTManager;
import protocol.ServerTransfer;

public class LeitaiQuitEvent extends LeitaiEventAbstractCommand {

    private ServerTransfer.GS_BS_MatchArenaLTQuit req;
    private GameServerTcpChannel gsChn;

    public void setReq(ServerTransfer.GS_BS_MatchArenaLTQuit req) {
        this.req = req;
    }

    public void setGsChn(GameServerTcpChannel gsChn) {
        this.gsChn = gsChn;
    }

    @Override
    public void doAction() {
        MatchArenaLTManager.getInstance().quitLeiTai(gsChn, req.getPlayerId(), req.getLeitaiId(), req.getTeamInfoRobot(), req.getShowPetId());
    }
}
