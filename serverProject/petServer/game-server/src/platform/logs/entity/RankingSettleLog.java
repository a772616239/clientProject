package platform.logs.entity;

import com.alibaba.fastjson.JSONObject;
import common.entity.RankingQuerySingleResult;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.AbstractServerLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.LanguageEnum;
import protocol.LoginProto.ClientData.Builder;
import protocol.Server.ServerActivity;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020.10.15
 * 排行榜结算日志,只打印发放了奖励的玩家
 */
@Getter
@Setter
@NoArgsConstructor
public class RankingSettleLog extends AbstractServerLog {

    private long activityId;
    private String activityName;
    /**
     * 排行榜类型名
     */
    private String rankingName;
    private long activityBeginTime;
    private long activityEndTime;

    List<PlayerRankingLog> rankingInfoList = new ArrayList<>();

    /**
     *
     * @param activity
     * @param rankinInfo  只包含发放了奖励的玩家
     */
    public RankingSettleLog(ServerActivity activity, List<RankingQuerySingleResult> rankinInfo) {
        if (activity == null || CollectionUtils.isEmpty(rankinInfo)) {
            return;
        }

        LogUtil.debug("RankingSettleLog.RankingSettleLog, rankingInfo:" + JSONObject.toJSONString(rankinInfo));

        this.activityId = activity.getActivityId();
        this.activityName = GameUtil.getLanguageStr(activity.getTitle(), LanguageEnum.LE_SimpleChinese);
        this.rankingName = StatisticsLogUtil.getRankingTypeName(activity.getRankingType());
        this.activityBeginTime = activity.getBeginTime();
        this.activityEndTime = activity.getEndTime();

        for (RankingQuerySingleResult rankingQuerySingleResult : rankinInfo) {
            this.rankingInfoList.add(new PlayerRankingLog(rankingQuerySingleResult));
        }
    }
}

@Getter
@Setter
class PlayerRankingLog {
    private String userId;
    private String playerIdx;
    private String playerName;
    private String playerChannel;
    private int playerShortId;
    /**
     * 玩家排行
     */
    private int playerRanking;

    private long rankingPrimaryScore;

    public PlayerRankingLog(RankingQuerySingleResult result) {
        if (result == null) {
            LogUtil.debug("platform.logs.entity.PlayerRankingLog.PlayerRankingLog, result is null");
            return;
        }

        playerEntity player = playerCache.getByIdx(result.getPrimaryKey());
        if (player == null) {
            LogUtil.debug("platform.logs.entity.PlayerRankingLog.PlayerRankingLog, playerIdx is not exist:" + result.getPrimaryKey());
            return;
        }

        this.userId = player.getUserid();
        this.playerIdx = player.getIdx();
        this.playerName = player.getName();
        Builder clientData = player.getClientData();
        if(clientData != null) {
            this.playerChannel = clientData.getChannel();
        }
        this.playerShortId = player.getShortid();
        this.playerRanking = result.getRanking();
        this.rankingPrimaryScore = result.getPrimaryScore();
    }
}
