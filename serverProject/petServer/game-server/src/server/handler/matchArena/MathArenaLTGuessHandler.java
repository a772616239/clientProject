package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaLTGuess;
import protocol.MatchArena.SC_MatchArenaLTGuess;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_MatchArenaLTGuess_VALUE)
public class MathArenaLTGuessHandler extends AbstractBaseHandler<CS_MatchArenaLTGuess> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MatchArenaLTGuess.Builder resultBuilder = SC_MatchArenaLTGuess.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MatchArenaLTGuess_VALUE, resultBuilder);
    }

    @Override
    protected CS_MatchArenaLTGuess parse(byte[] bytes) throws Exception {
        return CS_MatchArenaLTGuess.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaLTGuess req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int leitaiId = req.getLeitaiId();
        MatchArenaLTManager.getInstance().guess(playerIdx, leitaiId, req.getIsWin());
    }
}
