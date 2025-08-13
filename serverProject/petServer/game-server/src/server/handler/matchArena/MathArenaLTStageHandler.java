package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.matcharena.MatchArenaLTManager;
import protocol.Common.EnumFunction;
import protocol.MatchArena.CS_MatchArenaLTStageInfo;
import protocol.MatchArena.SC_MatchArenaLTStageInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MatchArenaLTStageInfo_VALUE)
public class MathArenaLTStageHandler extends AbstractBaseHandler<CS_MatchArenaLTStageInfo> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_MatchArenaLTStageInfo.Builder resultBuilder = SC_MatchArenaLTStageInfo.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
        gsChn.send(MsgIdEnum.SC_MatchArenaLTStageInfo_VALUE, resultBuilder);
    }

    @Override
    protected CS_MatchArenaLTStageInfo parse(byte[] bytes) throws Exception {
        return CS_MatchArenaLTStageInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaLTStageInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int stageId = req.getStageId();
        MatchArenaLTManager.getInstance().getStageInfo(playerIdx, stageId);
    }
}
