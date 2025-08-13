package server.handler.crossarenanote;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaHonorManager;
import model.crossarena.CrossArenaTopManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaNoteWorship;
import protocol.CrossArena.SC_CrossArenaNoteWorship;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaNoteWorship_VALUE)
public class CrossArenaNoteWorshipHandler extends AbstractBaseHandler<CS_CrossArenaNoteWorship> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaNoteWorship.Builder resultBuilder = SC_CrossArenaNoteWorship.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaNoteWorship_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaNoteWorship parse(byte[] bytes) throws Exception {
        return CS_CrossArenaNoteWorship.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaNoteWorship req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaHonorManager.getInstance().getNoteAward(playerIdx, req.getType());
    }
}
