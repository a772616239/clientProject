package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaLTQuit;
import protocol.MatchArena.SC_MatchArenaLTQuit;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_MatchArenaLTQuit_VALUE)
public class MathArenaLTQuitHandler extends AbstractBaseHandler<CS_MatchArenaLTQuit> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MatchArenaLTQuit.Builder resultBuilder = SC_MatchArenaLTQuit.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MatchArenaLTQuit_VALUE, resultBuilder);
    }

    @Override
    protected CS_MatchArenaLTQuit parse(byte[] bytes) throws Exception {
        return CS_MatchArenaLTQuit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaLTQuit req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int leitaiId = req.getLeitaiId();
        MatchArenaLTManager.getInstance().quitLeiTai(playerIdx, leitaiId);
    }
}
