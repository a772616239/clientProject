package server.handler.ranking;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.util.PlayerUtil;
import model.ranking.ranking.RankingTargetManager;
import protocol.Activity;
import protocol.Common;
import protocol.MessageId;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimRankingTargetRewardInfo_VALUE)
public class ClaimRankingTargetRewardInfoHandler extends AbstractBaseHandler<Activity.CS_ClaimRankingTargetRewardInfo> {
    @Override
    protected Activity.CS_ClaimRankingTargetRewardInfo parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimRankingTargetRewardInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimRankingTargetRewardInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        if (!PlayerUtil.queryFunctionUnlock(playerIdx, Common.EnumFunction.EF_RankingEntrance)) {
            sendEmptyMsg(gsChn);
            return;
        }
        RankingTargetManager.getInstance().sendPlayerRankTargetRewardInfo(req.getRankingType(), playerIdx);
    }

    private void sendEmptyMsg(GameServerTcpChannel gsChn) {
        Activity.SC_ClaimRankingTargetRewardInfo msg = Activity.SC_ClaimRankingTargetRewardInfo.getDefaultInstance();
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimRankingTargetRewardInfo_VALUE, msg.toBuilder());
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}