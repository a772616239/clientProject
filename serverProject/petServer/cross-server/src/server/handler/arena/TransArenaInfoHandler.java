package server.handler.arena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_TransArenaInfo;

/**
 * @author huhan
 * @date 2020/05/12
 */
@MsgId(msgId = MsgIdEnum.GS_CS_TransArenaInfo_VALUE)
public class TransArenaInfoHandler extends AbstractHandler<GS_CS_TransArenaInfo> {
    @Override
    protected GS_CS_TransArenaInfo parse(byte[] bytes) throws Exception {
        return GS_CS_TransArenaInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_TransArenaInfo req, int i) {
    }
}
