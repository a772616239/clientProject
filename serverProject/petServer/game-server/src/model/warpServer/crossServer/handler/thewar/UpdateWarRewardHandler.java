package model.warpServer.crossServer.handler.thewar;

import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_UpdateWarReward;
import protocol.TheWar.SC_UpdateWarReward;

@MsgId(msgId = MsgIdEnum.CS_GS_UpdateWarReward_VALUE)
public class UpdateWarRewardHandler extends AbstractHandler<CS_GS_UpdateWarReward> {

    @Override
    protected CS_GS_UpdateWarReward parse(byte[] bytes) throws Exception {
        return CS_GS_UpdateWarReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_UpdateWarReward ret, int i) {
        RewardManager.getInstance().doRewardByList(ret.getPlayerIdx(), ret.getRewardsList(), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_TheWar), false);
        SC_UpdateWarReward.Builder builder = SC_UpdateWarReward.newBuilder();
        builder.addAllRewards(ret.getRewardsList());
        builder.addAllWarRewards(ret.getWarRewardsList());
        GlobalData.getInstance().sendMsg(ret.getPlayerIdx(), MsgIdEnum.SC_UpdateWarReward_VALUE, builder);
    }
}
