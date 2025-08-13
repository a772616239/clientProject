package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaRefInfo;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaRefInfo_VALUE)
public class CrossArenaBGRefDBHandler extends AbstractHandler<BS_GS_CrossArenaRefInfo> {
    @Override
    protected BS_GS_CrossArenaRefInfo parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaRefInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaRefInfo req, int i) {
        CrossArenaManager.getInstance().tableChangeAfter(req.getTableInfo());
    }
}
