package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaMove;
import protocol.CrossArena.SC_CrossArenaMove;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaMove_VALUE)
public class CrossArenaMoveHandler extends AbstractBaseHandler<CS_CrossArenaMove> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaMove.Builder resultBuilder = SC_CrossArenaMove.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaMove_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaMove parse(byte[] bytes) throws Exception {
        return CS_CrossArenaMove.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaMove req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaManager.getInstance().movePos(playerIdx, req.getScreenIdView(), req.getScreenIdCurr(), req.getIsPlayerInfo());
    }
}
