package server.http.entity;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description 直购礼包
 * @Author hanx
 * @Date2020/10/12 0012 10:38
 **/
@Getter
@Setter
public class PlatformDirectPurchaseGift {

    /**
     * 礼包id
     */
    private long giftId;

    private String giftName ;
    /**
     * 奖励
     */
    private List<PlatformReward> rewards;
    /**
     * 超值额度
     */
    private int overflowValue;
    /**
     * 显示原价(多语言)
     */
    private String originalPrice;

    /**
     * 显示现价(多语言)
     */
    private String nowPrice;

    /**
     * 限购次数
     */
    private int limitBuy;


    /**
     * rechargeProductId
     */
    private int rechargeProductId ;

    /**
     * 充值具体金额
     */
    private int rechargeAmount;

    /**
     * vip经验
     */
    private int vipExp;

    /**
     * 是否每日重置
     */
    private boolean dailyReset;
}
