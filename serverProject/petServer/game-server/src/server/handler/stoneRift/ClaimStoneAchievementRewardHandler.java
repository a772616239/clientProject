package server.handler.stoneRift;

import cfg.StoneRiftAchievement;
import cfg.StoneRiftAchievementObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftAchievement;
import model.stoneRift.stoneriftEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_ClaimStoneRiftAchievementReward;
import protocol.StoneRift.SC_ClaimStoneRiftAchievementReward;
import util.GameUtil;

import java.util.Collections;
import java.util.List;

import static protocol.MessageId.MsgIdEnum.SC_ClaimStoneRiftAchievementReward_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ClaimStoneRiftAchievementReward_VALUE)
public class ClaimStoneAchievementRewardHandler extends AbstractBaseHandler<CS_ClaimStoneRiftAchievementReward> {

    @Override
    protected CS_ClaimStoneRiftAchievementReward parse(byte[] bytes) throws Exception {
        return CS_ClaimStoneRiftAchievementReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimStoneRiftAchievementReward req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimStoneRiftAchievementReward.Builder msg = buildMsg(playerId, req.getAchievementId());

        GlobalData.getInstance().sendMsg(playerId, SC_ClaimStoneRiftAchievementReward_VALUE, msg);

    }

    private SC_ClaimStoneRiftAchievementReward.Builder buildMsg(String playerId, int achievementId) {
        SC_ClaimStoneRiftAchievementReward.Builder msg = SC_ClaimStoneRiftAchievementReward.newBuilder();

        stoneriftEntity stoneRift = stoneriftCache.getByIdx(playerId);
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (stoneRift == null || target == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }
        DbStoneRiftAchievement achievement = stoneRift.getDB_Builder().getAchievement();
        if (!achievement.getCompleteAchievementIds().contains(achievementId)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_MissionCanNotClaim));
            return msg;
        }
        if (achievement.getClaimedIds().contains(achievementId)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Target_MissionAlreadyClaim));
            return msg;
        }
        StoneRiftAchievementObject cfg = StoneRiftAchievement.getById(achievementId);
        if (cfg == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            return msg;
        }
        List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(cfg.getReward());
        ReasonManager.Reason borrowReason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift, "石头峡谷成就奖励");

        SyncExecuteFunction.executeConsumer(stoneRift, sr -> {
            achievement.getClaimedIds().add(achievementId);
        });
        stoneRift.sendStoneRiftAchievementUpdate(Collections.emptyList());
        RewardManager.getInstance().doRewardByList(playerId, rewards, borrowReason, true);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        return msg;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimStoneRiftAchievementReward_VALUE, SC_ClaimStoneRiftAchievementReward.newBuilder().setRetCode(retCode));

    }
}
