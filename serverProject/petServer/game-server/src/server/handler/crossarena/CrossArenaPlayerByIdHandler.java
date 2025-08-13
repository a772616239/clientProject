package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaPlayerById;
import protocol.CrossArena.SC_CrossArenaPlayerById;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaPlayerById_VALUE)
public class CrossArenaPlayerByIdHandler extends AbstractBaseHandler<CS_CrossArenaPlayerById> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaPlayerById.Builder resultBuilder = SC_CrossArenaPlayerById.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaPlayerById_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaPlayerById parse(byte[] bytes) throws Exception {
        return CS_CrossArenaPlayerById.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaPlayerById req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaManager.getInstance().seePlayerInfo(playerIdx, req.getPlayerIdx());
    }
}
