package common.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import util.LogUtil;

/**
 * 排行榜更新实体
 *
 * @author xiao_FL
 * @date 2019/8/16
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RankingScore {
    /**
     * 主键,使用userId，平台服务器可能有基于userId的功能
     */
    private String primaryKey;

    private List<Long> sortValues;

    /**
     * 扩展信息json字符串，可空
     */
    private String extInfo;

    public void addScore(long value) {
        if (this.sortValues == null) {
            this.sortValues = new ArrayList<>();
        }
        this.sortValues.add(value);
    }

    public RankingScore(String primaryKey, long primaryScore, String extInfo) {
        this.primaryKey = primaryKey;
        addScore(primaryScore);
        addScore(Instant.now().toEpochMilli());
        this.extInfo = extInfo;
    }

    public RankingScore(String primaryKey, long primaryScore) {
        this(primaryKey, primaryScore, null);
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
}
