package http.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xiao_FL
 * @date 2019/8/21
 */

@Getter
@Setter
public class RankingServerMsg {
    /**
     * 必填
     * 更新排行榜的名称,无该字段请求将无效
     */
    private String rank;

    /**
     * 必填
     * 该字段用于区分数据不互通区服,无该字段请求将无效
     */
    private Integer serverIndex;

    /**
     * 可选，升序/降序，默认降序
     */
    private int asc = 0;

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getServerIndex() {
        return serverIndex;
    }

    public void setServerIndex(Integer serverIndex) {
        this.serverIndex = serverIndex;
    }

    public int getAsc() {
        return asc;
    }

    public void setAsc(int asc) {
        this.asc = asc;
    }
}
