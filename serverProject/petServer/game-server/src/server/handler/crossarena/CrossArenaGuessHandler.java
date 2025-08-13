package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaGuess;
import protocol.CrossArena.SC_CrossArenaGuess;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaGuess_VALUE)
public class CrossArenaGuessHandler extends AbstractBaseHandler<CS_CrossArenaGuess> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaGuess.Builder resultBuilder = SC_CrossArenaGuess.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaGuess_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaGuess parse(byte[] bytes) throws Exception {
        return CS_CrossArenaGuess.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaGuess req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int tableId = req.getLeitaiId();
        CrossArenaManager.getInstance().guess(playerIdx, tableId, req.getIsWin());
    }
}
