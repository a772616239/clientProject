package server.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_CrossArenaGuess;
import server.event.EventManager;
import server.event.crossarena.CrossArenaGuessEvent;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaGuess_VALUE)
public class CrossArenaGuessHandler extends AbstractHandler<GS_BS_CrossArenaGuess> {
    @Override
    protected GS_BS_CrossArenaGuess parse(byte[] bytes) throws Exception {
        return GS_BS_CrossArenaGuess.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaGuess req, int i) {
        CrossArenaGuessEvent event = new CrossArenaGuessEvent();
        event.setGsChn(gsChn);
        event.setReq(req);
        EventManager.getInstance().dealCrossArenaEvent(event, req.getLeitaiId());
    }

}
