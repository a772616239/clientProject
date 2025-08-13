package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaClose;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaClose_VALUE)
public class CrossArenaCloseHandler extends AbstractBaseHandler<CS_CrossArenaClose> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }

    @Override
    protected CS_CrossArenaClose parse(byte[] bytes) throws Exception {
        return CS_CrossArenaClose.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaClose req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaManager.getInstance().closeTable(playerIdx);
    }
}
