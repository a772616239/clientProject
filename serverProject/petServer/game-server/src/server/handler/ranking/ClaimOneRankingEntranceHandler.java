package server.handler.ranking;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.ranking.RankingManager;
import protocol.Activity;
import protocol.Common;
import protocol.MessageId;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimOneRankingEntrance_VALUE)
public class ClaimOneRankingEntranceHandler extends AbstractBaseHandler<Activity.CS_ClaimOneRankingEntrance> {
    @Override
    protected Activity.CS_ClaimOneRankingEntrance parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimOneRankingEntrance.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimOneRankingEntrance req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Activity.RankingEntrance rankingEntrance = RankingManager.getInstance().getOnePlayerRankingEntrance(playerIdx, req.getRankType());
        Activity.SC_ClaimOneRankingEntrance.Builder msg = Activity.SC_ClaimOneRankingEntrance.newBuilder().setRankingEntrance(rankingEntrance);
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimOneRankingEntrance_VALUE, msg);
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }
}