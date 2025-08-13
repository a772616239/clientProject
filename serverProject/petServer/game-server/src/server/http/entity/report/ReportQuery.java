package server.http.entity.report;

import lombok.Getter;
import lombok.Setter;
import protocol.Comment.CommentTypeEnum;
import server.http.entity.report.ReportConst.ReportQueryType;
import util.LogUtil;

/**
 * 举报查询
 *
 * @author huhan
 * @date 2020/07/14
 */
@Getter
@Setter
public class ReportQuery {

    private ReportQueryTypeSource queryTypeSource;

    /**
     * 是否是第一次查询
     */
    private boolean firstQuery;

    public boolean checkParams() {
        if (queryTypeSource == null) {
            LogUtil.info("");
            return false;
        }
        if (queryTypeSource.getQueryType() != ReportQueryType.RQT_COMMENT
                && queryTypeSource.getQueryType() != ReportQueryType.RQT_CHAT) {
            LogUtil.error("report.ReportQuery.checkParams,error query type");
            return false;
        }
        if (queryTypeSource.getQueryType() == ReportQueryType.RQT_COMMENT) {
            CommentTypeEnum typeEnum = CommentTypeEnum.forNumber(queryTypeSource.getQuerySource());
            if (typeEnum == null) {
                LogUtil.error("ReportQuery.checkParams,error query comment source:" + queryTypeSource.getQuerySource());
                return false;
            }
        }
        return true;
    }
}
