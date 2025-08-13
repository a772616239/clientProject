package server.http.entity;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2020-11-23
 */
@Getter
@Setter
public class PlatformBuyMission {
    private int index;
    private int limitBuy;
    private long endTimestamp;
    private PlatformConsume price;
    /**
     * 折扣 百分比
     */
    private int discount;
    private List<PlatformReward> rewards;
    private String title;
    private int specialType;
}
