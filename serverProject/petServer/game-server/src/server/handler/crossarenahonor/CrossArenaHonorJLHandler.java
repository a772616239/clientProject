package server.handler.crossarenahonor;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaHonorManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaHonorJL;
import protocol.CrossArena.SC_CrossArenaHonorJL;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaHonorJL_VALUE)
public class CrossArenaHonorJLHandler extends AbstractBaseHandler<CS_CrossArenaHonorJL> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaHonorJL.Builder resultBuilder = SC_CrossArenaHonorJL.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaHonorJL_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaHonorJL parse(byte[] bytes) throws Exception {
        return CS_CrossArenaHonorJL.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaHonorJL req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaHonorManager.getInstance().getAward(playerIdx, req.getIdList());
    }
}
