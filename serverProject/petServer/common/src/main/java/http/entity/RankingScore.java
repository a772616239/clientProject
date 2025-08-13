package http.entity;

import lombok.*;

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

    /**
     * 主分数
     */
    private int primaryScore;

    /**
     * 副分数，可空，(如果为时间）处理成分钟数
     */
    private int subsidiaryScore;

    /**
     * 扩展信息json字符串，可空
     */
    private String extInfo;

    public RankingScore(String primaryKey, long primaryScore, String extInfo) {
        this.primaryKey = primaryKey;
        this.primaryScore = primaryScore > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) primaryScore;
        this.extInfo = extInfo;
    }

    public RankingScore(String primaryKey, long primaryScore) {
        this.primaryKey = primaryKey;
        this.primaryScore = primaryScore > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) primaryScore;
    }
}
