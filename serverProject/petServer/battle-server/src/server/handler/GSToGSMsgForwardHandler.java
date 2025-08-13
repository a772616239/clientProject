package server.handler;

import com.google.protobuf.ByteString;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.warpServer.WarpServerManager;
import protocol.MessageId;
import protocol.ServerTransfer;
import protocol.ServerTransfer.BS_GS_ForwardPlayerMsg;
import protocol.ServerTransfer.GS_BS_ForwardPlayerMsg;

import static protocol.MessageId.MsgIdEnum.BS_GS_ForwardPlayerMsg_VALUE;
import protocol.ServerTransfer.ServerTypeEnum;


@MsgId(msgId = MessageId.MsgIdEnum.GS_BS_ForwardPlayerMsg_VALUE)
public class GSToGSMsgForwardHandler extends AbstractHandler<GS_BS_ForwardPlayerMsg> {
    @Override
    protected GS_BS_ForwardPlayerMsg parse(byte[] bytes) throws Exception {
        return GS_BS_ForwardPlayerMsg.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_ForwardPlayerMsg req, int i) {
        int msgId = req.getMsgId();
        ByteString msgData = req.getMsgData();

        req.getSendPlayerMap().forEach((k, v) -> {
            BS_GS_ForwardPlayerMsg.Builder msg = BS_GS_ForwardPlayerMsg.newBuilder().setMsgId(msgId).setMsgData(msgData);
            GameServerTcpChannel serverChannel = WarpServerManager.getInstance().getGameServerChannel(v);
            if (serverChannel != null) {
                msg.setSendPlayerIdx(k);
                serverChannel.send(BS_GS_ForwardPlayerMsg_VALUE, msg);
            }
        });
        req.getOldSendPlayerMap().forEach((k, v) -> {
            BS_GS_ForwardPlayerMsg.Builder msg = BS_GS_ForwardPlayerMsg.newBuilder().setMsgId(msgId).setMsgData(msgData);
            int serverIndex = WarpServerManager.getInstance().getSeverIndexByIp(v);
            if (serverIndex > 0) {
                msg.setSendPlayerIdx(k);
                WarpServerManager.getInstance().sendMsgToServer(ServerTypeEnum.STE_GameServer_VALUE, serverIndex, MessageId.MsgIdEnum.BS_GS_ForwardPlayerMsg_VALUE, msg);
            }
        });
    }
}