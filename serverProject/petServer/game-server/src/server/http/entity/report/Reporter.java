package server.http.entity.report;

import lombok.Getter;
import lombok.Setter;
import model.player.util.PlayerUtil;
import protocol.CommentDB.DB_Reporter;

/**
 * @author huhan
 * @date 2020/07/14
 */
@Getter
@Setter
public class Reporter {
    private String playerIdx;

    private String playerName;

    /**
     * 举报类型
     * 0:       //所有类型
     * 1;       //广告  (自动处理)
     * 2;       //非法言论 (自动处理)
     * 3;       //人身攻击
     * 4;       //其他,此项需要填写额外信息
     */
    private int reportType;

    /**
     * 举报额外信息
     */
    private String reportMsg;

    private long reportTime;

    public static Reporter create(DB_Reporter dbReporter) {
        if (dbReporter == null) {
            return null;
        }

        Reporter reporter = new Reporter();
        reporter.setPlayerIdx(dbReporter.getPlayerIdx());
        reporter.setPlayerName(PlayerUtil.queryPlayerName(reporter.getPlayerIdx()));
        reporter.setReportType(dbReporter.getReportTypeValue());
        reporter.setReportMsg(dbReporter.getReportMsg());
        reporter.setReportTime(dbReporter.getReportTime());
        return reporter;
    }
}
