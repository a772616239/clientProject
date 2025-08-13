package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaReadyFight;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaReadyFight_VALUE)
public class CrossArenaBGReadyFightNoticeHandler extends AbstractHandler<BS_GS_CrossArenaReadyFight> {
    @Override
    protected BS_GS_CrossArenaReadyFight parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaReadyFight.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaReadyFight req, int i) {
        CrossArenaManager.getInstance().noticeReadyFight(req.getPlayerId(), req.getEndtime(),req.getStateList(),req.getAtt());
    }
}
