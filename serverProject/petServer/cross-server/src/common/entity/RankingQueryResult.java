package common.entity;

import java.util.List;

/**
 * 排行榜查询返回
 *
 * @author xiao_FL
 * @date 2019/8/16
 */
public class RankingQueryResult extends HttpCommonResponse {
    /**
     * 按玩家id查询结果
     */
    private RankingQuerySingleResult assignInfo;

    /**
     * 分页查询结果
     */
    private List<RankingQuerySingleResult> pageInfo;

    public RankingQuerySingleResult getAssignInfo() {
        return assignInfo;
    }

    public void setAssignInfo(RankingQuerySingleResult assignInfo) {
        this.assignInfo = assignInfo;
    }

    public List<RankingQuerySingleResult> getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(List<RankingQuerySingleResult> pageInfo) {
        this.pageInfo = pageInfo;
    }
}
