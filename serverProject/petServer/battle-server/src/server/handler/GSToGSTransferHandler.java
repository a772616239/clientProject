package server.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import model.warpServer.WarpServerManager;
import protocol.MessageId;
import protocol.ServerTransfer;
import protocol.ServerTransfer.BS_GS_CrossArenaRefInfo;
import util.LogUtil;

@MsgId(msgId = MessageId.MsgIdEnum.GS_BS_TransferGSTOGSMsg_VALUE)
public class GSToGSTransferHandler extends AbstractHandler<ServerTransfer.GS_BS_TransferGSTOGSMsg> {

    @Override
    protected ServerTransfer.GS_BS_TransferGSTOGSMsg parse(byte[] bytes) throws Exception {
        return ServerTransfer.GS_BS_TransferGSTOGSMsg.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, ServerTransfer.GS_BS_TransferGSTOGSMsg req, int i) {
        try {
        	if (req.getMsgId() == MessageId.MsgIdEnum.BS_GS_CrossArenaRefInfo_VALUE) {
        		BS_GS_CrossArenaRefInfo msgs = BS_GS_CrossArenaRefInfo.parseFrom(req.getMsgData());
        		CrossArenaManager.getInstance().refTableInfoToAllServer(msgs.getTableInfo());
        		return;
        	}
            GameServerTcpChannel chn = WarpServerManager.getInstance().getGameServerChannel(req.getSvrIndex());
            if (null == chn) {
                return;
            }
            ServerTransfer.BS_GS_TransferGSTOGSMsg.Builder builder = ServerTransfer.BS_GS_TransferGSTOGSMsg.newBuilder();
            builder.setMsgId(req.getMsgId());
            builder.setMsgData(req.getMsgData());
            chn.send(MessageId.MsgIdEnum.BS_GS_TransferGSTOGSMsg_VALUE, builder);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            gsChn.close();
        }
    }

}
