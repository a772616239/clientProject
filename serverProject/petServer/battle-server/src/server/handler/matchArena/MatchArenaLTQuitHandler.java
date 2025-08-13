package server.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_MatchArenaLTQuit;
import server.event.EventManager;
import server.event.leitai.LeitaiQuitEvent;

@MsgId(msgId = MsgIdEnum.GS_BS_MatchArenaLTQuit_VALUE)
public class MatchArenaLTQuitHandler extends AbstractHandler<GS_BS_MatchArenaLTQuit> {
    @Override
    protected GS_BS_MatchArenaLTQuit parse(byte[] bytes) throws Exception {
        return GS_BS_MatchArenaLTQuit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_MatchArenaLTQuit req, int i) {
        LeitaiQuitEvent event = new LeitaiQuitEvent();
        event.setGsChn(gsChn);
        event.setReq(req);
        EventManager.getInstance().dealLeitaiEvent(event, req.getLeitaiId());
    }

}
