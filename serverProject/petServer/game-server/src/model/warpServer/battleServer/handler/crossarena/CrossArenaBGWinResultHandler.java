package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaWinResult;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaWinResult_VALUE)
public class CrossArenaBGWinResultHandler extends AbstractHandler<BS_GS_CrossArenaWinResult> {
    @Override
    protected BS_GS_CrossArenaWinResult parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaWinResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaWinResult req, int i) {
        CrossArenaManager.getInstance().battleWin(req);
    }
}
