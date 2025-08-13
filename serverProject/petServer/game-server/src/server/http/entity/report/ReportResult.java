package server.http.entity.report;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/14
 */
@Getter
@Setter
@NoArgsConstructor
public class ReportResult {
    /**
     * 剩余数量
     */
    private int remainSize;

    private List<Report> reports;

    public void addReport(Report report) {
        if (report == null) {
            return;
        }

        if (reports == null) {
            reports = new ArrayList<>();
        }
        reports.add(report);
    }

    public String toJsonString() {
        return JSONObject.toJSONString(this);
    }
}
