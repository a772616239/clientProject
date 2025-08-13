package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaLTViewFight;
import protocol.MatchArena.SC_MatchArenaLTViewFight;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MatchArenaLTViewFight_VALUE)
public class MathArenaLTViewFightHandler extends AbstractBaseHandler<CS_MatchArenaLTViewFight> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MatchArenaLTViewFight.Builder resultBuilder = SC_MatchArenaLTViewFight.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MatchArenaLTViewFight_VALUE, resultBuilder);
    }

    @Override
    protected CS_MatchArenaLTViewFight parse(byte[] bytes) throws Exception {
        return CS_MatchArenaLTViewFight.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaLTViewFight req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int leitaiId = req.getLeitaiId();
        MatchArenaLTManager.getInstance().reqViewFight(playerIdx, leitaiId);
    }
}
