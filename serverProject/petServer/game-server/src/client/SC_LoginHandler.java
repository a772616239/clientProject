package client;


import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.LoginProto.SC_Login;
import protocol.MessageId.MsgIdEnum;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.SC_Login_VALUE)
public class SC_LoginHandler extends AbstractHandler<SC_Login> {

    @Override
    protected SC_Login parse(byte[] msgByte) throws Exception {
        return SC_Login.parseFrom(msgByte);
    }

    @Override
    protected void execute(GameServerTcpChannel channel, SC_Login req, int codeNum) {
        switch (req.getRetCode().getRetCode()) {
            case RCE_Success: {
                LogUtil.info("login success");

                break;
            }
            default: {
                LogUtil.error("log failed,errorID=" + req.getRetCode().getRetCodeValue());
                break;
            }
        }
//		System.out.println("error>>>>>>>>>>"+req.getError());
    }
}
