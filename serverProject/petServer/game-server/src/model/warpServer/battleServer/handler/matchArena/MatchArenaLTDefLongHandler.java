package model.warpServer.battleServer.handler.matchArena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_MatchArenaLTDefLong;

@MsgId(msgId = MsgIdEnum.BS_GS_MatchArenaLTDefLong_VALUE)
public class MatchArenaLTDefLongHandler extends AbstractHandler<BS_GS_MatchArenaLTDefLong> {
    @Override
    protected BS_GS_MatchArenaLTDefLong parse(byte[] bytes) throws Exception {
        return BS_GS_MatchArenaLTDefLong.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, BS_GS_MatchArenaLTDefLong req, int i) {
        MatchArenaLTManager.getInstance().checkAIAtt(req.getLeitaiId(), req.getLastTime(), req.getDefWin(), req.getDefplayerId());
    }
}
