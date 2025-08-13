package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena;
import protocol.CrossArena.CS_CrossArenaQueAll;
import protocol.CrossArena.SC_CrossArenaQueAll;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaQueAll_VALUE)
public class CrossArenaQueAllHandler extends AbstractBaseHandler<CS_CrossArenaQueAll> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        CrossArena.SC_CrossArenaQueAll.Builder resultBuilder = CrossArena.SC_CrossArenaQueAll.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaQueAll_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaQueAll parse(byte[] bytes) throws Exception {
        return CS_CrossArenaQueAll.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaQueAll req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaManager.getInstance().getQue(playerIdx, req.getSceneId());
    }
}
