package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaAtt;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaAtt_VALUE)
public class CrossArenaBGAttHandler extends AbstractHandler<BS_GS_CrossArenaAtt> {
    @Override
    protected BS_GS_CrossArenaAtt parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaAtt.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaAtt req, int i) {
        if (req.getOper() == 0) {
            CrossArenaManager.getInstance().attTableBSBack(req.getPlayerId(), req.getRetCode(), req.getLeitaiId());
        }
    }
}
