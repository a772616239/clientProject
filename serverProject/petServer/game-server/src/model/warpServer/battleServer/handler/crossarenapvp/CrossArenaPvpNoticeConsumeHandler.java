package model.warpServer.battleServer.handler.crossarenapvp;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarenapvp.CrossArenaPvpManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_CrossArenaPvpNoticeConsume;

@MsgId(msgId = MsgIdEnum.BS_GS_CrossArenaPvpNoticeConsume_VALUE)
public class CrossArenaPvpNoticeConsumeHandler extends AbstractHandler<BS_GS_CrossArenaPvpNoticeConsume> {
    @Override
    protected BS_GS_CrossArenaPvpNoticeConsume parse(byte[] bytes) throws Exception {
        return BS_GS_CrossArenaPvpNoticeConsume.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_CrossArenaPvpNoticeConsume req, int i) {
    	CrossArenaPvpManager.getInstance().consumeResult(req.getAtterPlayerId(),req.getOwnSvrIndex(), req.getRoomId(), req.getOwnPlayerId(), req.getAtterPlayerId(), req.getCostIndex());
    }
}
