package common.entity;

import com.alibaba.fastjson.JSONObject;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.PetMessage;


@Getter
@Setter
public class PetRankingScore extends RankingScore {
    /**
     * 主键,使用petId
     */
    private String primaryKey;

    private String playerId;


    public PetRankingScore(String playerId) {
        this.playerId = playerId;
    }

    public static RankingScore createScore(String playerId, String primaryKey, long primaryScore, long subScore, String rankingName) {
        PetRankingScore score = new PetRankingScore(playerId);
        score.setPrimaryKey(primaryKey);
        score.addScore(primaryScore);
        if (subScore == 0) {
            score.addScore(Instant.now().toEpochMilli());
        } else {
            score.addScore(subScore);
        }
        String exInfo = score.buildExInfo().toJSONString();
        score.setExtInfo(exInfo);
        return score;
    }

    protected JSONObject buildExInfo() {
        JSONObject result = new JSONObject();
        playerEntity player = playerCache.getByIdx(getPlayerId());
        if (player == null) {
            return result;
        }

        result.put("roleName", player.getName());
        result.put("roleId", player.getIdx());
        result.put("level", player.getLevel());
        result.put("playerId", getPlayerId());

        PetMessage.Pet pet = petCache.getInstance().getPetById(getPlayerId(), getPrimaryKey());
        if (pet == null) {
            return result;
        }
        result.put("petBookId", pet.getPetBookId());

        return result;
    }


}
