package server.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_CrossArenaUpTable;
import server.event.EventManager;
import server.event.crossarena.CrossArenaUpEvent;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaUpTable_VALUE)
public class CrossArenaUpHandler extends AbstractHandler<GS_BS_CrossArenaUpTable> {
    @Override
    protected GS_BS_CrossArenaUpTable parse(byte[] bytes) throws Exception {
        return GS_BS_CrossArenaUpTable.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaUpTable req, int i) {
        CrossArenaUpEvent event = new CrossArenaUpEvent();
        event.setGsChn(gsChn);
        event.setReq(req);
        EventManager.getInstance().dealCrossArenaEvent(event, req.getLeitaiId());
    }

}
