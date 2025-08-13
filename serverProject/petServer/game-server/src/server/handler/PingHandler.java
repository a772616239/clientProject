package server.handler;

import protocol.Common.EnumFunction;
import hyzNet.GameServerTcpChannel;
import common.AbstractBaseHandler;
import hyzNet.message.MsgId;
import protocol.LoginProto.CS_Ping;
import protocol.LoginProto.SC_Ping;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.CS_Ping_VALUE)
public class PingHandler extends AbstractBaseHandler<CS_Ping> {
    @Override
    protected CS_Ping parse(byte[] bytes) throws Exception {
        return CS_Ping.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_Ping req, int codeNum) {
        gsChn.send(MsgIdEnum.SC_Ping_VALUE, SC_Ping.newBuilder());
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }

 }
