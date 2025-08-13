package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaGuessResult;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaGuessResult_VALUE)
public class CrossArenaBGGuessResultHandler extends AbstractHandler<BS_GS_CrossArenaGuessResult> {
    @Override
    protected BS_GS_CrossArenaGuessResult parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaGuessResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaGuessResult req, int i) {
        CrossArenaManager.getInstance().guessResult(req.getWinIdsList());
    }
}
