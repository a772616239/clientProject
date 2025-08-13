package server.handler.endlessSpire;

import cfg.EndlessAchivementConfig;
import cfg.EndlessAchivementConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.EndlessSpire.CS_ClaimSpireAchievementReward;
import protocol.EndlessSpire.EndlessAchiementInfo;
import protocol.EndlessSpire.SC_ClaimSpireAchievementReward;
import protocol.EndlessSpire.SC_ClaimSpireAchievementReward.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.EndlessSpireInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020.09.08
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimSpireAchievementReward_VALUE)
public class ClaimSpireAchievementRewardHandler extends AbstractBaseHandler<CS_ClaimSpireAchievementReward> {
    @Override
    protected CS_ClaimSpireAchievementReward parse(byte[] bytes) throws Exception {
        return CS_ClaimSpireAchievementReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimSpireAchievementReward req, int i) {
        Builder resultBuilder = SC_ClaimSpireAchievementReward.newBuilder();
        EndlessAchivementConfigObject spireAchievementCfg = EndlessAchivementConfig.getByStepid(req.getStepId());
        if (spireAchievementCfg == null || req.getNodeIndex() >= spireAchievementCfg.getRewardlist().length) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimSpireAchievementReward_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);

        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimSpireAchievementReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            int curLv = player.getDb_data().getEndlessSpireInfo().getMaxSpireLv();
            int needLv = spireAchievementCfg.getStartlayer() + req.getNodeIndex() * spireAchievementCfg.getLayergap();
            if (curLv < needLv) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_EndlessSpire_CurAchievementNotFinished));
                gsChn.send(MsgIdEnum.SC_ClaimSpireAchievementReward_VALUE, resultBuilder);
                return;
            }

            boolean exist = false;
            EndlessAchiementInfo.Builder achieveBuilder = null;
            EndlessSpireInfo.Builder infoBuilder = p.getDb_data().getEndlessSpireInfoBuilder();
            for (int index = 0; index < infoBuilder.getClaimedAchievementCount(); index++) {
                EndlessAchiementInfo.Builder achievement = infoBuilder.getClaimedAchievementBuilder(index);
                if (achievement.getStepId() == req.getStepId()) {
                    achieveBuilder = achievement;
                    exist = true;
                    break;
                }
            }
            if (achieveBuilder == null) {
                achieveBuilder = EndlessAchiementInfo.newBuilder().setStepId(req.getStepId());
            }

            if (achieveBuilder.getNodeIndexList().contains(req.getNodeIndex())) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_EndlessSpire_ThisLvAchievementRewardClaimed));
                gsChn.send(MsgIdEnum.SC_ClaimSpireAchievementReward_VALUE, resultBuilder);
                return;
            }

            int rewardId = spireAchievementCfg.getRewardlist()[req.getNodeIndex()];
            List<Reward> rewards = RewardUtil.getRewardsByRewardId(rewardId);
            if (CollectionUtils.isEmpty(rewards)) {
                LogUtil.error("endless achievement id :" + req.getStepId() + ", index:" + req.getNodeIndex()
                        + ", but rewardId:" + rewardId + ", is not have rewards");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
                gsChn.send(MsgIdEnum.SC_ClaimSpireAchievementReward_VALUE, resultBuilder);
                return;
            }

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_EndlessSpire);
            RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

            achieveBuilder.addNodeIndex(req.getNodeIndex());
            if (!exist) {
                infoBuilder.addClaimedAchievement(achieveBuilder);
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimSpireAchievementReward_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Endless;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimSpireAchievementReward_VALUE, SC_ClaimSpireAchievementReward.newBuilder().setRetCode(retCode));
    }
}
