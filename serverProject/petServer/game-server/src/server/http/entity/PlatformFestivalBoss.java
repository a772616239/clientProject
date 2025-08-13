package server.http.entity;

import lombok.Data;

import java.util.List;

@Data
public class PlatformFestivalBoss {

    /**
     * fightMakeId
     */
    private int fightMakeId;

    /**
     * 赠送单次消耗
     */
    private PlatformConsume presentConsume;

    /**
     * 平台boss宝箱
     */
    private List<PlatformFestivalBossTreasure> treasures;

    /**
     * 赠送随机奖励
     */
    private List<PlatformRandomReward> presentRandomReward;

    /**
     * 赠送必得奖励
     */
    private List<PlatformReward> presentReward;

    /**
     * 分享链接
     */
    private String shareLink;


    /**
     * 赠送积分
     */
    private int presentScore;

    /**
     * 排行榜上榜最低积分
     */
    private int rankMinLimitScore ;

    /**
     * 排行展示人数
     */
    private int showRankNum;

    /**
     * 每日可挑战次数
     */
    private  int dailyChallengeTimes ;

    /**
     * 伤害奖励
     */
    private List<PlatformFestivalBossDamageReward> damageReward;

    /**
     * 指定拥有魔灵
     */
    private int petCfgId ;

    /**
     *额外积分系数（放大1000倍给整数）
     */
    private int exScoreRate;

    /**
     * 商店使用货币类型
     */
    PlatformReward shopCurrency;

    /**
     * 分享奖励
     */
    List<PlatformReward> shareReward;

}
