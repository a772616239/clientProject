package model.ranking.sender;

import com.alibaba.fastjson.JSONObject;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import model.player.entity.playerEntity;
import protocol.Activity.MatchArenaRankingExtendInfo;
import protocol.Activity.PlayerRankingInfo;

/**
 * @author huhan
 * @date 2021/05/25
 */
public class MatchArenaRankingSender extends CommonRankingMsgSender {

    @Override
    protected PlayerRankingInfo.Builder buildByPlayer(playerEntity player) {
        PlayerRankingInfo.Builder builder = super.buildByPlayer(player);

        matcharenaEntity entity = matcharenaCache.getInstance().getEntity(player.getIdx());
        if (entity != null) {
            MatchArenaRankingExtendInfo.Builder matchArenaExInfo = MatchArenaRankingExtendInfo.newBuilder();
            matchArenaExInfo.setWinCount(entity.getDbBuilder().getRankMatchArena().getWinCount());
            matchArenaExInfo.setFailedCount(entity.getDbBuilder().getRankMatchArena().getFailedCount());
            matchArenaExInfo.setDanId(entity.getDbBuilder().getRankMatchArena().getDan());
            builder.setMatchArenaExtendInfo(matchArenaExInfo);
        }

        return builder;
    }

    @Override
    protected void subBuildFromExInfo(PlayerRankingInfo.Builder builder, JSONObject jsonObject) {
        if (builder == null || jsonObject == null) {
            return;
        }
        MatchArenaRankingExtendInfo.Builder matchArenaExInfo = MatchArenaRankingExtendInfo.newBuilder();
        if (jsonObject.containsKey("matchArenaWinCount")) {
            matchArenaExInfo.setWinCount(jsonObject.getIntValue("matchArenaWinCount"));
        }

        if (jsonObject.containsKey("matchArenaFailedCount")) {
            matchArenaExInfo.setFailedCount(jsonObject.getIntValue("matchArenaFailedCount"));
        }

        if (jsonObject.containsKey("matchArenaDanId")) {
            matchArenaExInfo.setDanId(jsonObject.getIntValue("matchArenaDanId"));
        }
        builder.setMatchArenaExtendInfo(matchArenaExInfo);
    }
}
