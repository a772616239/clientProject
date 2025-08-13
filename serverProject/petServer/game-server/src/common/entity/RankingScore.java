package common.entity;

import com.alibaba.fastjson.JSONObject;
import common.GameConst.RankingName;
import helper.ObjectUtil;
import helper.StringUtils;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.matcharena.dbCache.matcharenaCache;
import model.matcharena.entity.matcharenaEntity;
import model.pet.dbCache.petCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.ranking.RankingUtils;
import org.apache.commons.collections4.CollectionUtils;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 排行榜更新实体
 * <p>
 * 单键最大值为int最大值， 双键时：最大值不能超过一亿(100000000)
 *
 * @author xiao_FL
 * @date 2019/8/16
 */
@Getter
@Setter
@ToString
public class RankingScore {

    /**
     * 主键,使用playerIdx
     */
    private String primaryKey;

    private List<Long> sortValues;

    /**
     * 扩展信息json字符串，可空
     */
    private String extInfo;

    public RankingScore() {
    }

    public void addScore(long value) {
        if (this.sortValues == null) {
            this.sortValues = new ArrayList<>();
        }
        this.sortValues.add(value);
    }

    /**
     * @param primaryKey
     * @param primaryScore
     * @param subScore
     * @return
     */
    public static RankingScore createScore(String primaryKey, long primaryScore, long subScore, String rankingName) {
        RankingScore score = new RankingScore();
        score.setPrimaryKey(primaryKey);
        score.addScore(primaryScore);
        score.addScore(subScore);
        String exInfo = score.buildExInfo(rankingName).toJSONString();
        score.setExtInfo(exInfo);
        return score;
    }

    public int getIntPrimaryScore() {
        long primaryScore = getPrimaryScore();
        if (primaryScore > Integer.MAX_VALUE) {
            LogUtil.warn("RankingScore.getIntPrimaryScore, score max than Integer.MAX_VALUE");
        }
        return (int) Math.min(Integer.MAX_VALUE, primaryScore);
    }

    public long getPrimaryScore() {
        if (CollectionUtils.isEmpty(sortValues)) {
            return 0;
        } else {
            return sortValues.get(0);
        }
    }

    public long getIntSubScore() {
        return (int) Math.min(Integer.MAX_VALUE, getSubScore());
    }

    public long getSubScore() {
        if (CollectionUtils.size(sortValues) < 2) {
            return 0;
        } else {
            return sortValues.get(1);
        }
    }

    private JSONObject buildExInfo(String rankingName) {
        JSONObject result = new JSONObject();
        playerEntity player = playerCache.getByIdx(getPrimaryKey());
        if (player == null) {
            return result;
        }

        result.put("roleName", player.getName());
        result.put("roleId", player.getIdx());
        result.put("level", player.getLevel());

        if (isCrossRanking(rankingName)) {
            result.put("avatarId", player.getAvatar());
            result.put("avatarBorder", player.getDb_data().getCurAvatarBorder());
            result.put("fightPower", petCache.getInstance().totalAbility(player.getIdx()));
            result.put("titleId", player.getTitleId());
            result.put("vipLv", player.getVip());
            result.put("newTitleId", player.getCurEquipNewTitleId());
        }
        if (RankingUtils.isArenaScoreLocalDanRank(rankingName)) {
            arenaEntity entity = arenaCache.getInstance().getEntity(player.getIdx());
            if (entity != null) {
                result.put("fightAbility", entity.getDbBuilder().getFightAbility());
                result.put("dan", entity.getDbBuilder().getDan());
            }
        }

        if (ObjectUtil.equals(rankingName, RankingName.RN_MatchArena_Cross)) {
            matcharenaEntity entity = matcharenaCache.getInstance().getEntity(getPrimaryKey());
            if (entity != null) {
                result.put("matchArenaWinCount", entity.getDbBuilder().getRankMatchArena().getWinCount());
                result.put("matchArenaFailedCount", entity.getDbBuilder().getRankMatchArena().getFailedCount());
                result.put("matchArenaDanId", entity.getDbBuilder().getRankMatchArena().getDan());
            }
        }
        return result;
    }

    private static final Set<String> CROSS_RANKING_NAME = new ConcurrentSet<>();

    static {
        CROSS_RANKING_NAME.add(GameUtil.buildTransServerRankName(RankingName.RN_MistTransSvrRank));
        CROSS_RANKING_NAME.add(GameUtil.buildTransServerRankName(RankingName.RN_CrazyDuel_Attack));
        CROSS_RANKING_NAME.add(GameUtil.buildTransServerRankName(RankingName.RN_CrazyDuel_Defend));
//        CROSS_RANKING_NAME.add(RankingName.RN_TheWar_KillMonsterCount);
    }

    public boolean isCrossRanking(String rankingName) {
        if (StringUtils.isEmpty(rankingName)) {
            return false;
        }
        return CROSS_RANKING_NAME.contains(rankingName);
    }
}
