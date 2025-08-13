package model.warpServer.crossServer.handler.thewar;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_GainHolyWater;

@MsgId(msgId = MsgIdEnum.CS_GS_GainHolyWater_VALUE)
public class AddHolyWaterHandler extends AbstractHandler<CS_GS_GainHolyWater> {
    @Override
    protected CS_GS_GainHolyWater parse(byte[] bytes) throws Exception {
        return CS_GS_GainHolyWater.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_GainHolyWater req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        Reward.Builder reward = Reward.newBuilder();
        reward.setRewardType(RewardTypeEnum.RTE_HolyWater);
        reward.setCount(req.getAddHolyWater());
        RewardManager.getInstance().doReward(player.getIdx(), reward.build(),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TheWar), false);
    }
}
