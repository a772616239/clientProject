package common.entity;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 排行榜查询返回
 *
 * @author xiao_FL
 * @date 2019/8/16
 */
@Getter
@Setter
public class RankingQueryResult extends HttpCommonResponse {
    /**
     * 按玩家id查询结果
     */
    private RankingQuerySingleResult assignInfo;

    /**
     * 分页查询结果
     */
    private List<RankingQuerySingleResult> pageInfo;
}
