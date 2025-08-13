package server.http.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/11/25
 */

@Getter
@Setter
public class PlatformServerActivity {
    /**
     * 活动id
     */
    private long activityId;

    /**
     * 开始展示时间戳 -1为永久展示
     */
    private long startDisTime;

    /**
     * 展示结束时间戳
     */
    private long overDisTime;

    /**
     * 活动开始时间戳
     */
    private long beginTime;

    /**
     * 活动结束时间戳
     */
    private long endTime;

    /**
     * 每日开启时间(分钟数)
     */
    private long dailyBeginTime;

    /**
     * 每日结束时间(分钟数)
     */
    private long dailyEndTime;

    /**
     * 活动类型：1通用活动类型 2通用兑换 28天天充值
     */
    private int type;

    /**
     * 活动标题
     */
    private JSONObject title;

    /**
     * 活动描述
     */
    private JSONObject desc;

    /**
     * 活动使用的图片资源
     */
    private String pictureName;

    /**
     * 活动目标
     */
    private List<PlatformServerSubMission> missions;

    /**
     * 掉落物(兑换活动必须字段)
     */
    private List<PlatformDropInfo> dropInfo;

    /**
     * 兑换活动任务列
     */
    private List<PlatformServerExMission> exMission;

    /**
     * 模板类型
     */
    private int template;

    /**
     * 显示页签
     */
    private int tabType;

    /**
     * 排行榜类型
     */
    private int rankingType;

    /**
     *排行奖励
     */
    private List<PlatformRankingReward> rankingReward;

    /**
     * 活动详情
     */
    private String detail;

    /**
     * 红点类型
     */
    private int redDotType;

    /**
     * tag
     */
    private int tag;

    /**
     * 魔灵降临
     */
    private List<PlatformDemonDescendsRandom> demonDescentsRandom;

    /**
     * 每日充值
     */
    private PlatformDayDayRecharge platformDailyRecharge;

    /**
     * 充值返利百分比
     */
    private int rebateRate;

    private List<PlatformBuyMission> buyMissions;

    /**
     * 阶段奖励
     */
    private List<PlatformStageRewards> stageRewards;

    /**
     * 符文密藏奖励池
     */
    private List<PlatformRuneTreasurePool> runeTreasurePools;

    private List<PlatformDirectPurchaseGift> directPurchaseGifts;

    private PlatformRichMan richMan;

    /**
     * 迷宫刷新间隔
     */
    private long mazeRefreshInterval;

    /**
     * 展示奖励列表
     */
    List<PlatformReward> displayRewards;

    /**
     * 节日boss
     */
    PlatformFestivalBoss festivalBossData ;

    /**
     * 魔灵大躲避
     */
    PlatformPetAvoidanceData petAvoidanceData;

    PlatformStarTreasure starTreasure;
}

@Getter
@Setter
class PlatformActivityLang {
    private int language;
    private String content;
}


