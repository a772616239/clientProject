package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaLTOpen;
import protocol.MessageId.MsgIdEnum;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_MatchArenaLTOpen_VALUE)
public class MathArenaLTOpenHandler extends AbstractBaseHandler<CS_MatchArenaLTOpen> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }

    @Override
    protected CS_MatchArenaLTOpen parse(byte[] bytes) throws Exception {
        return CS_MatchArenaLTOpen.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaLTOpen req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int oper = req.getOper();
        MatchArenaLTManager.getInstance().openPanelPlayer(playerIdx, oper);
    }
}
