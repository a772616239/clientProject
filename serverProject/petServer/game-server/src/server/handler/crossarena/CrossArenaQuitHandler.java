package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaQuit;
import protocol.CrossArena.SC_CrossArenaQuit;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaQuit_VALUE)
public class CrossArenaQuitHandler extends AbstractBaseHandler<CS_CrossArenaQuit> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaQuit.Builder resultBuilder = SC_CrossArenaQuit.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaQuit_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaQuit parse(byte[] bytes) throws Exception {
        return CS_CrossArenaQuit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaQuit req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int tableId = req.getLeitaiId();
        CrossArenaManager.getInstance().quitTable(playerIdx, tableId);
    }
}
