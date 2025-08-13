package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaEvent;
import protocol.CrossArena.SC_CrossArenaEvent;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaEvent_VALUE)
public class CrossArenaEventHandler extends AbstractBaseHandler<CS_CrossArenaEvent> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaEvent.Builder resultBuilder = SC_CrossArenaEvent.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaEvent_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaEvent parse(byte[] bytes) throws Exception {
        return CS_CrossArenaEvent.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaEvent req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_CrossArenaEvent.Builder resultBuilder = SC_CrossArenaEvent.newBuilder();
        RetCodeId.RetCodeEnum retCodeEnum = CrossArenaManager.getInstance().eventFinish(playerIdx, req.getEventId(), req.getParm());
        resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_CrossArenaEvent_VALUE, resultBuilder);
    }
}
