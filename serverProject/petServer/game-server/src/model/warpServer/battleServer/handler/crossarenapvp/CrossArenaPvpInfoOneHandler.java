package model.warpServer.battleServer.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaPvpInfoOne;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaPvpInfoOne_VALUE)
public class CrossArenaPvpInfoOneHandler extends AbstractHandler<BS_GS_CrossArenaPvpInfoOne> {
    @Override
    protected BS_GS_CrossArenaPvpInfoOne parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaPvpInfoOne.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaPvpInfoOne req, int i) {
    	CrossArenaPvpManager.getInstance().sendInfoOne(req.getRoom(),req.getOnePlayer(),req.getType());
    }
}
