package server.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_BS_MatchArenaLTOpen;

@MsgId(msgId = MsgIdEnum.GS_BS_MatchArenaLTOpen_VALUE)
public class MatchArenaLTOpenHandler extends AbstractHandler<GS_BS_MatchArenaLTOpen> {
    @Override
    protected GS_BS_MatchArenaLTOpen parse(byte[] bytes) throws Exception {
        return GS_BS_MatchArenaLTOpen.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_BS_MatchArenaLTOpen req, int i) {
    }

}
