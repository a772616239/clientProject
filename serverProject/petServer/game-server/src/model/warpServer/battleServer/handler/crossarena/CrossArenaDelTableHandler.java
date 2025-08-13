package model.warpServer.battleServer.handler.crossarena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaLtDel;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaLtDel_VALUE)
public class CrossArenaDelTableHandler extends AbstractHandler<BS_GS_CrossArenaLtDel> {
    @Override
    protected BS_GS_CrossArenaLtDel parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaLtDel.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaLtDel req, int i) {
        CrossArenaManager.getInstance().settleDelTable(req.getTableId());
    }
}
