package model.warpServer.battleServer.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaOpen;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaOpen_VALUE)
public class CrossArenaPvpPanelHandler extends AbstractHandler<BS_GS_CrossArenaOpen> {
    @Override
    protected BS_GS_CrossArenaOpen parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaOpen.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaOpen req, int i) {
//    	CrossArenaPvpManager.getInstance().returnPanel(req.getPlayerId(),req.getRet(),req.getRoom());
    }
}
