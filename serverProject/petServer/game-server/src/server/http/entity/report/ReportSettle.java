package server.http.entity.report;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/14
 */
@Getter
@Setter
public class ReportSettle {
    /**
     * 1:评论
     * 2:聊天
     */
    private int queryType;

    private String idx;

    /**
     * 封禁处理信息
     */
    private List<ReportBan> banResult;
}

/**
 *  1;     //角色
 *  2;     //聊天
 *  3;     //禁止评论
 */
class ReportBan {
    private int banType;

    private long endTime;
}
