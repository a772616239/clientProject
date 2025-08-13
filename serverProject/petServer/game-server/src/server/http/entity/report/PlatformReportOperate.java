package server.http.entity.report;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import server.http.entity.report.ReportConst.ReportOperate;

/**
 * @author huhan
 * @date 2020/07/17
 */
@Getter
@Setter
public class PlatformReportOperate {
    /**
     * 1:评论
     * 2:聊天
     */
    private int queryType;


    private List<String> idx;

    /**
     * @see ReportOperate
     * -1:清除当前举报记录
     * 0:无操作
     * 1:屏蔽当前
     * 2:屏蔽当前模块下所有评论
     */
    private int operate;

    public boolean checkParams() {
        if (CollectionUtils.isEmpty(idx)) {
            return false;
        }
        return true;
    }

}
