package server.http.entity.report;

/**
 * @author huhan
 * @date 2020/07/20
 */
public class ReportConst {
    /**
     * @author huhan
     * @date 2020/07/20
     */
    public static class ReportQueryType {
        public static final int RQT_COMMENT = 1;
        public static final int RQT_CHAT = 2;
    }

    public static class ReportOperate {
        public static final int RO_CLEAR_REPORT_RECORD = -1;            //清除举报记录
        public static final int RO_NULL = 0;                            //无操作
        public static final int RO_SHIELD_CURRENT = 1;                  //删除当前
        public static final int RO_SHIELD_TOTAL_SOURCE_TYPE = 2;        //删除当前模块全部
    }
}
