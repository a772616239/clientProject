package petrobot.system.mistForest.handler;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.SC_MistEnterPlayerInfo;

@MsgId(msgId = MsgIdEnum.SC_MistEnterPlayerInfo_VALUE)
public class MistPlayerInfoHandler extends AbstractHandler<SC_MistEnterPlayerInfo> {
    @Override
    protected SC_MistEnterPlayerInfo parse(byte[] bytes) throws Exception {
        return SC_MistEnterPlayerInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, SC_MistEnterPlayerInfo ret, int i) {

    }
}
