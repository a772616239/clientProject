package platform.logs.statistics;

import com.alibaba.fastjson.JSONObject;

public abstract class AbstractStatistics {

    abstract void init();

    public String queryData() {
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("data", this);
        return result.toJSONString();
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    protected double getAvg(long total, long num) {
        if (num == 0L) {
            return 0.0;
        }
        return total * 100 / num / 100.0;
    }

}
