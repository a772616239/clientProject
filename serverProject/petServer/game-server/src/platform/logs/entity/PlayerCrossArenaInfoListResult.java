package platform.logs.entity;

import com.alibaba.fastjson.JSONObject;
import common.load.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import server.http.entity.PlatformCrossArenaInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huhan
 * @date 2020/07/08
 */
@Getter
@Setter
public class PlayerCrossArenaInfoListResult {
    private boolean success = false;
    private List<PlatformCrossArenaInfo> playerInfoList;
    private int totalSize;
    private int pageSize = ServerConfig.getInstance().getPlatformPageMaxSize();

    public void addBaseInfo(PlatformCrossArenaInfo info) {
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
