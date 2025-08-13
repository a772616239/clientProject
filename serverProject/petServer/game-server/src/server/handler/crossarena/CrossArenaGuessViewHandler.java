package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaGuessInfo;
import protocol.CrossArena.SC_CrossArenaGuessInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaGuessInfo_VALUE)
public class CrossArenaGuessViewHandler extends AbstractBaseHandler<CS_CrossArenaGuessInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaGuessInfo.Builder resultBuilder = SC_CrossArenaGuessInfo.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaGuessInfo_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaGuessInfo parse(byte[] bytes) throws Exception {
        return CS_CrossArenaGuessInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaGuessInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int tableId = req.getLeitaiId();
        CrossArenaManager.getInstance().guessInfoView(playerIdx, tableId);
    }

}
