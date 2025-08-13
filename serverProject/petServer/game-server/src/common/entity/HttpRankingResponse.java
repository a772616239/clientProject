package common.entity;

import lombok.ToString;

/**平台返回排行查询实体
 * @author xiao_FL
 * @date 2019/8/30
 */
@ToString
public class HttpRankingResponse extends HttpCommonResponse {
    private RankingQueryResult data;

    public RankingQueryResult getData() {
        return data;
    }

    public void setData(RankingQueryResult data) {
        this.data = data;
    }
}
