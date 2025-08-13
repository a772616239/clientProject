package server.handler.targetSystem;

import protocol.Common.EnumFunction;
import hyzNet.GameServerTcpChannel;
import common.AbstractBaseHandler;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_ClaimTargetInfo;
import protocol.TargetSystem.SC_ClaimTargetInfo;
import protocol.TargetSystemDB.DB_TargetSystem;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimTargetInfo_VALUE)
public class ClaimTargetInfoHandler extends AbstractBaseHandler<CS_ClaimTargetInfo> {
    @Override
    protected CS_ClaimTargetInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimTargetInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimTargetInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_ClaimTargetInfo.Builder resultBuilder = SC_ClaimTargetInfo.newBuilder();
        if (entity == null) {
            LogUtil.error("ClaimTargetInfoHandler, playerIdx[" + playerIdx + "] target entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimTargetInfo_VALUE, resultBuilder);
            return;
        }

        DB_TargetSystem.Builder db_data = entity.getDb_Builder();
        if (db_data == null) {
            LogUtil.error("ClaimTargetInfoHandler, playerIdx[" + playerIdx + "] target dbData is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimTargetInfo_VALUE, resultBuilder);
            return;
        }

        resultBuilder.addAllMissionPro(db_data.getDailyMissionMap().values());
        resultBuilder.addAllAchievement(db_data.getAchievementMap().values());
        resultBuilder.addAllMistSeasonTask(db_data.getMistSeasonTaskMap().values());
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimTargetInfo_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}
