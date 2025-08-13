package model.warpServer.battleServer.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaPvpConsumeBack;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaPvpConsumeBack_VALUE)
public class CrossArenaPvpConsumeBackHandler extends AbstractHandler<BS_GS_CrossArenaPvpConsumeBack> {
    @Override
    protected BS_GS_CrossArenaPvpConsumeBack parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaPvpConsumeBack.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaPvpConsumeBack req, int i) {
    	CrossArenaPvpManager.getInstance().consumeBack(req.getPlayerId(), req.getCostIndex());
    }
}
