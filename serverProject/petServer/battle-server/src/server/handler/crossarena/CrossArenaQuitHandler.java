package server.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_CrossArenaQuit;
import server.event.EventManager;
import server.event.crossarena.CrossArenaQuitEvent;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaQuit_VALUE)
public class CrossArenaQuitHandler extends AbstractHandler<GS_BS_CrossArenaQuit> {
    @Override
    protected GS_BS_CrossArenaQuit parse(byte[] bytes) throws Exception {
        return GS_BS_CrossArenaQuit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaQuit req, int i) {
        CrossArenaQuitEvent event = new CrossArenaQuitEvent();
        event.setGsChn(gsChn);
        event.setReq(req);
        EventManager.getInstance().dealCrossArenaEvent(event, req.getLeitaiId());
    }

}
