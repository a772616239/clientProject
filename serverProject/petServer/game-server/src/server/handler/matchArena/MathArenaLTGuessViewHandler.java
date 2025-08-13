package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaLTGuessInfo;
import protocol.MatchArena.SC_MatchArenaLTGuessInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_MatchArenaLTGuessInfo_VALUE)
public class MathArenaLTGuessViewHandler extends AbstractBaseHandler<CS_MatchArenaLTGuessInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MatchArenaLTGuessInfo.Builder resultBuilder = SC_MatchArenaLTGuessInfo.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MatchArenaLTGuessInfo_VALUE, resultBuilder);
    }

    @Override
    protected CS_MatchArenaLTGuessInfo parse(byte[] bytes) throws Exception {
        return CS_MatchArenaLTGuessInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaLTGuessInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int leitaiId = req.getLeitaiId();
        MatchArenaLTManager.getInstance().guessInfoView(playerIdx, leitaiId);
    }
}
