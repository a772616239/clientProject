package model.warpServer.battleServer.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaLTRef;

@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaLTRef_VALUE)
public class MatchArenaLTRefHandler extends AbstractHandler<BS_GS_MatchArenaLTRef> {
    @Override
    protected BS_GS_MatchArenaLTRef parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaLTRef.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaLTRef req, int i) {
        MatchArenaLTManager.getInstance().refStageInfoAllOnline(req.getLeitaiId());
    }
}
