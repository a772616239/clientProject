package server.handler.crossarenahonor;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaHonorManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaHonorHis;
import protocol.CrossArena.SC_CrossArenaHonorHis;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaHonorHis_VALUE)
public class CrossArenaHonorHisHandler extends AbstractBaseHandler<CS_CrossArenaHonorHis> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaHonorHis.Builder resultBuilder = SC_CrossArenaHonorHis.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaHonorHis_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaHonorHis parse(byte[] bytes) throws Exception {
        return CS_CrossArenaHonorHis.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaHonorHis req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaHonorManager.getInstance().sendHonorHis(playerIdx);
    }
}
