package server.http.entity.report;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import model.chatreport.entity.chatreportEntity;
import model.player.util.PlayerUtil;
import protocol.CommentDB.CommentDbData;
import protocol.CommentDB.DB_Reporter;
import server.http.entity.report.ReportConst.ReportQueryType;

/**
 * @author huhan
 * @date 2020/07/14
 */
@Getter
@Setter
public class Report {
    /**
     * 唯一id
     */
    private String idx;

    private String playerIdx;

    private String playerName;

    private String content;

    /**
     * 举报时间,最后一次被举报的时间
     */
    private long reportTime;

    /**
     * 举报玩家
     */
    private List<Reporter> reporters;

    private ReportQueryTypeSource queryTypeSource;

    private void addReporter(Reporter reporter) {
        if(reporter == null) {
            return;
        }
        if (reporters == null) {
            reporters = new ArrayList<>();
        }
        reporters.add(reporter);
    }

    public static Report create(CommentDbData.Builder dbData) {
        if (dbData == null) {
            return null;
        }

        Report report = new Report();
        report.setIdx(String.valueOf(dbData.getCommentId()));
        report.setPlayerIdx(dbData.getPlayerIdx());
        report.setPlayerName(PlayerUtil.queryPlayerName(report.getPlayerIdx()));
        report.setContent(dbData.getContent());

        long lastReportTime = 0;
        for (DB_Reporter value : dbData.getReportsMap().values()) {
            Reporter reporter = Reporter.create(value);
            if (reporter != null) {
                report.addReporter(reporter);
            }

            if (value.getReportTime() > lastReportTime) {
                lastReportTime = value.getReportTime();
            }
        }

        report.setReportTime(lastReportTime);
        return report;
    }

    public static Report create(chatreportEntity entity) {
        if (entity == null) {
            return null;
        }

        Report report = new Report();
        report.setIdx(entity.getIdx());
        report.setPlayerIdx(entity.getLinkplayer());
        report.setPlayerName(PlayerUtil.queryPlayerName(report.getPlayerIdx()));
        report.setContent(entity.getContent());

        long lastReportTime = 0;
        for (DB_Reporter value : entity.getDbBuilder().getReportsMap().values()) {
            Reporter reporter = Reporter.create(value);
            if (reporter != null) {
                report.addReporter(reporter);
            }

            if (value.getReportTime() > lastReportTime) {
                lastReportTime = value.getReportTime();
            }
        }

        report.setReportTime(lastReportTime);
        report.setQueryTypeSource(new ReportQueryTypeSource(ReportQueryType.RQT_CHAT));
        return report;
    }
}
