package common.entity;


import lombok.Getter;
import lombok.Setter;

/**
 * @author xiao_FL
 * @date 2019/8/16
 */
@Getter
@Setter
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
}
