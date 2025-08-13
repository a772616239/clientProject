package server.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_CrossArenaAtt;
import server.event.EventManager;
import server.event.crossarena.CrossArenaAttEvent;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaAtt_VALUE)
public class CrossArenaAttHandler extends AbstractHandler<GS_BS_CrossArenaAtt> {
    @Override
    protected GS_BS_CrossArenaAtt parse(byte[] bytes) throws Exception {
        return GS_BS_CrossArenaAtt.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaAtt req, int i) {
        CrossArenaAttEvent event = new CrossArenaAttEvent();
        event.setGsChn(gsChn);
        event.setReq(req);
        EventManager.getInstance().dealCrossArenaEvent(event, req.getLeitaiId());
    }

}
