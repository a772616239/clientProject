package model.warpServer.battleServer.handler;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_ForwardPlayerMsg;

@MsgId(msgId = MsgIdEnum.BS_GS_ForwardPlayerMsg_VALUE)
public class ForwardMsgToPlayerHandler extends AbstractHandler<BS_GS_ForwardPlayerMsg> {
    @Override
    protected BS_GS_ForwardPlayerMsg parse(byte[] bytes) throws Exception {
        return BS_GS_ForwardPlayerMsg.parseFrom(bytes);
    }


    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_ForwardPlayerMsg req, int i) {
        GlobalData.getInstance().sendByteMsg(req.getSendPlayerIdx(), req.getMsgId(), req.getMsgData().toByteArray());
    }
}
