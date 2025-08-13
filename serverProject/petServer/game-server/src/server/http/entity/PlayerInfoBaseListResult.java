package server.http.entity;

import com.alibaba.fastjson.JSONObject;
import common.load.ServerConfig;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020/07/08
 */
@Getter
@Setter
public class PlayerInfoBaseListResult {
    private boolean success = false;
    private List<PlatformPlayerBaseInfo> playerInfoList;
    private int totalSize;
    private int pageSize = ServerConfig.getInstance().getPlatformPageMaxSize();

    public void addBaseInfo(PlatformPlayerBaseInfo info) {
        if (info == null) {
            return;
        }
        if (playerInfoList == null) {
            playerInfoList = new ArrayList<>();
        }
        playerInfoList.add(info);
        totalSize += 1;
        if (!success) {
            success = true;
        }
    }


    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }
}
