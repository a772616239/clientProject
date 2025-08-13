package model.warpServer.battleServer.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.ServerTransfer.BS_GS_MatchArenaLTAtt;
import protocol.MessageId.MsgIdEnum;

@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaLTAtt_VALUE)
public class MatchArenaLTAttHandler extends AbstractHandler<BS_GS_MatchArenaLTAtt> {
    @Override
    protected BS_GS_MatchArenaLTAtt parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaLTAtt.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaLTAtt req, int i) {
        MatchArenaLTManager.getInstance().attLeiTaiBSBack(req.getPlayerId(), req.getRetCode());
    }
}
