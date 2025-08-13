package model.warpServer.crossServer.handler.thewar;

import cfg.TheWarJobTileConfig;
import cfg.TheWarJobTileConfigObject;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_GetJobTileReward;

@MsgId(msgId = MsgIdEnum.CS_GS_GetJobTileReward_VALUE)
public class GetJobTileRewardHandler extends AbstractHandler<CS_GS_GetJobTileReward> {
    @Override
    protected CS_GS_GetJobTileReward parse(byte[] bytes) throws Exception {
        return CS_GS_GetJobTileReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_GetJobTileReward ret, int i) {
        playerEntity player = playerCache.getByIdx(ret.getPlayerIdx());
        if (player == null) {
            return;
        }
        TheWarJobTileConfigObject cfg = TheWarJobTileConfig.getById(ret.getJobTileLevel());
        if (cfg == null) {
            return;
        }
        List<Reward> rewardList = RewardUtil.parseRewardIntArrayToRewardList(cfg.getJobtilereward());
        if (CollectionUtils.isEmpty(rewardList)) {
            return;
        }
        RewardManager.getInstance().doRewardByList(ret.getPlayerIdx(), rewardList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TheWar), true);
    }
}
