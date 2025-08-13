package server.handler.matchArena;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_MatchArenaTaskMission;
import protocol.TargetSystem.SC_MatchArenaTaskMission;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_MatchArenaTaskMission_VALUE)
public class MathArenaTaskTrackHandler extends AbstractBaseHandler<CS_MatchArenaTaskMission> {

    @Override
    protected CS_MatchArenaTaskMission parse(byte[] bytes) throws Exception {
        return CS_MatchArenaTaskMission.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaTaskMission req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_MatchArenaTaskMission.Builder resultBuilder = SC_MatchArenaTaskMission.newBuilder();
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_MatchArenaTaskMission_VALUE, resultBuilder);
            return;
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.addAllMission(entity.getDb_Builder().getMatchArenaInfoMap().values());
        gsChn.send(MsgIdEnum.SC_MatchArenaTaskMission_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.GrowthTrack;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_MatchArenaTaskMission_VALUE, SC_MatchArenaTaskMission.newBuilder().setRetCode(retCode));
    }
}
