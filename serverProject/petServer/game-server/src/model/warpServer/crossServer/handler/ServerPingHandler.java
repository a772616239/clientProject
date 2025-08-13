package model.warpServer.crossServer.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.ServerPing;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.ServerPing_VALUE)
public class ServerPingHandler extends AbstractHandler<ServerPing> {
    @Override
    protected ServerPing parse(byte[] bytes) throws Exception {
        return ServerPing.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChannel, ServerPing serverPing, int i) {
        LogUtil.info("recv ping msg from " + gsChannel.channel.remoteAddress().toString());
    }
}
