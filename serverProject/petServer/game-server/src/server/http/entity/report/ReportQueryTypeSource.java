package server.http.entity.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/20
 */
@Getter
@Setter
@AllArgsConstructor
public class ReportQueryTypeSource {
    /**
     * 1:评论
     * 2:聊天
     */
    private int queryType;

    /**
     * 来源：评论
     * 0;       //查询全部
     * 1;		// 宠物评论
     * 2;		// 主线评论
     * 3;		// 无尽尖塔
     * 4;		// 勇气试炼
     * 5;       // boss塔
     * 聊天不填
     */
    private int querySource;

    public ReportQueryTypeSource(int queryType) {
        this.queryType = queryType;
    }
}
