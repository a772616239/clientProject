package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaQuit;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaQuit_VALUE)
public class CrossArenaBGQuitHandler extends AbstractHandler<BS_GS_CrossArenaQuit> {
    @Override
    protected BS_GS_CrossArenaQuit parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaQuit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaQuit req, int i) {
        CrossArenaManager.getInstance().quitTableBSBack(req.getPlayerId(), req.getRetCode());
    }
}
