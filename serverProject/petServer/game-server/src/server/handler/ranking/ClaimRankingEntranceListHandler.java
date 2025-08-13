package server.handler.ranking;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.ranking.RankingManager;
import protocol.Activity;
import protocol.Common;
import protocol.MessageId;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimRankingEntranceList_VALUE)
public class ClaimRankingEntranceListHandler extends AbstractBaseHandler<Activity.CS_ClaimRankingEntranceList> {
    @Override
    protected Activity.CS_ClaimRankingEntranceList parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimRankingEntranceList.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimRankingEntranceList req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Activity.SC_ClaimRankingEntranceList.Builder msg = Activity.SC_ClaimRankingEntranceList.newBuilder();
        msg.addAllRankingEntrance(RankingManager.getInstance().getPlayerAllRankingEntrance(playerIdx));
        msg.addAllSeasonInfo(ActivityManager.getInstance().queryAllSeasonInfo());
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimRankingEntranceList_VALUE, msg);
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}