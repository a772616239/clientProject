package server.http.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description
 * @Author hanx
 * @Date2020/10/12 0012 10:38
 **/
@Getter
@Setter
public class PlatformDayDayRecharge {
    int dailyTarget; //每日充值需求
    List<List<PlatformReward>> freeRewards; //每日免费奖励
    List<List<PlatformReward>> rechargeRewards; //充值奖励
    String adMessage; //广告语
    List<Integer> rewardWorth;//宝箱价值
}
