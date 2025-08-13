package server.handler.activity.petAvoidance;


import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.*;
import protocol.Activity.CS_ClaimPetAvoidance;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimPetAvoidance_VALUE)
public class ClaimPetAvoidanceHandler extends AbstractBaseHandler<CS_ClaimPetAvoidance> {

    @Override
    protected CS_ClaimPetAvoidance parse(byte[] bytes) throws Exception {
        return CS_ClaimPetAvoidance.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimPetAvoidance req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        long activityId = req.getActivityId();

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(activityId);
        Activity.SC_ClaimPetAvoidance.Builder resultBuilder = Activity.SC_ClaimPetAvoidance.newBuilder();
        if (!ActivityUtil.activityInOpen(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_ClaimPetAvoidance_VALUE, resultBuilder);
            return;
        }

        if (activityCfg.getType() != Activity.ActivityTypeEnum.ATE_PetAvoidance) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimPetAvoidance_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimPetAvoidance_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            buildClientMsg(resultBuilder, entity, activityCfg);
            gsChn.send(MsgIdEnum.SC_ClaimPetAvoidance_VALUE, resultBuilder);
        });
    }

    private void buildClientMsg(Activity.SC_ClaimPetAvoidance.Builder resultBuilder, targetsystemEntity entity, ServerActivity activityCfg) {
        TargetSystemDB.DB_PetAvoidance petAvoidance = entity.getDb_Builder().getSpecialInfo().getPetAvoidance();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setTimes(petAvoidance.getChallengedTimes());
        resultBuilder.setTimesLimit(activityCfg.getPetAvoidance().getDailyChallengeTimes());
        LogUtil.info(" 魔灵大躲避 ：" +petAvoidance.getChallengedTimes() + " " + activityCfg.getPetAvoidance().getDailyChallengeTimes());

    }

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
