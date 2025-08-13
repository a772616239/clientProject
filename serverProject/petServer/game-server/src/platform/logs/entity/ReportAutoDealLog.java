package platform.logs.entity;

import common.IdGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.AbstractServerLog;
import server.http.entity.report.ReportQueryTypeSource;

/**
 * @author huhan
 * @date 2020/07/20
 */
@Getter
@Setter
public class ReportAutoDealLog extends AbstractServerLog {
        private String uniqueId;

    private String playerIdx;
    private String playerName;

    private String idx;

    ReportQueryTypeSource typeSource;
    /**
     * 被举报类型
     */
    private List<Integer> reportType;

    private String content;

    /**
     * 被举报次数
     */
    private int reportedTimes;

    private long dealTime;

    /**
     *   1;     //角色
     *   2;     //聊天
     *   3;     //禁止评论
     */
    private int banType;

    /**
     * 封禁结束时间
     */
    private long endTime;

    public ReportAutoDealLog() {
        this.uniqueId = IdGenerator.getInstance().generateId();
    }

    public void addReportType(int type) {
        if (reportType == null) {
            reportType = new ArrayList<>();
        }
        if (reportType.contains(type)) {
            return;
        }
        reportType.add(type);
    }

    public void addReportType(Collection<Integer> types) {
        if (CollectionUtils.isEmpty(types)) {
            return;
        }
        types.forEach(this::addReportType);
    }
}
