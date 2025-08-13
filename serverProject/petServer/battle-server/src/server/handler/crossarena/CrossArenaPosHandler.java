package server.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_CrossArenaPos;
import server.event.EventManager;
import server.event.crossarena.CrossArenaPosEvent;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaPos_VALUE)
public class CrossArenaPosHandler extends AbstractHandler<GS_BS_CrossArenaPos> {
    @Override
    protected GS_BS_CrossArenaPos parse(byte[] bytes) throws Exception {
        return GS_BS_CrossArenaPos.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaPos req, int i) {
        CrossArenaPosEvent event = new CrossArenaPosEvent();
        event.setGsChn(gsChn);
        event.setReq(req);
        EventManager.getInstance().dealCrossArenaEvent(event, 0);
    }

}
