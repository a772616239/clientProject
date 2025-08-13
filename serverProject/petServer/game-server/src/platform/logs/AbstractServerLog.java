package platform.logs;

import com.alibaba.fastjson.JSONObject;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.time.DateFormatUtils;

import java.io.File;
import java.util.Date;

@Getter
@Setter
public abstract class AbstractServerLog {

    private String cid;

    private String zone;

    private String serverIndex;

    public AbstractServerLog() {
        this.cid = ServerConfig.getInstance().getClientId();
        this.zone = ServerConfig.getInstance().getZone();
        this.serverIndex = String.valueOf(ServerConfig.getInstance().getServer());
    }

    public String toJson() {
        String json = JSONObject.toJSONString(this);
        String replaceStr = "{\"@timestamp\":\"" + formatTime(GlobalTick.getInstance().getCurrentTime()) + "\",";
        return json.replaceFirst("\\{", replaceStr);
    }

    public void init() {
        File dir = new File(LogService.getInstance().URL + "/" + this.getClass().getSimpleName());
        if ((!(dir.exists())) || !(dir.isDirectory())) {
            dir.mkdir();
        }
    }

    public static String formatTime(long time) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
        return DateFormatUtils.format(new Date(time), pattern);
    }
}
