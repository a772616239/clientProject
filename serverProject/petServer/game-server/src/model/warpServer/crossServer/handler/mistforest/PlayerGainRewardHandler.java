package model.warpServer.crossServer.handler.mistforest;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_GainMistForestReward;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_GainMistForestReward_VALUE)
public class PlayerGainRewardHandler extends AbstractHandler<CS_GS_GainMistForestReward> {
    @Override
    protected CS_GS_GainMistForestReward parse(byte[] bytes) throws Exception {
        return CS_GS_GainMistForestReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_GainMistForestReward req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        RewardManager.getInstance().doRewardByRewardId(
                req.getPlayerIdx() ,req.getRewardId(), ReasonManager.getInstance().borrowReason(req.getRewardSource()), true);
        LogUtil.debug("player[" + req.getPlayerIdx() + "] gain reward id =" + req.getRewardId());
    }
}

