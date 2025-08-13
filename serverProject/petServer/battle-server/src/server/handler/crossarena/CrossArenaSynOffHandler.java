package server.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_CrossArenaPlyoffline;

@MsgId(msgId = MsgIdEnum.GS_BS_CrossArenaPlyoffline_VALUE)
public class CrossArenaSynOffHandler extends AbstractHandler<GS_BS_CrossArenaPlyoffline> {
    @Override
    protected GS_BS_CrossArenaPlyoffline parse(byte[] bytes) throws Exception {
        return GS_BS_CrossArenaPlyoffline.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_CrossArenaPlyoffline req, int i) {
        CrossArenaManager.getInstance().synOffCache(req.getSid(), req.getPartInfoList());
    }

}
