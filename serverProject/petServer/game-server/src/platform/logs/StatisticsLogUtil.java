package platform.logs;

import cfg.Head;
import cfg.Item;
import cfg.PetBaseProperties;
import cfg.PetFragmentConfig;
import cfg.PetGemNameIconConfig;
import cfg.PetRuneProperties;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import platform.logs.LogClass.ConsumeLog;
import platform.logs.LogClass.PetLog;
import platform.logs.LogClass.PetPropertyLog;
import platform.logs.LogClass.RewardLog;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.EnumRankingType;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.SC_EnterFight;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.Mail.MailStatusEnum;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetProperty;
import protocol.PetMessage.PetPropertyEntity;
import protocol.ResourceCopy.ResourceCopyTypeEnum;
import protocol.Shop.ShopTypeEnum;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 该类中所有map初始化后只读
 *
 * @author huhan
 */
public class StatisticsLogUtil {

    private static final Map<RewardTypeEnum, String> REWARD_TYPE_NAME_MAP = new EnumMap<>(RewardTypeEnum.class);

    static {
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Gold, "金币");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Diamond, "魔石");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Item, "道具");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_PetFragment, "宠物碎片");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Rune, "符文");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Pet, "宠物");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Avatar, "头像");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_VIPEXP, "VIP经验");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_EXP, "经验");

        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Coupon, "魔晶");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_MistStamina, "迷雾森林体力");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_AvatarBorder, "头像框");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_VoidStone, "虚空宝石");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_TheWarItem, "远征道具");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Gem, "宝石");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_HolyWater, "圣水");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_ApocalypseBlessing, "天启点数");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_MineExp, "矿区经验");

        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_PointInstance, "积分副本的积分");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_MonthCard, "月卡 （id:1普通月卡 2高级月卡）");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_AdvancedFeats, "高级功勋");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_RechargeProduct, "充值道具");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_NewTitleSystem, "新称号系统");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Inscription, "铭文");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_Train, "训练营专用");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_CrossArenaGrade, "擂台赛荣誉积分");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_AdvancedFeats_WUJIN, "无尽尖塔功勋道具");
        REWARD_TYPE_NAME_MAP.put(RewardTypeEnum.RTE_AdvancedFeats_XUKONG, "虚空秘境功勋道具");
    }

    public static String getRewardTypeName(RewardTypeEnum typeEnum) {
        if (typeEnum == null || typeEnum == RewardTypeEnum.RTE_Null) {
            return "";
        }

        String name = REWARD_TYPE_NAME_MAP.get(typeEnum);
        if (name == null) {
            LogUtil.warn("StatisticsLogUtil.getRewardTypeName, Reward Type is not link name, type:" + typeEnum);
            return "";
        }
        return name;
    }

    private final static Map<RewardSourceEnum, String> REWARD_SOURCE_NAME_MAP = new EnumMap<>(RewardSourceEnum.class);

    static {
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MainLineCheckPoint, "主线闯关");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_EndlessSpire, "无尽尖塔");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ForeignInvasion, "外敌入侵");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MistForest, "迷雾深林");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Mail, "邮件");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ItemBag, "背包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DrawCard_Common, "普通抽卡");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetMission, "宠物委托");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetStore, "宠物商店");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Patrol, "巡逻队");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BlackMarket, "黑市");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DailyMission, "每日任务");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_VIPLvUp, "VIP升级");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BraveChallenge, "勇气试炼");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_AncientCall, "远古召唤");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetCollection, "宠物收集");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MineFight, "矿区争夺");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ResCopy, "资源副本");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MainLine_OnHook, "主线挂机");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Activity, "活动");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetFragment, "宠物碎片");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_RuneBagLvlUp, "符文背包升级");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BraveChallengeStore, "勇气试炼商店");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetBagLvlUp, "宠物背包升级");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetRuneLvUp, "宠物符文升级");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetDischarge, "宠物放生");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetTransfer, "宠物转换");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_GM, "GM");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PointInstance, "积分副本");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CumuSignIn, "累积签到");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Vagrant, "流浪者小屋");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_WortyGift, "超值礼包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_LimitGift, "限购礼包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Recharge, "充值");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Friend, "好友");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ActiveCode, "激活码");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Achievement, "成就");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_NewBee, "新手引导");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_AlterName, "改名");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_GoldExchange, "金币兑换");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ExchangeLowBook, "低级召唤书兑换");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Expire, "过期");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Sell, "贩卖");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Use, "使用");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetLvUp, "宠物升级");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetStarUp, "宠物升星");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetAwake, "宠物觉醒");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Platform, "平台");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CDK, "礼品码");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DrawCard_High, "高级抽卡");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_SellRune, "符文出售");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Novice, "新手积分");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_WipeResCopy, "资源副本扫荡");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_EquipRune, "符文穿戴");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_UnEquipRune, "卸下符文");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BuyTeam, "购买小队");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MineExploit, "开采矿区");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MineGift, "矿区交互奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MistForestTeamReward, "迷雾森林击败boss后队友奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_NewBeeChallenge, "新手闯关");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MainLineMission, "主线阶段性奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MistForestSeasonTask, "迷雾森林领取赛季任务奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetReBorn, "宠物重生");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Arena, "竞技场");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Buy, "购买");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ActivityBoss, "活动boss");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DrawCard_FriensShip, "友情抽卡");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ScratchLottery, "刮刮乐");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_NormalMonthCardReward, "初级月卡奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_AdvanceMonthCardReward, "高级月卡奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CumuRecharge, "主线闯关");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_FriendHelp, "好友助阵");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_FeatsReward, "功勋奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ArenaStore, "竞技场商店");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_WishingWell, "许愿池");

        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_GrowthFund, "成长基金");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CouponExchange, "魔晶兑换");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DailyGift, "日礼包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_WeeklyGift, "周礼包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MonthlyGift, "月礼包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BuyItem_Activity, "通用购买活动");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_OnlineTime, "在线活动奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Ads_FreeAdsBonus, "普通广告");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Ads_WheelBonus, "转盘广告");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BossTower, "boss爬塔");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_GrowthTrack, "成长轨迹");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PlayerSkillLvUp, "玩家技能升级");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_VoidStoneLvUp, "虚空宝石升级");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_VoidStoneConvert, "虚空宝石转换");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Pet_RarityUp, "宠物品质升级");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TimeLimitGift, "限时礼包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TheWar, "战戈");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Report, "举报");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_VoidStoneActive, "虚空宝石激活");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_VipExpCard, "vip经验卡");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_NeeBeeGift, "新手礼包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Mist_TimeLimitMission, "迷雾深林限时任务");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PlayerReturn, "老玩家回归");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PlayerSkillStarUp, "玩家技能升星");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ArenaMission, "竞技场任务");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetGemLvUp, "宝石精炼");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetGemStarUp, "宝石升星");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetGemSale, "宝石售卖");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_EquipGem, "穿戴宝石");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_UnEquipGem, "卸下宝石");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DemonDescends_Recharge, "魔灵降临充值奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DemonDescends_Buy, "魔灵降临购买");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DemonDescends_DailyMission, "魔灵降临每日任务");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DemonDescends, "魔灵降临");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DayDayRecharge_Free, "天天充值活动免费");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DayDayRecharge_Recharge, "天天充值活动充值奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_HadesTreasure, "哈迪斯的宝藏");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DayDayRecharge_Settle, "天天充值活动结算");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ZeroCostPurchase, "0元购");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DirectPurchaseGift, "直购礼包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_RuneTreasure, "符文密藏");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DailyFirstPayRecharge, "每日首充充值奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DailyFirstPayExplore, "每日首充宝藏塔探索奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MonthCardPurchase, "购买月卡");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_FeatsPurchase, "购买功勋");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ChallengePlayer, "玩家挑战");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TargetRank, "目标排行榜");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_GloryRoad, "荣耀之路");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_GloryRoadQuiz, "荣耀之路竞猜");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_RedBag, "红包");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MistMaze, "迷宫活动奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MistGhostBuster, "抓鬼活动奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_RichMan, "大富翁");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MainlineAdditionRewards, "主线额外奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MatchArena, "匹配竞技场");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_InscriptionCompose, "铭文合成");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_InscriptionEquip, "铭文装备");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_InscriptionUnEquip, "铭文卸装");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TRAINBUYSHOP, "训练场道具购买");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TRAIN_POINT, "训练场通过点给与");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TRAIN_BATTLE, "训练场战斗给与");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TRAIN_RANK, "训练场排行榜");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ResourceRecycle, "资源回收");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetEvolve, "宠物进化");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BosstowerBuyTime, "boss塔购买次");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MainLinePlot, "主线剧情");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_HelpPet, "助阵宠物");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MatchArenaleitai, "匹配竞技场擂台");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_FARMMINE_USEITEM, "矿区农场使用锄头");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_FARMMINE_INS, "矿区农场累计奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_FARMMINE_OFFER, "矿区农场竞拍");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_FARMMINE_FRUIT, "矿区农场收获");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_FARMMINE_STEAL, "矿区农场偷取");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MAGICTHRON, "魔法王座");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MistForest_BossActivity, "迷雾森林boss活动");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TRAIN_ROOL, "训练场祝福卡抽取消耗");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_OFFER_REWARD, "悬赏任务发布消耗");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_OFFER_REWARD_FIGHT, "悬赏任务挑战消耗");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_OFFER_REWARD_OVER, "悬赏任务发布者领奖");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_OFFER_REWARD_BATTLE, "悬赏任务挑战者奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossAreanUP, "手动上擂");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossAreanEVENT, "擂台事件");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossAreanPvp_PAY, "切磋支付");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossAreanPvp_BACK, "切磋退回");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossAreanPvp_GET, "切磋获得");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_LT_CP, "擂台组队");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossAreanHonor, "擂台成就");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MainLineKeyNode, "关键节点");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_TOPPAYRANK, "巅峰赛");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CROSSARENA_WORSHIP, "擂台膜拜");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrazyDuel, "疯狂对决");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CROSSARENA_GRADE, "擂台积分奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BOSSTOWER_SWEEPITEM, "风暴之塔扫荡道具消耗");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Pet_AutoFree, "自动分解");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_ItemCard_day, "道具卡每日奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_BraveReset, "契约者试炼重置");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_StoneRift, "石头峡谷");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_StoneRift_Steal, "石头峡谷偷取");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_StoneRift_Unlock, "石头峡谷解锁");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MagicThronDayReward, "魔法王座每日结算奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossArenaWinTask, "擂台赛连胜领奖");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossArenaDailyMission, "擂台赛日常任务");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossArenaWeeklySettle, "擂台赛周结算");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_CrossArena10Win, "擂台赛十连胜");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_DrawCard_New, "上古神庙抽卡");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_EpisodePlot, "插曲奖励");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_Train_Task, "训练营积分任务");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_PetAvoidance, "魔灵大躲避活动");
        REWARD_SOURCE_NAME_MAP.put(RewardSourceEnum.RSE_MakeRune, "打造符文");
    }

    public static String getRewardSourceName(RewardSourceEnum source) {
        if (source == null || source == RewardSourceEnum.RSE_Null) {
            return "";
        }

        String name = REWARD_SOURCE_NAME_MAP.get(source);
        if (name == null) {
            LogUtil.warn("StatisticsLogUtil.getRewardSourceName, Reward source Type is not link name, type:" + source);
            return "";
        }
        return name;
    }

    private static final Map<MailStatusEnum, String> MAIL_STATUS_NAME_MAP = new EnumMap<>(MailStatusEnum.class);

    static {
        MAIL_STATUS_NAME_MAP.put(MailStatusEnum.MSE_NoAttachmentUnread, "无附件未读");
        MAIL_STATUS_NAME_MAP.put(MailStatusEnum.MSE_AttachmentReadClaimed, "有附件已读已领取");
        MAIL_STATUS_NAME_MAP.put(MailStatusEnum.MSE_AttachmentUnRead, "有附件未读");
        MAIL_STATUS_NAME_MAP.put(MailStatusEnum.MSE_AttachmentReadUnclaimed, "有附件已读未领取");
        MAIL_STATUS_NAME_MAP.put(MailStatusEnum.MSE_NoAttachmentRead, "无附件已读");
    }

    public static String getMailStateName(MailStatusEnum statusEnum) {
        if (statusEnum == null) {
            return "";
        }
        String name = MAIL_STATUS_NAME_MAP.get(statusEnum);
        if (name == null) {
            LogUtil.warn("StatisticsLogUtil.getMailStateName, mail status Type is not link name, type:" + statusEnum);
            return "";
        }
        return name;
    }

    private static final Map<ShopTypeEnum, String> SHOP_TYPE_NAME_MAP = new EnumMap<>(ShopTypeEnum.class);

    static {
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_Pet, "魔灵商店");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_BlackMarket, "精品商店");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_CourageTrial, "勇气试炼商店");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_Vagrant, "活动商店");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_Arena, "竞技场商店");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_ScaleMan, "虚空商人");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_ScaleShop, "虚空商城");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_MistForest, "迷雾森林商店");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_CrossArenaVip, "新擂台赛特权商店");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_CrossArena, "新擂台赛代币商店");
        SHOP_TYPE_NAME_MAP.put(ShopTypeEnum.STE_StoneRiftS, "矿区商店");
    }

    public static String getShopName(ShopTypeEnum shopType) {
        if (shopType == null) {
            return "";
        }
        String name = SHOP_TYPE_NAME_MAP.get(shopType);
        if (name == null) {
            LogUtil.warn("StatisticsLogUtil.getShopName, shop Type is not link name, type:" + shopType);
            return "";
        }
        return name;
    }

    private static final Map<PetProperty, String> PET_PROPERTY_NAME_MAP = new EnumMap<>(PetProperty.class);

    static {
        PET_PROPERTY_NAME_MAP.put(PetProperty.ATTACK, "攻击");
        PET_PROPERTY_NAME_MAP.put(PetProperty.DEFENSIVE, "防御");
        PET_PROPERTY_NAME_MAP.put(PetProperty.HEALTH, "最大血量");
        PET_PROPERTY_NAME_MAP.put(PetProperty.CRITICAL_RATE, "暴击率");
        PET_PROPERTY_NAME_MAP.put(PetProperty.CRITICAL_DAMAGE, "暴击伤害");
        PET_PROPERTY_NAME_MAP.put(PetProperty.ACCURACY, "精确率");
        PET_PROPERTY_NAME_MAP.put(PetProperty.MISS, "闪避");
        PET_PROPERTY_NAME_MAP.put(PetProperty.SPEED, "速度");
        PET_PROPERTY_NAME_MAP.put(PetProperty.ATTACK_SPEED, "攻速");
        PET_PROPERTY_NAME_MAP.put(PetProperty.Range, "射程");
        PET_PROPERTY_NAME_MAP.put(PetProperty.CRIT_RATE_Resistance, "抗暴击");
        PET_PROPERTY_NAME_MAP.put(PetProperty.CRIT_DAMAGE_Resistance, "抗爆伤");
        PET_PROPERTY_NAME_MAP.put(PetProperty.AllMainProp, "主属性加成(仅攻防血)");
        PET_PROPERTY_NAME_MAP.put(PetProperty.Current_Health, "宠物当前血量千分比");
        PET_PROPERTY_NAME_MAP.put(PetProperty.ExtendAttackRate, "附加攻强千分比");
        PET_PROPERTY_NAME_MAP.put(PetProperty.ExtendDefenceRate, "附加防御千分比");
        PET_PROPERTY_NAME_MAP.put(PetProperty.ExtendHealthRate, "附加生命千分比");
        PET_PROPERTY_NAME_MAP.put(PetProperty.ExtendCriticalRateRate, "附加暴击率千分比");
    }

    public static String getPropertyName(PetProperty property) {
        if (property == null) {
            return "";
        }

        String name = PET_PROPERTY_NAME_MAP.get(property);
        if (name == null) {
            LogUtil.warn("StatisticsLogUtil.getPropertyName, PetProperty Type is not link name, type:" + property);
            return "";
        }
        return name;
    }

    public static String getPropertyName(int propertyType) {
        return getPropertyName(PetProperty.forNumber(propertyType));
    }

    private static final Map<EnumFunction, String> FUNCTION_TYPE_NAME_MAP = new EnumMap<>(EnumFunction.class);

    static {
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.DrawCard, "抽卡");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.BlackMarket, "精品商店");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.ResCopy, "遗迹副本");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.ForeignInvasion, "外敌入侵");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.Endless, "无尽尖塔");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.MistForest, "迷雾森林");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.PetEvolution, "宠物觉醒");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.PetDelegate, "宠物委托");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.Patrol, "巡逻队");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.CourageTrial, "契约者试炼");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.MiningArea, "矿区");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.WordChat, "聊天");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.AncientCall, "远古召唤");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.MainLine, "主线闯关");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.GoldExchange, "金币兑换");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.PetDischarge, "宠物放生");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.Arena, "竞技场");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.DrawCard_AncientCall, "高级抽卡轮盘抽卡");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.Comment, "通用评论");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.BossTower, "Boss塔");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.PetVoidStone, "宠物虚空宝石");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.TheWar, "远征");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.ArtifactRes, "神器遗迹");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.SoulRes, "灵魂遗迹");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.RuinsRes, "觉醒遗迹");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.RelicsRes, "生命遗迹");
        FUNCTION_TYPE_NAME_MAP.put(EnumFunction.GoldenRes, "黄金遗迹");
    }

    public static String getFunctionName(EnumFunction function) {
        if (function == null || function == EnumFunction.NullFuntion) {
            return "";
        }

        String name = FUNCTION_TYPE_NAME_MAP.get(function);
        if (name == null) {
            LogUtil.warn("StatisticsLogUtil.getFunctionName, function Type is not link name, type:" + function);
            return "";
        }
        return name;
    }

    private static final Map<BattleSubTypeEnum, String> BATTLE_SUB_TYPE_NAME_MAP = new EnumMap<>(BattleSubTypeEnum.class);

    static {
//        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_PureClient, "存客户端战斗");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_MainLineCheckPoint, "主线关卡");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_EndlessSpire, "无尽尖塔");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_ForeignInvasion, "外敌入侵");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_MistForest, "迷雾森林");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_Patrol, "巡逻队");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_BreaveChallenge, "勇气试炼");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_MineFight, "矿区争夺");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_ResourceCopy, "资源副本");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_PointCopy, "积分副本");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_Arena, "竞技场");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_ActivityBoss, "活动boss");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_BossTower, "boss爬塔");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_TheWar, "战戈");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_NewForeignInvasion, "新外敌入侵");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_ChallengePlayer, "挑战玩家");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_GloryRoad, "荣耀之路");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_MatchArena, "匹配竞技场");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_Training, "训练场");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_MatchArenaLeitai, "竞技场擂台赛");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_ArenaMatchNormal, "匹配竞技场匹配赛");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_MatchArenaRanking, "匹配竞技场排位赛");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_magicthron, "魔法王座");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_OfferReward, "悬赏任务小队");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_CrossArenaEvent, "擂台赛事件");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_CrossArenaPvp, "擂台赛切磋");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_LTCpTeam, "擂台赛组队玩法战斗");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_CrazyDuel, "擂台赛疯狂对决");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_CrossArenaTop, "擂台赛巅峰对决");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_EpisodeGeneral, "通用插曲战斗(玩家魔灵用主线编队)");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_EpisodeSpecial, "特殊插曲战斗(用系统魔灵战斗)");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_CrossArenaLeiTaiBoss, "擂台赛守关boss");
        BATTLE_SUB_TYPE_NAME_MAP.put(BattleSubTypeEnum.BSTE_FestivalBoss, "节日Boss");
    }

    public static String getBattleSubTypeName(BattleSubTypeEnum battleType) {
        if (battleType == null || battleType == BattleSubTypeEnum.BSTE_Null) {
            return "";
        }

        String name = BATTLE_SUB_TYPE_NAME_MAP.get(battleType);
        if (name == null) {
            LogUtil.warn("StatisticsLogUtil.getBattleSubTypeName, battle sub Type is not link name, type:" + battleType);
            return "";
        }
        return name;
    }

    private static final Map<ResourceCopyTypeEnum, String> RESOURCE_COPY_TYPE_NAME_MAP = new EnumMap<>(ResourceCopyTypeEnum.class);

    static {
        RESOURCE_COPY_TYPE_NAME_MAP.put(ResourceCopyTypeEnum.RCTE_Crystal, "生命石遗迹");
        RESOURCE_COPY_TYPE_NAME_MAP.put(ResourceCopyTypeEnum.RCTE_SoulStone, "灵魂石遗迹(灵魂石)");
        RESOURCE_COPY_TYPE_NAME_MAP.put(ResourceCopyTypeEnum.RCTE_Rune, "神器遗迹(原符石遗迹)");
        RESOURCE_COPY_TYPE_NAME_MAP.put(ResourceCopyTypeEnum.RCTE_Awaken, "觉醒遗迹(觉醒石)");
        RESOURCE_COPY_TYPE_NAME_MAP.put(ResourceCopyTypeEnum.RCTE_Gold, "黄金遗迹(金币)");

    }

    public static String getResCopyTypeName(ResourceCopyTypeEnum typeEnum) {
        if (typeEnum == null || typeEnum == ResourceCopyTypeEnum.RCTE_Null) {
            return "";
        }

        String name = RESOURCE_COPY_TYPE_NAME_MAP.get(typeEnum);
        if (name == null) {
            LogUtil.warn("StatisticsLogUtil.getResCopyTypeName, resource copy Type is not link name, type:" + typeEnum);
            return "";
        }
        return name;
    }

    public static String getResCopyName(int type) {
        return getResCopyTypeName(ResourceCopyTypeEnum.forNumber(type));
    }


    private static final String[] qualityName = new String[]{"绿色", "绿+", "蓝色", "蓝+", "紫色", "紫+", "橙色", "橙+", "红色", "红+"};
    private static final String[] gemRarityName = new String[]{"", "绿色", "蓝色", "紫色", "橙色", "红色"};

    public static String getQualityName(int quality) {
        if (quality <= qualityName.length) {
            return qualityName[quality - 1];
        }
        return "";
    }

    public static List<PetLog> buildPetLogByPetList(List<Pet> pets) {
        if (pets == null) {
            return null;
        }

        List<PetLog> result = new ArrayList<>();
        for (Pet pet : pets) {
            result.add(new PetLog(pet));
        }
        return result;
    }

    public static List<PetLog> buildPetLogListByBattleData(SC_EnterFight.Builder battleData, int camp) {
        if (battleData == null) {
            return null;
        }

        return buildPetLogListByBattleData(battleData.getPlayerInfoList(), camp);
    }

    public static List<PetLog> buildPetLogListByBattleData(List<BattlePlayerInfo> battlePlayerInfos, int camp) {
        if (GameUtil.collectionIsEmpty(battlePlayerInfos)) {
            return null;
        }

        List<PetLog> result = new ArrayList<>();
        for (BattlePlayerInfo builder : battlePlayerInfos) {
            if (builder.getCamp() == camp) {
                List<BattlePetData> petListList = builder.getPetListList();
                for (BattlePetData battlePetData : petListList) {
                    result.add(new PetLog(battlePetData));
                }
                break;
            }
        }
        return result;
    }

    public static List<ConsumeLog> buildConsumeByList(List<Consume> consumes) {
        if (consumes == null) {
            return null;
        }

        List<ConsumeLog> result = new ArrayList<>();
        for (Consume consume : consumes) {
            result.add(new ConsumeLog(consume));
        }
        return result;
    }

    public static List<PetPropertyLog> buildPetProperty(Pet.Builder pet) {
        if (pet == null) {
            return null;
        }
        List<PetPropertyLog> result = new ArrayList<>();
        List<PetPropertyEntity> propertyList = pet.getPetProperty().getPropertyList();
        for (PetPropertyEntity petPropertyEntity : propertyList) {
            result.add(new PetPropertyLog(petPropertyEntity));
        }
        return result;

    }

    public static List<PetPropertyLog> buildPropertyList(Map<Integer, Integer> params) {
        if (MapUtils.isEmpty(params)) {
            return Collections.emptyList();
        }
        List<PetPropertyLog> result = new ArrayList<>();
        for (Entry<Integer, Integer> entry : params.entrySet()) {
            result.add(new PetPropertyLog(entry.getKey(), entry.getValue()));
        }
        return result;

    }

    public static List<PetPropertyLog> buildPropertyList(int[][] params) {
        if (ArrayUtils.isEmpty(params)) {
            return null;
        }
        List<PetPropertyLog> result = new ArrayList<>();
        for (int[] param : params) {
            if (param.length < 2) {
                continue;
            }
            result.add(new PetPropertyLog(param[0], param[1]));
        }
        return result;

    }

    public static List<RewardLog> buildRewardLogList(List<Reward> rewards) {
        if (rewards == null) {
            return null;
        }
        List<RewardLog> result = new ArrayList<>();
        for (Reward reward : rewards) {
            result.add(new RewardLog(reward));
        }
        return result;
    }

    public static String getBattleResultByWinCamp(int winCamp) {
        if (-1 == winCamp) {
            return "平局";
        } else if (1 == winCamp) {
            return "胜利";
        } else if (2 == winCamp) {
            return "失败";
        } else if (3 == winCamp) {
            return "平局";
        }
        return "";
    }

    /**
     * 勇气试炼难度
     *
     * @param diff
     * @return
     */
    public static String getBraveDiffName(int diff) {
        if (diff == 1) {
            return "初级";
        } else if (diff == 2) {
            return "高级";
        } else if (diff == 3) {
            return "困难";
        } else if (diff == 4) {
            return "噩梦";
        }
        return "";
    }

    public static String getNameByTypeAndId(RewardTypeEnum type, int cfgId) {
        if (type == RewardTypeEnum.RTE_Item) {
            return Item.getItemName(cfgId);
        } else if (type == RewardTypeEnum.RTE_PetFragment) {
            return PetFragmentConfig.getNameById(cfgId);
        } else if (type == RewardTypeEnum.RTE_Rune) {
            return PetRuneProperties.getNameById(cfgId);
        } else if (type == RewardTypeEnum.RTE_Pet) {
            return PetBaseProperties.getNameById(cfgId);
        } else if (type == RewardTypeEnum.RTE_Avatar) {
            return Head.getNameById(cfgId);
        } else if (type == RewardTypeEnum.RTE_Gem) {
            return PetGemNameIconConfig.queryName(cfgId);
        }
        return getRewardTypeName(type);
    }

    public static String getNameByTypeValueAndId(int typeValue, int cfgId) {
        return getNameByTypeAndId(RewardTypeEnum.forNumber(typeValue), cfgId);
    }


    private static final Map<EnumRankingType, String> RANKING_TYPE_NAME = new EnumMap<>(EnumRankingType.class);

    static {
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_Null, "");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_Ability, "战力排行榜");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_PetAbility, "宠物战力排行榜(单个宠物最高战力)");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_PlayerLevel, "玩家等级排行榜");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_MainLine, "主线排行榜");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_Spire, "爬塔排行榜");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_ArenaScoreLocal, "竞技场段位排行(副分数,竞技场积分)");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_ArenaScoreCross, "竞技场全服全段位积分排行(副分数,竞技场积分)");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_ArenaScoreLocalDan, "竞技场本服本段位积分排行(副分数,竞技场积分)");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_ArenaGainScore, "竞技场积分获取");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_MineScore, "魔晶矿区积分排行榜");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_ActivityBoss_Damage, "Boss活动造成的伤害");
        RANKING_TYPE_NAME.put(EnumRankingType.ERT_DemonDescendsScore, "魔灵降临积分");
    }

    public static String getRankingTypeName(EnumRankingType rankingType) {
        if (rankingType != null) {
            return RANKING_TYPE_NAME.get(rankingType);
        }
        return "";
    }


    /**
     * ==========================ActivityTypeName Start =============================
     */
    private static final Map<ActivityTypeEnum, String> ACTIVITY_TYPE_NAME_MAP = new EnumMap<>(ActivityTypeEnum.class);

    static {
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_General, "通用活动");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_Exchange, "通用兑换");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_ForInv, "外敌入侵");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_PointCopy, "积分副本");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_NoviceCredit, "新手积分");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_CumuSignIn, "累积签到");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_FirstPay, "首充活动");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_LimitGift, "限购礼包");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_WorthGift, "超值礼包");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_BossBattle, "Boss战");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_ScratchLottery, "刮刮乐");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_CumuPay, "累充");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_Ranking, "排行榜");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_WishWell, "许愿池活动");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_GrowthFund, "成长基金活动");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_DailyGift, "日礼包");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_WeeklyGift, "周礼包");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_MonthlyGift, "月礼包");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_BuyItem, "通用购买物品");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_CumuOnline, "总在线活动");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_DailyOnline, "每日在线活动");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_CumuRecharge, "累计充值");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_SevenDaysSignIn, "七日签到");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_NewBeeGift, "新手礼包");
//        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_BestExchange, "极品兑换");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_Ads, "广告");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_DemonDescends, "魔灵降临");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_DayDayRecharge, "天天充值");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_HadesTreasure, "哈迪斯的宝藏");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_RechargeRebate, "充值返利");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_ZeroCostPurchase, "0元购");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_DirectPurchaseGift, "直购礼包");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_RuneTreasure, "符文密藏");

        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_DailyFirstRecharge, "每日首充");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_RichMan, "大富翁");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_MistMaze, "迷雾森林迷宫活动");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_MistGhostBuster, "迷雾森林抓鬼活动");
        ACTIVITY_TYPE_NAME_MAP.put(ActivityTypeEnum.ATE_ItemCard, "道具卡");
    }

    public static String getActivityTypeName(ActivityTypeEnum activityType) {
        if (activityType == null) {
            return "";
        }
        return ACTIVITY_TYPE_NAME_MAP.get(activityType);
    }

    public static String getGemRarityName(int queryRarity) {
        if (queryRarity >= gemRarityName.length) {
            return "";
        }

        return gemRarityName[queryRarity];
    }

    /**
     * ==========================ActivityTypeName end =============================
     */
}
