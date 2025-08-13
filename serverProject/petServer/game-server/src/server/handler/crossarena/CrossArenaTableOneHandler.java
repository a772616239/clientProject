package server.handler.crossarena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaTableOneInfo;
import protocol.CrossArena.SC_CrossArenaTableOneInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaTableOneInfo_VALUE)
public class CrossArenaTableOneHandler extends AbstractBaseHandler<CS_CrossArenaTableOneInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaTableOneInfo.Builder resultBuilder = SC_CrossArenaTableOneInfo.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaTableOneInfo_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaTableOneInfo parse(byte[] bytes) throws Exception {
        return CS_CrossArenaTableOneInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaTableOneInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaManager.getInstance().sendTableOneInfo(playerIdx, req.getTableId());
    }
}
