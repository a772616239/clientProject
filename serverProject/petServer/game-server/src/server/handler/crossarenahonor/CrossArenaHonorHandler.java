package server.handler.crossarenahonor;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaHonorManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaHonor;
import protocol.CrossArena.SC_CrossArenaHonor;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaHonor_VALUE)
public class CrossArenaHonorHandler extends AbstractBaseHandler<CS_CrossArenaHonor> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaHonor.Builder resultBuilder = SC_CrossArenaHonor.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaHonor_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaHonor parse(byte[] bytes) throws Exception {
        return CS_CrossArenaHonor.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaHonor req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaHonorManager.getInstance().sendHonor(playerIdx);
    }
}
