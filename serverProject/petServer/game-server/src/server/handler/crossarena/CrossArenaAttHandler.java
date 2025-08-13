package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaAtt;
import protocol.CrossArena.SC_CrossArenaAtt;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaAtt_VALUE)
public class CrossArenaAttHandler extends AbstractBaseHandler<CS_CrossArenaAtt> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaAtt.Builder resultBuilder = SC_CrossArenaAtt.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaAtt_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaAtt parse(byte[] bytes) throws Exception {
        return CS_CrossArenaAtt.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaAtt req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int tableId = req.getLeitaiId();
        if (tableId == 0) {
            CrossArenaManager.getInstance().jionQue(playerIdx);
        } else if (tableId < 0) {
            CrossArenaManager.getInstance().quitAll(playerIdx, true);
        } else {
            CrossArenaManager.getInstance().attTable(playerIdx, tableId);
        }
    }
}
