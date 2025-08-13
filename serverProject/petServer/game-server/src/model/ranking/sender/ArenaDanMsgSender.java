package model.ranking.sender;

import com.alibaba.fastjson.JSONObject;
import common.entity.RankingQuerySingleResult;
import lombok.Setter;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.entity.playerEntity;
import protocol.Activity;
import protocol.Activity.PlayerRankingInfo;
import protocol.Activity.PlayerRankingInfo.Builder;
import protocol.Activity.SC_ClaimRanking;

@Setter
public class ArenaDanMsgSender extends CommonRankingMsgSender {

    int dan;

    @Override
    protected long getRankingScore(RankingQuerySingleResult singleResult) {
        return singleResult.getSubScore();
    }

    @Override
    protected PlayerRankingInfo.Builder buildByPlayer(playerEntity player) {
        PlayerRankingInfo.Builder builder = super.buildByPlayer(player);

        arenaEntity arenaEntity = arenaCache.getInstance().getEntity(player.getIdx());
        if (arenaEntity != null) {
            Activity.RankingExInfo.Builder exInfo = Activity.RankingExInfo.newBuilder();
            Activity.ArenaRankingExInfo.Builder detail = Activity.ArenaRankingExInfo.newBuilder();
            detail.setFightAbility(arenaEntity.getDbBuilder().getFightAbility());
            detail.setDan(arenaEntity.getDbBuilder().getDan());
            exInfo.setExInfoEnum(Activity.RankingExInfoEnum.REIE_Arena);
            exInfo.setDetail(detail.build().toByteString());
            builder.setExInfo(exInfo);
        }

        return builder;
    }

    @Override
    protected void subBuildFromExInfo(Builder builder, JSONObject jsonObject) {
        super.subBuildFromExInfo(builder, jsonObject);
        Activity.RankingExInfo.Builder exInfo = Activity.RankingExInfo.newBuilder();
        Activity.ArenaRankingExInfo.Builder detail = Activity.ArenaRankingExInfo.newBuilder();
        if (jsonObject.containsKey("fightAbility")) {
            detail.setFightAbility(jsonObject.getIntValue("fightAbility"));
        }
        if (jsonObject.containsKey("dan")) {
            detail.setDan(jsonObject.getIntValue("dan"));
        }
        exInfo.setExInfoEnum(Activity.RankingExInfoEnum.REIE_Arena);
        exInfo.setDetail(detail.build().toByteString());
        builder.setExInfo(exInfo);
    }

    @Override
    protected void subBuildClaimRanking(SC_ClaimRanking.Builder msgBuilder) {
        super.subBuildClaimRanking(msgBuilder);
        msgBuilder.setDan(dan);
    }
}
