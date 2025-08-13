package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaLTAtt;
import protocol.MatchArena.SC_MatchArenaLTAtt;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 擂台挑战
 */
@MsgId(msgId = MsgIdEnum.CS_MatchArenaLTAtt_VALUE)
public class MathArenaLTAttHandler extends AbstractBaseHandler<CS_MatchArenaLTAtt> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MatchArenaLTAtt.Builder resultBuilder = SC_MatchArenaLTAtt.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MatchArenaLTAtt_VALUE, resultBuilder);
    }

    @Override
    protected CS_MatchArenaLTAtt parse(byte[] bytes) throws Exception {
        return CS_MatchArenaLTAtt.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaLTAtt req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int leitaiId = req.getLeitaiId();
        MatchArenaLTManager.getInstance().attLeiTai(playerIdx, leitaiId);
    }
}
