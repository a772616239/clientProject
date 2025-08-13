package server.http.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author xiao_FL
 * @date 2019/11/28
 */
@Getter
@Setter
public class PlatformReward {
    private int rewardType;
    private int id;
    private int count;
}