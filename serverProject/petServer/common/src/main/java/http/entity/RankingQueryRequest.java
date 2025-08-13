package http.entity;


/**
 * @author xiao_FL
 * @date 2019/8/16
 */
public class RankingQueryRequest extends RankingServerMsg {
    /**
     * 必填
     * 获取排行数据的第几页
     */
    private Integer page;

    /**
     * 必填
     * 取值范围[10,200]
     */
    private Integer size;

    /**
     * 选填
     * 用于查询指定的playerId对应的排名信息
     */
    private String assignPrimaryKey;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getAssignPrimaryKey() {
        return assignPrimaryKey;
    }

    public void setAssignPrimaryKey(String assignPrimaryKey) {
        this.assignPrimaryKey = assignPrimaryKey;
    }
}
