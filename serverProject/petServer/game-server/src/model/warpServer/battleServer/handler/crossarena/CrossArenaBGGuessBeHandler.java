package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaGuessBe;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaGuessBe_VALUE)
public class CrossArenaBGGuessBeHandler extends AbstractHandler<BS_GS_CrossArenaGuessBe> {
    @Override
    protected BS_GS_CrossArenaGuessBe parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaGuessBe.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaGuessBe req, int i) {
        CrossArenaManager.getInstance().guessBSBe(req.getPlayerId());
    }
}
