package server.handler.crossarenanote;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaHonorManager;
import model.crossarena.CrossArenaTopManager;
import protocol.Common.EnumFunction;
import protocol.CrossArena.CS_CrossArenaNote;
import protocol.CrossArena.SC_CrossArenaNote;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_CrossArenaNote_VALUE)
public class CrossArenaNoteHandler extends AbstractBaseHandler<CS_CrossArenaNote> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_CrossArenaNote.Builder resultBuilder = SC_CrossArenaNote.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_CrossArenaNote_VALUE, resultBuilder);
    }

    @Override
    protected CS_CrossArenaNote parse(byte[] bytes) throws Exception {
        return CS_CrossArenaNote.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaNote req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        CrossArenaHonorManager.getInstance().getNote(playerIdx, req.getType(), req.getParm());
    }
}
