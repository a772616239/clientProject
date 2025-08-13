package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaViewFight;
import protocol.CrossArena.SC_CrossArenaViewFight;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaViewFight_VALUE)
public class CrossArenaViewFightHandler extends AbstractBaseHandler<CS_CrossArenaViewFight> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaViewFight.Builder resultBuilder = SC_CrossArenaViewFight.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaViewFight_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaViewFight parse(byte[] bytes) throws Exception {
        return CS_CrossArenaViewFight.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaViewFight req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int tableId = req.getLeitaiId();
        CrossArenaManager.getInstance().reqViewFight(playerIdx, tableId);
    }
}
