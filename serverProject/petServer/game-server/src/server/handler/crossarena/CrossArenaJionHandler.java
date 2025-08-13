package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaChoose;
import protocol.CrossArena.SC_CrossArenaChoose;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaChoose_VALUE)
public class CrossArenaJionHandler extends AbstractBaseHandler<CS_CrossArenaChoose> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaChoose.Builder resultBuilder = SC_CrossArenaChoose.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaChoose_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaChoose parse(byte[] bytes) throws Exception {
        return CS_CrossArenaChoose.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaChoose req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaManager.getInstance().changeScene(playerIdx, req.getSceneId());
    }
}
