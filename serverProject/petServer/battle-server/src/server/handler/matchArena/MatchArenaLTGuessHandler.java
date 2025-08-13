package server.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_MatchArenaLTGuess;
import server.event.EventManager;
import server.event.leitai.LeitaiGuessEvent;

@MsgId(msgId = MsgIdEnum.GS_BS_MatchArenaLTGuess_VALUE)
public class MatchArenaLTGuessHandler extends AbstractHandler<GS_BS_MatchArenaLTGuess> {
    @Override
    protected GS_BS_MatchArenaLTGuess parse(byte[] bytes) throws Exception {
        return GS_BS_MatchArenaLTGuess.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_MatchArenaLTGuess req, int i) {
        LeitaiGuessEvent event = new LeitaiGuessEvent();
        event.setGsChn(gsChn);
        event.setReq(req);
        EventManager.getInstance().dealLeitaiEvent(event, req.getLeitaiId());
    }

}
