package http.entity;

import com.alibaba.fastjson.JSONObject;
import common.GameConst.RankingName;
import lombok.Getter;
import lombok.Setter;
import util.ServerConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 排行榜数据更新实体,
 * 此类亲不要使用无参构造器,封装 items，玩家的额外信息需要用到排行榜名
 *
 * @author xiao_FL
 * @date 2019/8/16
 */
@Getter
@Setter
public class RankingUpdateRequest {
    /**
     * 更新排行榜的名称,无该字段请求将无效
     */
    private String rank;

    /**
     * 该字段用于区分数据不互通区服,无该字段请求将无效,
     */
    private int serverIndex;

    /**
     * 排行榜更新数据
     */
    private List<RankingScore> items;

    /**
     * 可选，升序/降序，默认降序
     */
    private int asc = 0;

    public void addItems(RankingScore score, String playerName, String playerId, int playerLvl, int playerAvatar) {
        if (score == null) {
            return;
        }
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(addExInfo(score, playerName, playerId, playerLvl, playerAvatar));
    }

    /**
     * 封装额外信息
     *
     * @param score
     * @param playerName
     * @param playerId
     * @param playerLvl
     * @return
     */
    private RankingScore addExInfo(RankingScore score, String playerName, String playerId, int playerLvl, int playerAvatar) {

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("name", playerName);
        jsonObj.put("roleId", playerId);
        jsonObj.put("level", playerLvl);

        if (RankingName.RN_MistTransSvrRank.equals(this.rank)) {
            jsonObj.put("avatarId", playerAvatar);
        }
        score.setExtInfo(jsonObj.toJSONString());
        return score;
    }

    public RankingUpdateRequest(String rankingName, int serverIndex) {
        this.rank = rankingName;
        this.serverIndex = serverIndex;
    }

    public RankingUpdateRequest(String rankingName) {
        this.rank = rankingName;
        this.serverIndex = ServerConfig.getInstance().getServer();
    }
}
