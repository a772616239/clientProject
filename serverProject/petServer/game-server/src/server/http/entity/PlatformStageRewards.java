package server.http.entity;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020.11.09
 */
@Getter
@Setter
public class PlatformStageRewards {
    /**
     * 序号，唯一
     */
    private int index;

    /**
     * 需要抽取次数(目标次数)
     */
    private int needDrawTimes;

    /**
     * 奖励
     */
    List<PlatformReward> rewards;
}
