package server.http.entity;

import lombok.Getter;
import lombok.Setter;
import protocol.Common.Consume;

/**
 * @author xiao_FL
 * @date 2019/11/28
 */
@Getter
@Setter
public class PlatformConsume extends PlatformReward{
    private int rewardType;
    private int id;
    private int count;

    public Consume toConsume() {
        return Consume.newBuilder()
                .setRewardTypeValue(rewardType)
                .setId(id)
                .setCount(count)
                .build();
    }
}