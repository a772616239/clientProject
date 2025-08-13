package model.rank.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import common.entity.DBEntity;

public class RankDb extends DBEntity implements Serializable {
    private static final long serialVersionUID = 3244676383999349677L;

    /**
     * 排行榜成就达成记录
     */
    private List<TargetRank> targetRankAchieve = new ArrayList<>();

    /**
     * 排行榜id
     */
    private int rankId;

    /**
     * 该排行榜是否处理过老数据
     */
    private boolean settleOldData;

    public void addTargetRankAchieve(TargetRank targetRank) {
        if (targetRank == null) {
            return;
        }
        targetRankAchieve.add(targetRank);

    }

    public List<TargetRank> getTargetRankAchieve() {
        return targetRankAchieve;
    }

    public void setTargetRankAchieve(List<TargetRank> targetRankAchieve) {
        this.targetRankAchieve = targetRankAchieve;
    }

    public int getRankId() {
        return rankId;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public boolean isSettleOldData() {
        return settleOldData;
    }

    public void setSettleOldData(boolean settleOldData) {
        this.settleOldData = settleOldData;
    }
}
