package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaGuess;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaGuess_VALUE)
public class CrossArenaBGGuessHandler extends AbstractHandler<BS_GS_CrossArenaGuess> {
    @Override
    protected BS_GS_CrossArenaGuess parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaGuess.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaGuess req, int i) {
        CrossArenaManager.getInstance().guessBSBack(req.getPlayerId(), req.getRetCode());
    }
}
