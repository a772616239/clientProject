package petrobot.system;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.LoginProto.SC_Ping;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.SC_Ping_VALUE)
public class PingHandler extends AbstractHandler<SC_Ping> {
    @Override
    protected SC_Ping parse(byte[] bytes) throws Exception {
        return SC_Ping.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_Ping result, int i) {
    }
}
