package model.ranking.sender;

import common.entity.RankingQuerySingleResult;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import protocol.Activity.EnumRankingType;

/**
 * @author huhan
 * @date 2020/12/11
 */
public abstract class AbstractRankingMsgSender<T> {
    /**
     * 默认排行榜展示大小
     */
    public static final int DEFAULT_RANKING_DIS_SIZE = 100;

    @Getter
    @Setter
    private int disSize = DEFAULT_RANKING_DIS_SIZE;

    /**
     * 用于存放需要展示的数据
     */
    @Getter
    private final List<T> disInfoList = new ArrayList<>();

    /**
     * 当排行榜更新后的回调函数
     *
     * @param newResults
     */
    public synchronized void updateRanking(List<RankingQuerySingleResult> newResults) {
        if (newResults == null) {
            return;
        }

        clear();

        for (RankingQuerySingleResult result : newResults) {
            if (result.getRanking() <= this.disSize) {
                T disInfo = buildRankingDisInfo(result);
                if (disInfo != null) {
                    this.disInfoList.add(disInfo);
                }
            }
        }
    }

    public void clear() {
        this.disInfoList.clear();
    }

    /**
     * 构建排行榜展示数据
     *
     * @param singleResult
     * @return
     */
    public abstract T buildRankingDisInfo(RankingQuerySingleResult singleResult);

    /**
     * 发送排行榜到指定玩家
     */
    public abstract void sendRankingInfo(String playerIdx, RankingQuerySingleResult playerRankinInfo, EnumRankingType rankingType);
}
