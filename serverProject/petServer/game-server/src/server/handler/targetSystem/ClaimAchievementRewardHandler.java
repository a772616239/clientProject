package server.handler.targetSystem;

import cfg.Achievement;
import cfg.AchievementObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.AchievmentPro;
import protocol.TargetSystem.CS_ClaimAchievementReward;
import protocol.TargetSystem.SC_ClaimAchievementReward;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimAchievementReward_VALUE)
public class ClaimAchievementRewardHandler extends AbstractBaseHandler<CS_ClaimAchievementReward> {
    @Override
    protected CS_ClaimAchievementReward parse(byte[] bytes) throws Exception {
        return CS_ClaimAchievementReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimAchievementReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimAchievementReward.Builder resultBuilder = SC_ClaimAchievementReward.newBuilder();
        int cfgId = req.getCfgId();
        int index = req.getIndex();

        AchievementObject achievementCfg = Achievement.getById(cfgId);
        if (achievementCfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionCfgIdNotExist));
            gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, resultBuilder);
            return;
        }

        if (achievementCfg.getTargetcount().length <= index) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            LogUtil.error("ClaimAchievementRewardHandler, playerIdx[" + playerIdx + "] target entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(target, entity -> {
            Builder db_data = target.getDb_Builder();
            if (db_data == null) {
                LogUtil.error("ClaimAchievementRewardHandler, playerIdx[" + playerIdx + "]target dbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, resultBuilder);
                return;
            }

            AchievmentPro.Builder achievementPro = target.getAchievementPro(cfgId);
            int[] ints = achievementCfg.getTargetcount()[index];
            if (ints[0] > achievementPro.getCurPro()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, resultBuilder);
                return;
            }

            if (achievementPro.getClaimedIndexList().contains(index)) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, resultBuilder);
                return;
            }

            if (RewardManager.getInstance().doRewardByRewardId(playerIdx, ints[1],
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Achievement), true) == null) {

                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, resultBuilder);
                return;
            }

            achievementPro.addClaimedIndex(index);
            target.putAchievementPro(achievementPro);

            resultBuilder.addAllClaimedIndex(achievementPro.getClaimedIndexList());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Achievement;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimAchievementReward_VALUE, SC_ClaimAchievementReward.newBuilder().setRetCode(retCode));
    }
}
