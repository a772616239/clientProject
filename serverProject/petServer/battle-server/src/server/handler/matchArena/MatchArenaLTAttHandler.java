package server.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_MatchArenaLTAtt;
import server.event.EventManager;
import server.event.leitai.LeitaiAttEvent;

@MsgId(msgId = MsgIdEnum.GS_BS_MatchArenaLTAtt_VALUE)
public class MatchArenaLTAttHandler extends AbstractHandler<GS_BS_MatchArenaLTAtt> {
    @Override
    protected GS_BS_MatchArenaLTAtt parse(byte[] bytes) throws Exception {
        return GS_BS_MatchArenaLTAtt.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_MatchArenaLTAtt req, int i) {
        LeitaiAttEvent event = new LeitaiAttEvent();
        event.setGsChn(gsChn);
        event.setReq(req);
        EventManager.getInstance().dealLeitaiEvent(event, req.getLeitaiId());
    }

}
