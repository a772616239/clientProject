package server.http.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/11/28
 */

@Getter
@Setter
public class PlatformServerSubMission {
    private int index;

    private int subType;

    private int addition;

    private int target;

    private JSONObject name;

    private JSONObject desc;

    private List<PlatformReward> reward;

    private List<PlatformRandomReward> randoms;

    private int randomTimes;

    private long endTimestamp;
}