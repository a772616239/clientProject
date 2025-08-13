package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaCacheSyn;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaCacheSyn_VALUE)
public class CrossArenaBGCacheSynHandler extends AbstractHandler<BS_GS_CrossArenaCacheSyn> {
    @Override
    protected BS_GS_CrossArenaCacheSyn parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaCacheSyn.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaCacheSyn req, int i) {
        if (req.getOffline() > 0) {
            CrossArenaManager.getInstance().cacheSynOff(req.getSynDataList());
        } else {
            CrossArenaManager.getInstance().cacheSyn(req.getSynTime(), req.getSynDataList());
        }
    }
}
