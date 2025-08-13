package model.activity;

import cfg.BuyTaskConfig;
import cfg.BuyTaskConfigObject;
import cfg.CumuSignIn;
import cfg.CumuSignInObject;
import cfg.ExchangeAddition;
import cfg.ExchangeAdditionObject;
import cfg.GameConfig;
import cfg.Item;
import cfg.ItemObject;
import cfg.LocalActivityOpenTime;
import cfg.LocalActivityOpenTimeObject;
import cfg.MistSeasonConfig;
import cfg.MistSeasonConfigObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetExchangeMission;
import cfg.PetExchangeMissionObject;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import cfg.PetRuneExp;
import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import cfg.RuneTreasure;
import cfg.ServerStringRes;
import cfg.TheWarSeasonConfig;
import cfg.TheWarSeasonConfigObject;
import cfg.TimeLimitActivity;
import cfg.TimeLimitActivityObject;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.RedisKey;
import common.HttpRequestUtil;
import common.JedisUtil;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import entity.UpdateActivityDropCount;
import entity.UpdateDailyData;
import lombok.Getter;
import model.activity.ActivityUtil.LocalActivityId;
import model.activityboss.ActivityBossManager;
import model.consume.ConsumeUtil;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.gloryroad.GloryRoadManager;
import model.itembag.ItemConst.ItemType;
import model.mistforest.MistForestManager;
import model.pet.dbCache.petCache;
import model.ranking.RankingManager;
import model.ranking.settle.ActivityRankingSettleHandler;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;
import protocol.Activity;
import protocol.Activity.ActivityNoticeEnum;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.Addition;
import protocol.Activity.AdditionEnumType;
import protocol.Activity.ApposeAddition;
import protocol.Activity.ApposeAddition.Builder;
import protocol.Activity.ClientActivity;
import protocol.Activity.ClientActivityNotice;
import protocol.Activity.DemonDescendsRandom;
import protocol.Activity.EnumRankingType;
import protocol.Activity.EnumSeasonType;
import protocol.Activity.ExchangeSlot;
import protocol.Activity.GeneralActivityTemplate;
import protocol.Activity.RankingReward;
import protocol.Common.Consume;
import protocol.Common.LanguageEnum;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.GameplayDB.DB_PlatformActivityInfo;
import protocol.GameplayDB.GameplayTypeEnum;
import protocol.Server.DropInfo;
import protocol.Server.DropResourceEnum;
import protocol.Server.ServerActivity;
import protocol.Server.ServerActivityNotice;
import protocol.Server.ServerBuyMission;
import protocol.Server.ServerExMission;
import protocol.Server.ServerSubMission;
import protocol.TargetSystem.TargetTypeEnum;
import server.http.HttpUtil;
import server.http.entity.PlatformRetCode.RetCode;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author huhan
 */
public class ActivityManager implements Tickable, GamePlayerUpdate, UpdateDailyData {
    private static ActivityManager instance = new ActivityManager();

    public static ActivityManager getInstance() {
        if (instance == null) {
            synchronized (ActivityManager.class) {
                if (instance == null) {
                    instance = new ActivityManager();
                }
            }
        }
        return instance;
    }

    private ActivityManager() {
    }

    /**
     * <ActivityId,Activity>
     */
    private final Map<Long, ServerActivity> activities = new ConcurrentHashMap<>();

    private final List<ServerActivity> scheduledPushActivity = Collections.synchronizedList(new ArrayList<>());

    /**
     * <noticeId, notice>
     **/
    private final Map<Long, ServerActivityNotice> noticesMap = new ConcurrentHashMap<>();

    /**
     * 特殊活动,只保存活动开放时间,其他信息走其他接口获取
     **/
    private final Map<ActivityTypeEnum, ClientActivity> specialActivity = new ConcurrentHashMap<>();

    private volatile long nextTickTime;
    private static final long TICK_INTERVAL_TIME = TimeUtil.MS_IN_A_MIN;

    @Getter
    private static final List<ActivityTypeEnum> dailyRestPlatActivity = Arrays.asList(ActivityTypeEnum.ATE_DemonDescends, ActivityTypeEnum.ATE_HadesTreasure, ActivityTypeEnum.ATE_RuneTreasure);


    private final Set<Long> alreadySettledActivityIdSet = Collections.synchronizedSet(new HashSet<>());

    /**
     * 按类型查找一个开始时间最近的活动
     *
     * @param typeEnum
     * @return
     */
    public ServerActivity findOneRecentActivityByType(ActivityTypeEnum typeEnum) {
        List<ServerActivity> activities = getActivitiesByType(typeEnum);
        if (CollectionUtils.isEmpty(activities)) {
            return null;
        }
        return activities.stream().filter(ActivityUtil::activityInOpen).min(Comparator.comparing(ServerActivity::getBeginTime)).orElse(null);

    }


    public boolean init() {
        loadDBPlatformInfo();
        return initLocalActivity()
                && gameplayCache.getInstance().addToUpdateSet(this)
                && GlobalTick.getInstance().addTick(this);
    }

    private void loadDBPlatformInfo() {
        DB_PlatformActivityInfo.Builder dbBuilder = null;
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_PlatformActivityInfo);
        if (entity != null && entity.getGameplayinfo() != null) {
            try {
                dbBuilder = DB_PlatformActivityInfo.parseFrom(entity.getGameplayinfo()).toBuilder();
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }

        if (dbBuilder != null) {
            this.alreadySettledActivityIdSet.addAll(dbBuilder.getAlreadySettleActivityIdList());
        }
    }

    private boolean initLocalActivity() {
        pullPlatformActivity();
//        initTempDemonDescends();
//        initTempHadesTreasure();
        return initCumuSignIn() && initLocalActivityByConfig();
    }

//    private void initTempHadesTreasure() {
//        ServerActivity.Builder hadesTreasure = ServerActivity.newBuilder();
//        hadesTreasure.setType(ActivityTypeEnum.ATE_HadesTreasure);
//        hadesTreasure.setActivityId(ActivityTypeEnum.ATE_HadesTreasure_VALUE);
//        hadesTreasure.setTitle("{0:\"哈迪斯的宝藏测试\"}");
//        hadesTreasure.setStartDisTime(-1);
//        hadesTreasure.setBeginTime(GlobalTick.getInstance().getCurrentTime());
//        hadesTreasure.setEndTime(hadesTreasure.getBeginTime() + TimeUtil.MS_IN_A_WEEK);
//        hadesTreasure.setOverDisTime(-1);
//
//        addServerActivity(hadesTreasure.build());
//    }

//    /**
//     * 初始化临时魔灵降临活动用于测试
//     */
//    private void initTempDemonDescends() {
//        ServerActivity.Builder demonDescends = ServerActivity.newBuilder();
//        demonDescends.setType(ActivityTypeEnum.ATE_DemonDescends);
//        demonDescends.setActivityId(ActivityTypeEnum.ATE_DemonDescends_VALUE);
//        demonDescends.setTitle("{0:\"魔灵降临测试\"}");
//        demonDescends.setStartDisTime(-1);
//        demonDescends.setBeginTime(GlobalTick.getInstance().getCurrentTime());
//        demonDescends.setEndTime(demonDescends.getBeginTime() + TimeUtil.MS_IN_A_WEEK);
//        demonDescends.setOverDisTime(-1);
//
//        demonDescends.setRankingType(EnumRankingType.ERT_DemonDescendsScore);
//
//        for (int i = 1; i <= 4; i++) {
//            RankingReward.Builder rankingBuilder = RankingReward.newBuilder();
//            if (i <= 3) {
//                rankingBuilder.setStartRanking(i);
//                rankingBuilder.setEndRanking(i);
//            } else {
//                rankingBuilder.setStartRanking(4);
//                rankingBuilder.setEndRanking(10);
//            }
//            rankingBuilder.addRewards(RewardUtil.parseReward(RewardTypeEnum.RTE_Gold, 0, (i + 1) * 100));
//            demonDescends.addRankingReward(rankingBuilder.build());
//
//
//            DemonDescendsRandom.Builder random = DemonDescendsRandom.newBuilder();
//            random.setRandomRewards(RandomReward.newBuilder()
//                    .setRewardType(RewardTypeEnum.RTE_PetFragment)
//                    .setId(94030 + i)
//                    .setCount(40)
//                    .setRandomOdds((i + 1) * 20)
//                    .build());
//            if (GameUtil.inScope(1, 3, i)) {
//                random.setRewardLv(i);
//
//                random.setGrandPrize(true);
//            }
//            demonDescends.addDemonDescentsRandom(random.build());
//        }
//
//        addServerActivity(demonDescends.build());
//    }

    /**
     * 拉取平台活动信息
     *
     * @return
     */
    private void pullPlatformActivity() {
        pullActivity();
        pullNotice();
    }

    private void pullActivity() {
        JSONObject pull = new JSONObject();
        pull.put("clientId", ServerConfig.getInstance().getClientId());
        pull.put("serverIndex", ServerConfig.getInstance().getServer());

        String activityResult = HttpRequestUtil.doPost(ServerConfig.getInstance().getPlatformActivityPull(), pull.toJSONString());
        LogUtil.info("model.activity.ActivityManager.pullActivity, pull params:" + pull.toJSONString() + ", pull result:" + activityResult);
        if (activityResult == null || !HttpUtil.parseAndAddActivities(activityResult)) {
            LogUtil.info("model.activity.ActivityManager.pullActivity, paras and add failed");
            pull.put("flag", false);
        } else {
            pull.put("flag", true);
        }
        HttpRequestUtil.doPost(ServerConfig.getInstance().getPlatformActivityPullReturn(), pull.toJSONString());
    }

    private void pullNotice() {
        JSONObject pull = new JSONObject();
        pull.put("clientId", ServerConfig.getInstance().getClientId());
        pull.put("serverIndex", ServerConfig.getInstance().getServer());
        String noticeResult = HttpRequestUtil.doPost(ServerConfig.getInstance().getPlatformActivityNoticePull(), pull.toJSONString());
        LogUtil.info("model.activity.ActivityManager.pullNotice, pull params:" + pull.toJSONString() + ", pull result:" + noticeResult);
        if (noticeResult == null || HttpUtil.parseActivityNoticeAndAdd(noticeResult).getRetCode() != RetCode.success) {
            pull.put("flag", false);
            LogUtil.info("model.activity.ActivityManager.pullActivity, paras and add failed");
        } else {
            pull.put("flag", true);
        }
        HttpRequestUtil.doPost(ServerConfig.getInstance().getPlatformActivityPullReturn(), pull.toJSONString());
    }

    private boolean initLocalActivityByConfig() {
        Map<Integer, LocalActivityOpenTimeObject> ix_id = LocalActivityOpenTime._ix_id;
        if (ix_id == null || ix_id.isEmpty()) {
            LogUtil.warn("LocalActivityOpenTime cfg is empty");
            return true;
        }
        for (LocalActivityOpenTimeObject activity : ix_id.values()) {
            if (activity.getId() <= 0) {
                continue;
            }
            long overDisTime = TimeUtil.parseActivityTime(activity.getOverdistime());
            if (overDisTime != -1 && GlobalTick.getInstance().getCurrentTime() > overDisTime) {
                LogUtil.warn("cur LocalActivity activity is over");
                continue;
            }
            long startDisTime = TimeUtil.parseActivityTime(activity.getStartdistime());
            if (startDisTime > 0 && GlobalTick.getInstance().getCurrentTime() < startDisTime) {
                LogUtil.warn("cur LocalActivity activity is not start");
                continue;
            }
            ServerActivity.Builder newActivity = ServerActivity.newBuilder();
            newActivity.setActivityId(activity.getId());
            newActivity.setTitle(ServerStringRes.getLanguageNumContent(activity.getTitle()));
            newActivity.setDesc(ServerStringRes.getLanguageNumContent(activity.getDesc()));
            newActivity.setDetail(ServerStringRes.getLanguageNumContent(activity.getDetail()));
            newActivity.setTabTypeValue(activity.getTabtype());
            newActivity.setRedDotTypeValue(activity.getReddottype());

            ActivityTypeEnum typeEnum = getActivityTypeByLocalActivityId(activity.getId());
            if (typeEnum != null) {
                newActivity.setType(typeEnum);
            }

            if (activity.getId() == LocalActivityId.ALIEN_REQUEST) {
                newActivity.setTemplate(GeneralActivityTemplate.GAT_AlienRequest);
            } else if (activity.getId() == LocalActivityId.BestExchange) {
                newActivity.setTemplate(GeneralActivityTemplate.GAT_SimpleExchange);
            }

            if (!StringUtils.isEmpty(activity.getIcon())) {
                newActivity.setPictureName(activity.getIcon());
            }
            newActivity.setStartDisTime(TimeUtil.parseActivityTime(activity.getStartdistime()));
            long beginTime = TimeUtil.parseActivityTime(activity.getBegintime());
            newActivity.setBeginTime(beginTime);
            newActivity.setEndTime(TimeUtil.parseActivityTime(activity.getEndtime()));
            newActivity.setOverDisTime(overDisTime);

            int[] missionList = activity.getMissionlist();
            for (int index : missionList) {
                ServerExMission.Builder builder = builderMission(index, beginTime);
                if (builder == null) {
                    continue;
                }
                newActivity.putExMission(index, builder.build());
            }
            if (!initActivityBuyTask(activity, newActivity)) {
                return false;
            }
            addActivity(newActivity.build());
            if (!initActivityManager(newActivity.getType())) {
                return false;
            }
        }

        return true;
    }

    private boolean initActivityManager(ActivityTypeEnum typeEnum) {
        if (ActivityTypeEnum.ATE_DailyFirstRecharge == typeEnum) {
            return DailyFirstRechargeManage.getInstance().init();
        }
        return true;
    }

    /**
     * @param activityId
     * @return
     */
    private ActivityTypeEnum getActivityTypeByLocalActivityId(int activityId) {
        if (activityId == LocalActivityId.PET_EXCHANGE
                || activityId == LocalActivityId.ALIEN_REQUEST) {
            return ActivityTypeEnum.ATE_Exchange;
        } else if (activityId == LocalActivityId.GrowthFund) {
            return ActivityTypeEnum.ATE_GrowthFund;
        } else if (activityId == LocalActivityId.DailyGift) {
            return ActivityTypeEnum.ATE_DailyGift;
        } else if (activityId == LocalActivityId.WeeklyGift) {
            return ActivityTypeEnum.ATE_WeeklyGift;
        } else if (activityId == LocalActivityId.MonthlyGift) {
            return ActivityTypeEnum.ATE_MonthlyGift;
        } else if (activityId == LocalActivityId.BestExchange) {
            return ActivityTypeEnum.ATE_BuyItem;
        } else if (activityId == LocalActivityId.DailyFirstRecharge) {
            return ActivityTypeEnum.ATE_DailyFirstRecharge;
        }
        return ActivityTypeEnum.ATE_General;
    }

    private boolean initActivityBuyTask(LocalActivityOpenTimeObject activity, ServerActivity.Builder newActivity) {
        for (BuyTaskConfigObject config : BuyTaskConfig._ix_id.values()) {
            if (config.getId() > 0 && activity.getId() == config.getActivityid()) {
                ServerBuyMission.Builder buyMission = ServerBuyMission.newBuilder();
                buyMission.setIndex(config.getId());
                buyMission.setLimitBuy(config.getLimitbuy());
                Consume consume = ConsumeUtil.parseConsume(config.getPrice());
                if (consume == null) {
                    LogUtil.error("BuyTaskConfig error, not found price id=" + config.getId());
                    return false;
                }
                buyMission.setPrice(consume);
                buyMission.setDiscount(config.getDiscount());
                List<Reward> rewards = RewardUtil.getRewardsByRewardId(config.getReward());
                if (rewards == null) {
                    LogUtil.error("BuyTaskConfig error, not found reward id=" + config.getId());
                    return false;
                }
                buyMission.addAllRewards(rewards);
                buyMission.setEndTimestamp(-1);
                buyMission.setTitle(ServerStringRes.getLanguageNumContent(config.getTitle()));
                buyMission.setSpecialType(config.getSpecialtype());
                newActivity.putBuyMission(config.getId(), buyMission.build());
            }
        }
        return true;
    }

    private boolean initCumuSignIn() {
        for (int i = 1; i < CumuSignIn.getInstance().maxDays; i++) {
            CumuSignInObject byDays = CumuSignIn.getByDays(i);
            if (byDays == null || RewardUtil.parseRewardIntArrayToRewardList(byDays.getRewards()) == null) {
                LogUtil.error("CumuSignIn cfg is error, cumu days = " + i + ", is null");
                return false;
            }
        }

        ClientActivity.Builder cumuSignIn = ClientActivity.newBuilder();
        cumuSignIn.setActivityType(ActivityTypeEnum.ATE_CumuSignIn);
        return addSpecialActivity(cumuSignIn.build());
    }

    /**
     * 获得所有的通用活动
     *
     * @return
     */
    public Collection<ServerActivity> getAllGeneralActivities() {
        return activities.values();
    }


    public List<ServerActivity> getActivitiesByType(ActivityTypeEnum typeEnum) {
        return activities.values().stream().filter(e -> e.getType() == typeEnum).collect(Collectors.toList());
    }

    public ServerActivity getActivityByType(ActivityTypeEnum typeEnum) {
        for (ServerActivity value : activities.values()) {
            if (value.getType() == typeEnum) {
                return value;
            }
        }
        return null;
    }

    public List<Long> getActivitiesIdByType(ActivityTypeEnum typeEnum) {
        return activities.values().stream().filter(e -> e.getType() == typeEnum).map(ServerActivity::getActivityId).collect(Collectors.toList());
    }

    public List<ServerActivity> getOpenActivitiesByType(ActivityTypeEnum typeEnum) {
        return activities.values().stream().filter(e -> e.getType() == typeEnum && ActivityUtil.activityInOpen(e)).collect(Collectors.toList());
    }

    /**
     * 获取当前开放的活动
     *
     * @param typeEnum
     * @return
     */
    public List<Long> getOpenActivitiesIdByType(ActivityTypeEnum typeEnum) {
        List<ServerActivity> openActivities = getOpenActivitiesByType(typeEnum);
        if (CollectionUtils.isEmpty(openActivities)) {
            return null;
        }
        return openActivities.stream().map(ServerActivity::getActivityId).collect(Collectors.toList());
    }

    public List<ServerActivity> getAllActivities() {
        return new ArrayList<>(activities.values());
    }

    /**
     * 获取所有的特殊活动
     *
     * @return
     */
    public Collection<ClientActivity> getAllSpecialActivities() {
        return specialActivity.values();
    }

    public boolean addSpecialActivity(ClientActivity clientActivity) {
        if (clientActivity == null || clientActivity.getActivityType() == null
                || clientActivity.getActivityType() == ActivityTypeEnum.ATE_Null
                || clientActivity.getActivityType() == ActivityTypeEnum.ATE_General
                || clientActivity.getActivityType() == ActivityTypeEnum.ATE_Exchange) {
            LogUtil.error("model.activity.ActivityManager.addSpecialActivity, UnSupported activity type");
            return false;
        }
        specialActivity.put(clientActivity.getActivityType(), clientActivity);
        return true;
    }

    public boolean removeSpecialActivity(ActivityTypeEnum activityType) {
        if (activityType == null
                || activityType == ActivityTypeEnum.ATE_Null
                || activityType == ActivityTypeEnum.ATE_General
                || activityType == ActivityTypeEnum.ATE_Exchange) {
            LogUtil.error("model.activity.ActivityManager.removeSpecialActivity, UnSupported activity type");
            return false;
        }
        specialActivity.remove(activityType);
        return true;
    }

    /**
     * 添加一个新的活动,不检查
     *
     * @param serverActivity
     * @return
     */
    private boolean addActivity(ServerActivity serverActivity) {
        //删除旧活动的排行榜
        ServerActivity oldActivity = activities.get(serverActivity.getActivityId());
        if (EnumRankingType.ERT_RichMan != serverActivity.getRankingType()
                && oldActivity != null
                && serverActivity.getRankingTypeValue() != EnumRankingType.ERT_Null_VALUE) {
            RankingManager.getInstance().removeActivityRanking(oldActivity);
        }
        activities.put(serverActivity.getActivityId(), serverActivity);
        LogUtil.debug("success add activity, id = " + serverActivity.getActivityId() + ", title = " + serverActivity.getTitle());

        if (serverActivity.getType() == ActivityTypeEnum.ATE_MistMaze) {
            MistForestManager.getInstance().getMazeManager().updateMazeSyncData(serverActivity);
        } else if (serverActivity.getType() == ActivityTypeEnum.ATE_MistGhostBuster) {
            MistForestManager.getInstance().getGhostBusterManager().updateGhostActivityData(serverActivity);
        }
        return true;
    }

    /**
     * 添加一个新的活动并检查
     *
     * @param serverActivity
     * @return
     */
    private boolean addActivityWithCheck(ServerActivity serverActivity) {
        if (!checkActivity(serverActivity)) {
            return false;
        }

        return addActivity(serverActivity);
    }

    /**
     * 添加活动不检查
     *
     * @param activities
     * @return
     */
    public boolean addAllActivities(List<ServerActivity> activities) {
        if (CollectionUtils.isEmpty(activities)) {
            return true;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        List<ServerActivity> needUpdateToPlayer = new ArrayList<>();
        for (ServerActivity activity : activities) {
            ServerActivity oldActivity = ActivityManager.getInstance().getActivityCfgById(activity.getActivityId());
            //如果老活动不存在或未开启&&新活动未开启,则新活动加入待推送列表中,否则推送给在线玩家
            if ((oldActivity == null || !GameUtil.inScope(oldActivity.getStartDisTime(), oldActivity.getOverDisTime(), currentTime))
                    && !GameUtil.inScope(activity.getStartDisTime(), activity.getOverDisTime(), currentTime)) {

                addScheduledPushActivity(activity);
            } else {
                needUpdateToPlayer.add(activity);
            }
            addActivity(activity);
        }

        //更新新活动到玩家
        if (!needUpdateToPlayer.isEmpty()) {
            targetsystemCache.getInstance().sendNewActivityToAllOnlinePlayer(activities);
        }

        return true;
    }

    private void addScheduledPushActivity(ServerActivity activity) {
        scheduledPushActivity.add(activity);
    }

    /**
     * 通过列表添加新活动
     *
     * @param activities
     * @return
     */
    public boolean addAllActivitiesWithCheck(List<ServerActivity> activities) {
        if (!checkAllActivities(activities)) {
            return false;
        }
        return addAllActivities(activities);
    }

    public boolean checkAllActivities(List<ServerActivity> activities) {
        if (CollectionUtils.isEmpty(activities)) {
            return true;
        }

        for (ServerActivity activity : activities) {
            if (!checkActivity(activity)) {
                LogUtil.error("ActivityManager.addAllActivities, check activity failed, activity id:" + activity.getActivityId());
                return false;
            }
        }
        return true;
    }

    /**
     * 是否是普通活动
     *
     * @return
     */
    private boolean isCommonActivity(ActivityTypeEnum activityType) {
      /*  if (activityType == ActivityTypeEnum.ATE_General
                || activityType == ActivityTypeEnum.ATE_Exchange) {
            return true;
        }
        return false;*/
        return true;
    }

    public void addActivityNotice(ServerActivityNotice notice) {
        if (!checkNotice(notice)) {
            return;
        }

        //更新也走同样的接口
        if (noticesMap.containsKey(notice.getNoticeId())) {
            LogUtil.warn("model.activity.ActivityManager.addActivityNotice, notice id = " + notice.getNoticeId() + " is already exist");
        }

        noticesMap.put(notice.getNoticeId(), notice);
    }

    public void addAllActivityNotice(List<ServerActivityNotice> noticeList) {
        if (noticeList == null) {
            return;
        }
        for (ServerActivityNotice notice : noticeList) {
            addActivityNotice(notice);
        }
    }

    public List<ClientActivityNotice> getAllActivityNotice(LanguageEnum language) {
        List<ClientActivityNotice> result = new ArrayList<>();
        for (ServerActivityNotice value : noticesMap.values()) {
            result.add(buildClientNotice(value, language));
        }
        return result;
    }

    public void deleteActivityNotice(long noticeId) {
        noticesMap.remove(noticeId);
    }

    public static ClientActivityNotice buildClientNotice(ServerActivityNotice serverNotice, LanguageEnum language) {
        if (serverNotice == null) {
            return null;
        }
        ClientActivityNotice.Builder builder = ClientActivityNotice.newBuilder();
        builder.setNoticeId(serverNotice.getNoticeId());
        String title = serverNotice.getTitleMap().get(language.getNumber());
        if (title != null) {
            builder.setTitle(title);
        }
        String sidebar = serverNotice.getSidebarMap().get(language.getNumber());
        if (sidebar != null) {
            builder.setSidebar(sidebar);
        }
        String body = serverNotice.getBodyMap().get(language.getNumber());
        if (body != null) {
            builder.setBody(body);
        }
        builder.setNoticeType(serverNotice.getNoticeType());
        String picture = serverNotice.getPicture();
        if (picture != null) {
            builder.setPicture(picture);
        }
        if (serverNotice.getRewardsCount() > 0) {
            builder.addAllRewards(serverNotice.getRewardsList());
        }
        builder.setActivityStartTime(serverNotice.getActivityStartTime());
        builder.setActicityEndTime(serverNotice.getActicityEndTime());
        builder.setInnerHyperlinkValue(serverNotice.getInnerHyperlinkValue());
        if (serverNotice.getHttpHyperlink() != null) {
            builder.setHttpHyperlink(serverNotice.getHttpHyperlink());
        }
        return builder.build();
    }

    public static boolean checkNotice(ServerActivityNotice notice) {
        if (notice == null) {
            LogUtil.info("activity.ActivityManager.checkNotice, check target is null");
            return false;
        }

        if (notice.getStartTime() >= notice.getEndTime()) {
            LogUtil.info("activity.ActivityManager.checkNotice, valid time is error");
            return false;
        }

        if (notice.getNoticeType() == ActivityNoticeEnum.ANE_Picture
                && notice.getActivityStartTime() >= notice.getActicityEndTime()) {
            LogUtil.info("activity.ActivityManager.checkNotice, activity valid time is error");
            return false;
        }

        return true;
    }


    /**
     * 删除一个活动
     *
     * @param activityId
     * @return
     */
    public boolean removeServerActivity(long activityId) {
        ServerActivity remove = activities.remove(activityId);
        if (remove != null) {
            removeActivityFromSettledActivity(remove);

            //清空排行榜数据
            RankingManager.getInstance().removeActivityRanking(remove);

            //清空活动数据保存
            EventUtil.clearAllPlayerActivityInfo(remove);

            if(remove.getType().getNumber() == ActivityTypeEnum.ATE_StarTreasure_VALUE){
                JedisUtil.jedis.del(RedisKey.getStarTreasureRecordKey());
            }
        }
        LogUtil.info("delete server activity success, id = " + activityId);
        return true;
    }

    public ServerActivity getActivityCfgById(long activityId) {
        return activities.get(activityId);
    }

    /**
     * 不检查重复活动,更新活动走同一个接口
     *
     * @param serverActivity
     * @return
     */
    public boolean checkActivity(ServerActivity serverActivity) {
        if (serverActivity == null) {
            return false;
        }

        LogUtil.debug("ActivityManager.checkActivity, start check activity id :" + serverActivity.getActivityId());
        if (!checkTime(serverActivity.getStartDisTime(), serverActivity.getBeginTime(),
                serverActivity.getEndTime(), serverActivity.getOverDisTime())) {
            LogUtil.error("activity time cfg is error");
            return false;
        }

        boolean checkResult = true;
        if (serverActivity.getType() == ActivityTypeEnum.ATE_General) {
            checkResult = checkGeneralActivityMission(serverActivity);
        } else if (serverActivity.getType() == ActivityTypeEnum.ATE_Exchange) {
            checkResult = checkExActivityMission(serverActivity);
        } else if (serverActivity.getType() == ActivityTypeEnum.ATE_Ranking) {
            checkResult = checkRankingActivity(serverActivity);
        } else if (serverActivity.getType() == ActivityTypeEnum.ATE_DemonDescends) {
            checkResult = checkDemonDescendsActivity(serverActivity);
        } else if (serverActivity.getType() == ActivityTypeEnum.ATE_RuneTreasure) {
            checkResult = checkRuneTreasureActivity(serverActivity);
        }

        LogUtil.info("ActivityManager.checkActivity,end check activity,check activity type:" + serverActivity.getType()
                + ", id = " + serverActivity.getActivityId() + ",title = " + serverActivity.getTitle() + ", check result = " + checkResult);

        return checkResult;
    }

    private boolean checkRuneTreasureActivity(ServerActivity serverActivity) {
        if (serverActivity == null) {
            return false;
        }

        if (serverActivity.getBuyMissionCount() <= 0) {
            LogUtil.error("ActivityManager.checkRuneTreasureActivity, BuyMission is empty");
            return false;
        }

        if (serverActivity.getStageRewardsCount() <= 0
                || serverActivity.getStageRewardsCount() > RuneTreasure.getById(GameConst.CONFIG_ID).getStagerewardsmaxsize()) {
            LogUtil.error("ActivityManager.checkRuneTreasureActivity, stage size is error, size:" + serverActivity.getStageRewardsCount());
            return false;
        }

        if (serverActivity.getRuneTreasurePoolCount() <= 0) {
            LogUtil.error("ActivityManager.checkRuneTreasureActivity, pool is empty");
            return false;
        }
        return true;
    }

    private boolean checkDemonDescendsActivity(ServerActivity serverActivity) {
        if (serverActivity == null) {
            return false;
        }

        if (serverActivity.getDemonDescentsRandomCount() <= 0) {
            LogUtil.error("model.activity.ActivityManager.checkDemonDescendsActivity, demons random pool is empty");
            return false;
        }

        for (DemonDescendsRandom demonDescendsRandom : serverActivity.getDemonDescentsRandomList()) {
            if (!RewardUtil.checkRandomReward(demonDescendsRandom.getRandomRewards())) {
                LogUtil.error("model.activity.ActivityManager.checkDemonDescendsActivity, random rewards cfg error");
                return false;
            }
        }
        return true;
    }

    private boolean checkRankingActivity(ServerActivity serverActivity) {
        if (serverActivity.getRankingType() == null || serverActivity.getRankingType() == EnumRankingType.ERT_Null) {
            LogUtil.error("ActivityManager.checkRankingActivity, ranking type cfg is error, type:" + serverActivity.getRankingType());
            return false;
        }

        //检查排名奖励配置是否正确
        int lastEndRanking = 0;
        for (RankingReward rankingReward : serverActivity.getRankingRewardList()) {
            //起始排名不能高于结束排名
            if (rankingReward.getEndRanking() < rankingReward.getStartRanking()) {
                LogUtil.error("ActivityManager.checkRankingActivity, ranking scope cfg error, activity id:"
                        + serverActivity.getActivityId() + ", info:" + rankingReward);
                return false;
            }
            //按照顺序配置判断， 下一个开始的排名奖励不能小于等于上一个排名奖励的结束名次
            if (rankingReward.getStartRanking() <= lastEndRanking) {
                LogUtil.error("ActivityManager.checkRankingActivity, ranking scope cfg error, activity id:"
                        + serverActivity.getActivityId() + ", info:" + rankingReward + ", max than last endRanking:" + lastEndRanking);
                return false;
            }

            if (!checkActivityRewards(rankingReward.getRewardsList())) {
                LogUtil.error("ranking mission reward cfg  error:" + RewardUtil.toJsonStr(rankingReward.getRewardsList()));
                return false;
            }
        }
        return true;
    }

    public boolean checkTime(long startDisTime, long beginTime, long endTime, long overDisTime) {
        //全部时间都为永久显示
        if (startDisTime == -1 && beginTime == -1 && endTime == -1 && overDisTime == -1) {
            return true;
        }

        //展示时间为永久
        if (startDisTime == -1 && overDisTime == -1 && endTime > beginTime) {
            return true;
        }

        //开始时间为永久
        if (startDisTime == -1 && beginTime == -1 && overDisTime >= endTime) {
            return true;
        }

        //无结束时间
        if (overDisTime == -1 && endTime == -1 && startDisTime <= beginTime) {
            return true;
        }

        if (beginTime < endTime && startDisTime <= beginTime && overDisTime >= endTime) {
            return true;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (GameUtil.outOfScope(startDisTime, overDisTime, currentTime)) {
            LogUtil.error("ActivityManager.checkTime, activity is not in dis time scope");
            return false;
        }

        return false;
    }

    private boolean checkGeneralActivityMission(ServerActivity serverActivity) {
        if (serverActivity == null) {
            return false;
        }
        Map<Integer, ServerSubMission> missionsMap = serverActivity.getMissionsMap();
        if (missionsMap == null || missionsMap.isEmpty()) {
            LogUtil.warn("activity mission is null, activity id = " + serverActivity.getActivityId()
                    + ", activityTitle = " + serverActivity.getTitle());
        }

        Set<Integer> indexSet = new HashSet<>();
        for (ServerSubMission value : missionsMap.values()) {
            if (indexSet.contains(value.getIndex())) {
                LogUtil.error("have the same index in activity");
                return false;
            }
            indexSet.add(value.getIndex());
            if (value.getSubType() == null || value.getSubType() == TargetTypeEnum.TTE_NULL) {
                LogUtil.error("set targetType error");
                return false;
            }
            if (value.getTarget() <= 0) {
                LogUtil.error("activity target count <= 0");
                return false;
            }
            if (value.getEndTimestamp() != -1 && GlobalTick.getInstance().getCurrentTime() > value.getEndTimestamp()) {
                LogUtil.error("mission endTimestamp cfg  error, endTimestamp < curTime");
                return false;
            }

            if (!checkActivityRewards(value.getRewardList())) {
                LogUtil.error("mission reward cfg  error:" + RewardUtil.toJsonStr(value.getRewardList()));
                return false;
            }
        }
        return true;
    }

    /**
     * 检查活动配置的奖励, 目前主要检查配置个数为0 和道具表中不能用于发放的奖励
     *
     * @param rewards
     * @return
     */
    private boolean checkActivityRewards(List<Reward> rewards) {
        if (CollectionUtils.isEmpty(rewards)) {
            return true;
        }

        for (Reward reward : rewards) {
            if (reward == null || reward.getCount() <= 0) {
                LogUtil.error("model.activity.ActivityManager.checkActivityReward, reward count is <= 0");
                return false;
            }

            if (reward.getRewardType() == RewardTypeEnum.RTE_Item) {
                ItemObject itemCfg = Item.getById(reward.getId());
                if (itemCfg == null || itemCfg.getSpecialtype() == ItemType.ONLY_USE_FOR_DISPLAY) {
                    LogUtil.error("model.activity.ActivityManager.checkActivityReward, dis player item is can not use in activity mission, item id:" + reward.getId());
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkExActivityMission(ServerActivity serverActivity) {
        if (serverActivity == null) {
            return false;
        }
        List<DropInfo> dropInfoList = serverActivity.getDropInfoList();
        if (dropInfoList.isEmpty()) {
            LogUtil.warn("exActivity drop resource is null");
        }

        for (DropInfo dropInfo : dropInfoList) {
            ItemObject byId = Item.getById(dropInfo.getDropId());
            if (byId == null) {
                LogUtil.error("drop item do not cfg in itemCfg excel");
                return false;
            }
            if (dropInfo.getDropOdds() <= 0) {
                LogUtil.warn("drop item odds is not set");
            }
            if (dropInfo.getDropDailyLimit() == 0) {
                LogUtil.warn("drop item daily limit is not set");
            }
        }

        Map<Integer, ServerExMission> exMissionMap = serverActivity.getExMissionMap();
        if (exMissionMap == null || exMissionMap.isEmpty()) {
            LogUtil.error("activity exMission is null, activity id = " + serverActivity.getActivityId()
                    + ", activityTitle = " + serverActivity.getTitle());
            return true;
        }
        Set<Integer> indexSet = new HashSet<>();
        for (ServerExMission value : exMissionMap.values()) {
            if (indexSet.contains(value.getIndex())) {
                LogUtil.error("have the same index in activity");
                return false;
            }
            indexSet.add(value.getIndex());

            if (value.getEndTimestamp() != -1 && GlobalTick.getInstance().getCurrentTime() > value.getEndTimestamp()) {
                LogUtil.warn("mission endTimestamp cfg  error, endTimestamp < curTime");
            }

            List<ExchangeSlot> exSlotsList = value.getExSlotsList();
            if (exSlotsList == null || exMissionMap.isEmpty()) {
                LogUtil.error("ExSlot is empty");
                return false;
            }
            for (ExchangeSlot exchangeSlot : exSlotsList) {
                if (!checkExActivityAddition(exchangeSlot)) {
                    LogUtil.info("activity slot cfg is error");
                    return false;
                }
            }

            if (!checkActivityRewards(value.getRewardsList())) {
                LogUtil.error("exchange mission reward cfg  error:" + RewardUtil.toJsonStr(value.getRewardsList()));
                return false;
            }
        }
        return true;
    }

    private boolean checkExActivityAddition(ExchangeSlot slot) {
        if (slot == null) {
            return false;
        }
        List<ApposeAddition> apposeAdditionList = slot.getApposeAdditionList();
        if (apposeAdditionList == null || apposeAdditionList.isEmpty()) {
            return false;
        }
        for (ApposeAddition apposeAddition : apposeAdditionList) {
            if (!checkApposeAddition(apposeAddition)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkApposeAddition(ApposeAddition apposeAddition) {
        if (apposeAddition == null) {
            return false;
        }

        if (apposeAddition.getCount() <= 0) {
            LogUtil.error("error need count cfg <= 0, count = " + apposeAddition.getCount());
            return false;
        }

        switch (apposeAddition.getType()) {
            case RTE_Diamond:
            case RTE_Gold:
                break;
            case RTE_Item:
                Set<Integer> allSatisfiedItem = getAllSatisfiedItem(apposeAddition.getAdditionList());
                if (allSatisfiedItem == null || allSatisfiedItem.isEmpty()) {
                    LogUtil.info("have no satisfied item");
                    return false;
                }
                break;
            case RTE_Pet:
                Set<Integer> allSatisfiedPet = getAllSatisfiedPet(apposeAddition.getAdditionList());
                if (allSatisfiedPet == null || allSatisfiedPet.isEmpty()) {
                    LogUtil.info("have no satisfied pet");
                    return false;
                }
                break;
            case RTE_PetFragment:
                Set<Integer> allSatisfiedPetFragment = getAllSatisfiedPetFragment(apposeAddition.getAdditionList());
                if (allSatisfiedPetFragment == null || allSatisfiedPetFragment.isEmpty()) {
                    LogUtil.info("have no satisfied fragment");
                    return false;
                }
                break;
            case RTE_Rune:
                Set<Integer> allSatisfiedRune = getAllSatisfiedRune(apposeAddition.getAdditionList());
                if (allSatisfiedRune == null || allSatisfiedRune.isEmpty()) {
                    LogUtil.info("have no satisfied rune");
                    return false;
                }
                break;
            default:
                LogUtil.error("unSupported reward type, type = " + apposeAddition.getType());
                return false;
        }
        return true;
    }

    /**
     * 返回满足指定条件的所有道具id;
     *
     * @param additionList
     * @return 当返回为null 或者为空时,没有满足条件的道具
     */
    public Set<Integer> getAllSatisfiedItem(List<Addition> additionList) {
        if (additionList == null || additionList.isEmpty()) {
            return Item._ix_id.keySet();
        }

        Set<Integer> result = new HashSet<>();
        for (ItemObject value : Item._ix_id.values()) {
            if (itemSatisfied(value, additionList)) {
                result.add(value.getId());
            }
        }
        return result;
    }

    public boolean itemSatisfied(ItemObject itemCfg, List<Addition> additionList) {
        if (itemCfg == null) {
            return false;
        }

        if (additionList == null || additionList.isEmpty()) {
            return true;
        }

        for (Addition addition : additionList) {
            if (addition.getAdditionType() == AdditionEnumType.AET_Id
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), itemCfg.getId())) {
                return false;
            } else if (addition.getAdditionType() == AdditionEnumType.AET_Quality
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), itemCfg.getQuality())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 返回满足指定条件的所有道具id;
     *
     * @param additionList
     * @return 当返回为null 或者为空时,没有满足条件的道具
     */
    public Set<Integer> getAllSatisfiedPet(List<Addition> additionList) {
        if (additionList == null || additionList.isEmpty()) {
            return PetBaseProperties._ix_petid.keySet();
        }

        Set<Integer> result = new HashSet<>();
        for (PetBasePropertiesObject value : PetBaseProperties._ix_petid.values()) {
            if (petSatisfied(value, additionList)) {
                result.add(value.getPetid());
            }
        }
        return result;
    }

    /**
     * 指定配置是否满足指定的条件
     *
     * @param petCfg
     * @param additionList
     * @return
     */
    public boolean petSatisfied(PetBasePropertiesObject petCfg, List<Addition> additionList) {
        if (petCfg == null) {
            return false;
        }

        if (additionList == null || additionList.isEmpty()) {
            return true;
        }

        for (Addition addition : additionList) {
            if (addition.getAdditionType() == AdditionEnumType.AET_Id
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), petCfg.getPetid())) {
                return false;

            } else if (addition.getAdditionType() == AdditionEnumType.AET_Quality
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), petCfg.getStartrarity())) {
                return false;

            } else if (addition.getAdditionType() == AdditionEnumType.AET_PetRace
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), petCfg.getPettype())) {
                return false;

            } else if (addition.getAdditionType() == AdditionEnumType.AET_Level
                    && petCache.getInstance().getPexMaxLv(petCfg.getStartrarity()) < addition.getLowerLimit()) {
                return false;

            } else if (addition.getAdditionType() == AdditionEnumType.AET_Awake
                    && petCfg.getMaxuplvl() < addition.getLowerLimit()) {
                return false;

            } else if (addition.getAdditionType() == AdditionEnumType.AET_Class
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), petCfg.getStartrarity())) {
                return false;
            }
        }

        return true;
    }

    public Set<Integer> getAllSatisfiedRune(List<Addition> additionList) {
        if (additionList == null || additionList.isEmpty()) {
            return PetRuneProperties._ix_runeid.keySet();
        }

        Set<Integer> result = new HashSet<>();
        for (PetRunePropertiesObject value : PetRuneProperties._ix_runeid.values()) {
            if (runeSatisfied(value, additionList)) {
                result.add(value.getRuneid());
            }
        }
        return result;
    }

    /**
     * 指定配置是否满足指定的条件
     *
     * @param runeCfg
     * @param additionList
     * @return
     */
    public boolean runeSatisfied(PetRunePropertiesObject runeCfg, List<Addition> additionList) {
        if (runeCfg == null) {
            return false;
        }

        if (additionList == null || additionList.isEmpty()) {
            return true;
        }

        for (Addition addition : additionList) {
            if (addition.getAdditionType() == AdditionEnumType.AET_Id
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), runeCfg.getRuneid())) {
                return false;

            } else if (addition.getAdditionType() == AdditionEnumType.AET_Quality
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), runeCfg.getRunerarity())) {
                return false;

            } else if (addition.getAdditionType() == AdditionEnumType.AET_Level
                    && PetRuneExp.queryRuneMaxLv(runeCfg.getRunerarity()) < addition.getLowerLimit()) {
                return false;
            }
        }

        return true;
    }

    public Set<Integer> getAllSatisfiedPetFragment(List<Addition> additionList) {
        if (additionList == null || additionList.isEmpty()) {
            return PetRuneProperties._ix_runeid.keySet();
        }

        Set<Integer> result = new HashSet<>();
        for (PetFragmentConfigObject value : PetFragmentConfig._ix_id.values()) {
            if (runeSatisfied(value, additionList)) {
                result.add(value.getId());
            }
        }
        return result;
    }

    /**
     * 指定配置是否满足指定的条件
     *
     * @param petFragmentCfg
     * @param additionList
     * @return
     */
    public boolean runeSatisfied(PetFragmentConfigObject petFragmentCfg, List<Addition> additionList) {
        if (petFragmentCfg == null) {
            return false;
        }

        if (additionList == null || additionList.isEmpty()) {
            return true;
        }

        for (Addition addition : additionList) {
            if (addition.getAdditionType() == AdditionEnumType.AET_Id
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), petFragmentCfg.getId())) {
                return false;

            } else if (addition.getAdditionType() == AdditionEnumType.AET_Quality
                    && GameUtil.outOfScope(addition.getUpLimit(), addition.getLowerLimit(), petFragmentCfg.getDebrisrarity())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public synchronized void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (this.nextTickTime >= currentTime) {
            return;
        }

        settleActivities();
        removeExpireActivity();
        removeExpireActivityNotice();
        sendToPlayerNowOpenActivity();

        queryGiftActivity();
        // boss战
        queryActivityBoss();
        this.nextTickTime = currentTime + TICK_INTERVAL_TIME;
    }

    private void sendToPlayerNowOpenActivity() {
        if (CollectionUtils.isEmpty(scheduledPushActivity)) {
            return;
        }
        List<ServerActivity> needSendActivity = null;
        for (ServerActivity next : scheduledPushActivity) {
            if (next.getStartDisTime() <= GlobalTick.getInstance().getCurrentTime()) {
                needSendActivity = needSendActivity == null ? new ArrayList<>() : needSendActivity;
                needSendActivity.add(next);
            }
        }
        if (!CollectionUtils.isEmpty(needSendActivity)) {
            scheduledPushActivity.removeAll(needSendActivity);
            targetsystemCache.getInstance().sendNewActivityToAllOnlinePlayer(needSendActivity);
        }
    }

    /**
     * 结算活动,主要结算排行榜活动
     */
    private void settleActivities() {
        List<ServerActivity> needSettleList = activities.values()
                .stream()
                .filter(ActivityUtil::activityIsEnd)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(needSettleList)) {
            needSettleList.forEach(this::settleActivity);
        }
    }

    /**
     * 结算活动,暂时只有排行榜活动和魔灵降临需要结算
     *
     * @param activity
     */
    private void settleActivity(ServerActivity activity) {
        if (activity == null || activity.getRankingType() == null || activitySettled(activity)) {
            return;
        }
        addSettledActivity(activity);

        ActivityRankingSettleHandler handler = new ActivityRankingSettleHandler(activity);
        handler.settleRanking();

        LogUtil.info("model.activity.ActivityManager.settleActivity, settle activity id:" + activity.getActivityId()
                + ", type:" + activity.getType() + ", title:" + activity.getTitle());
    }

    /**
     * 添加已经结算的活动
     *
     * @param activity
     */
    private synchronized void addSettledActivity(ServerActivity activity) {
        if (activity == null) {
            return;
        }
        this.alreadySettledActivityIdSet.add(activity.getActivityId());
    }

    /**
     * 判断活动是否已经结算
     *
     * @param activity
     * @return
     */
    private synchronized boolean activitySettled(ServerActivity activity) {
        if (activity == null) {
            return true;
        }
        return this.alreadySettledActivityIdSet.contains(activity.getActivityId());
    }

    /**
     * 移除已经过期的活动
     */
    private synchronized void removeActivityFromSettledActivity(ServerActivity activity) {
        if (activity == null) {
            return;
        }
        this.alreadySettledActivityIdSet.remove(activity.getActivityId());
    }

    private void removeExpireActivityNotice() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        Set<Long> removeId = new HashSet<>();
        for (ServerActivityNotice value : noticesMap.values()) {
            if (currentTime > value.getEndTime()) {
                removeId.add(value.getNoticeId());
            }
        }

        if (!removeId.isEmpty()) {
            for (Long aLong : removeId) {
                noticesMap.remove(aLong);
                LogUtil.info("activity.ActivityManager.removeExpireActivityNotice, delete activity notice , id = "
                        + aLong + ", reason : expire");
            }
        }
    }

    /**
     * 删除已经结束展示的活动
     */
    private void removeExpireActivity() {
        Set<Long> expireSet = new HashSet<>();
        for (ServerActivity value : activities.values()) {
            if (ActivityUtil.activityIsOverDisplay(value)) {
                expireSet.add(value.getActivityId());
            }
        }

        if (CollectionUtils.isNotEmpty(expireSet)) {
            for (Long aLong : expireSet) {
                removeServerActivity(aLong);
            }
        }
    }

    /**
     * 更新超值礼包和限购礼包活动列表
     */
    private void queryGiftActivity() {
        //当前版本屏蔽礼包
//        List<ActivityTypeEnum> typeList = Arrays.asList(ActivityTypeEnum.ATE_LimitGift, ActivityTypeEnum.ATE_WorthGift);
//        for (ActivityTypeEnum activityTypeEnum : typeList) {
//            ClientActivity clientActivity = ExchangeHistoryServiceImpl.getInstance().buildGiftClientActivity(activityTypeEnum);
//            if (clientActivity == null) {
//                removeSpecialActivity(activityTypeEnum);
//            } else {
//                addSpecialActivity(clientActivity);
//            }
//        }
    }

    /**
     * 构建本地Mission
     *
     * @param missionIndex
     * @param beginTime
     * @return
     */
    private ServerExMission.Builder builderMission(int missionIndex, long beginTime) {
        PetExchangeMissionObject mission = PetExchangeMission.getByIndex(missionIndex);
        if (mission == null) {
            LogUtil.error("localActivity mission = " + missionIndex + ", is null");
            return null;
        }

        ServerExMission.Builder exMissionBuilder = ServerExMission.newBuilder();
        exMissionBuilder.setIndex(mission.getIndex());
        exMissionBuilder.setExchangeLimit(mission.getLimit());
        exMissionBuilder.setName(ServerStringRes.getLanguageNumContent(mission.getName()));
        exMissionBuilder.setDesc(ServerStringRes.getLanguageNumContent(mission.getDesc()));
        exMissionBuilder.addAllRewards(RewardUtil.parseRewardIntArrayToRewardList(mission.getRewards()));
        long endTime = mission.getEndtime();
        if (endTime != -1) {
            endTime = beginTime + TimeUtil.MS_IN_A_DAY * endTime;
        }
        exMissionBuilder.setEndTimestamp(endTime);

        int[][] apposeAddition = mission.getApposeaddition();
        if (apposeAddition == null || apposeAddition.length <= 0) {
            LogUtil.error("petExchange activity addition is null");
            return null;
        }

        for (int[] appose : apposeAddition) {
            ExchangeSlot.Builder slot = ExchangeSlot.newBuilder();
            for (int additionList : appose) {
                ExchangeAdditionObject byIndex = ExchangeAddition.getByIndex(additionList);
                if (byIndex == null) {
                    LogUtil.error("addition index is not exist, index = " + additionList);
                    return null;
                }

                Builder builder = ApposeAddition.newBuilder();
                builder.setIndex(byIndex.getIndex());
                RewardTypeEnum rewardTypeEnum = RewardTypeEnum.forNumber(byIndex.getType());
                builder.setType(rewardTypeEnum);

                // 宠物和符文只允许设置一个
                if (rewardTypeEnum == RewardTypeEnum.RTE_Pet || rewardTypeEnum == RewardTypeEnum.RTE_Rune) {
                    builder.setCount(1);
                } else {
                    builder.setCount(byIndex.getCount());
                }
                int[][] addition = byIndex.getAddition();
                if (addition == null || addition.length <= 0) {
                    LogUtil.error("additionCfg = [" + byIndex.getIndex() + "] addition is null");
                    return null;
                }
                for (int[] ints : addition) {
                    if (ints.length < 3) {
                        LogUtil.error("additionCfg = [" + byIndex.getIndex() + "] addition cfg is error, length not enough");
                        return null;
                    }
                    Addition.Builder additionBuilder = Addition.newBuilder();
                    additionBuilder.setAdditionType(AdditionEnumType.forNumber(ints[0]));
                    additionBuilder.setLowerLimit(ints[1]);
                    additionBuilder.setUpLimit(ints[2]);
                    builder.addAddition(additionBuilder);
                }
                slot.addApposeAddition(builder);
            }
            exMissionBuilder.addExSlots(slot);
        }
        return exMissionBuilder;
    }

    private void queryActivityBoss() {
        if (!ActivityBossManager.getInstance().isDisplayed()) {
            removeServerActivity(LocalActivityId.BossBattle);
            return;
        }
        if (this.activities.containsKey((long) LocalActivityId.BossBattle)) {
            return;
        }
        addActivity(ActivityBossManager.getInstance().buildBossActivity());
    }


    /**
     * 计算玩家活动掉落道具
     *
     * @param playerIdx
     * @param resourceEnum
     * @param param
     * @return
     */
    public List<Reward> calculateAllActivityDrop(String playerIdx, DropResourceEnum resourceEnum, long param) {
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null || resourceEnum == null || DropResourceEnum.DRE_Null == resourceEnum) {
            LogUtil.error("model.activity.ActivityManager.calculateAllActivityDrop, error param: playIdx = "
                    + playerIdx + ", resourceEnum =" + resourceEnum + ", param = " + param);
            return null;
        }

        long calculateCount = param;
        if (DropResourceEnum.DRE_MainLineOnHook == resourceEnum) {
            calculateCount = calculateCount / (GameConfig.getById(GameConst.CONFIG_ID).getDropmainlineinterval() * TimeUtil.MS_IN_A_S);
        }

        if (calculateCount <= 0) {
            return null;
        }

        Random random = new Random();
        List<Reward> result = new ArrayList<>();
        List<UpdateActivityDropCount> update = new ArrayList<>();
        for (ServerActivity activity : activities.values()) {
            if (activity.getType() != ActivityTypeEnum.ATE_Exchange || activity.getDropInfoCount() <= 0) {
                continue;
            }

            for (DropInfo dropInfo : activity.getDropInfoList()) {
                int maxCanGet = dropInfo.getDropDailyLimit()
                        - target.getAlreadyGetDropCount(activity.getActivityId(), dropInfo.getDropId());
                if ((dropInfo.getDropDailyLimit() != -1 && maxCanGet <= 0)
                        || !dropInfo.getDropSourceList().contains(resourceEnum)) {
                    continue;
                }

                int rewardCount = 0;
                for (int i = 0; i < calculateCount; i++) {
                    if (dropInfo.getDropOdds() > random.nextInt(1000)) {
                        rewardCount++;
                    }

                    if (rewardCount >= maxCanGet) {
                        break;
                    }
                }

                //有可能不能得到掉落道具
                if (rewardCount <= 0) {
                    continue;
                }

                Reward reward = RewardUtil.parseReward(RewardTypeEnum.RTE_Item, dropInfo.getDropId(), rewardCount);
                if (reward != null) {
                    result.add(reward);
                }
                update.add(new UpdateActivityDropCount(activity.getActivityId(), dropInfo.getDropId(), rewardCount));
            }
        }

        //更新掉落数据
        EventUtil.triggerUpdateDropItemCount(playerIdx, update);
        return result;
    }

    public static boolean activityIsOpen(long activityId, String playerIdx) {
        if (activityId <= 0) {
            return false;
        }
        if (TimeLimitActivity.getById((int) activityId) != null) {
            TimeLimitActivityObject config = TimeLimitActivity.getById((int) activityId);
            if (config == null) {
                return false;
            }
            int endDisTime = config.getEnddistime();
            if (endDisTime == -1) {
                return true;
            }
            targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
            if (target == null) {
                return false;
            }
            return GlobalTick.getInstance().getCurrentTime() <
                    target.getDb_Builder().getSpecialInfo().getTimeLimitActivities().getStartTime() + TimeUtil.MS_IN_A_DAY * endDisTime;
        }

        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(activityId);
        return activity != null && activity.getEndTime() >= GlobalTick.getInstance().getCurrentTime()
                && activity.getBeginTime() <= GlobalTick.getInstance().getCurrentTime();
    }

    @Override
    public void update() {
        gameplayEntity foreignInvasion = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_PlatformActivityInfo);
        if (foreignInvasion == null) {
            foreignInvasion = new gameplayEntity();
        }

        byte[] byteArray = DB_PlatformActivityInfo
                .newBuilder()
                .addAllAlreadySettleActivityId(this.alreadySettledActivityIdSet)
                .build()
                .toByteArray();

        foreignInvasion.setGameplayinfo(byteArray);
        foreignInvasion.putToCache();
    }

    /**
     * 活动是否已经结束
     *
     * @param activityId
     * @return
     */
    public boolean activityIsEnd(long activityId) {
        ServerActivity activity = getActivityCfgById(activityId);
        if (activity == null) {
            return true;
        }
        return ActivityUtil.activityIsEnd(activity);
    }

    /**
     * 查询充值返利活动返利百分比
     *
     * @return
     */
    public int queryRechargeRebateRate() {
        ServerActivity activity = findOneRecentActivityByType(ActivityTypeEnum.ATE_RechargeRebate);
        if (activity == null) {
            return 0;
        }
        return activity.getRebateRate();
    }

    public ServerActivity findGeneraActivityByTemplate(GeneralActivityTemplate template) {
        if (template == null) {
            return null;
        }
        for (ServerActivity activity : activities.values()) {
            if (template == activity.getTemplate()) {
                return activity;
            }
        }
        return null;
    }

    public List<ServerActivity> getActivitiesByRankingType(EnumRankingType rankingType) {
        if (rankingType == null) {
            return null;
        }
        return this.activities.values().stream()
                .filter(e -> e.getRankingType() == rankingType && ActivityUtil.activityInOpen(e))
                .collect(Collectors.toList());
    }

    @Override
    public void updateDailyData() {
        settleActivityBossDamageRanking();
    }

    private void settleActivityBossDamageRanking() {
//        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(ActivityTypeEnum.ATE_BossBattle_VALUE);
//        if (ActivityUtil.activityInOpen(activity)) {
//            settleActivity(activity);
//
//            RankingManager.getInstance().clearRanking(RankingUtils.getActivityRankingName(activity));
//        }
    }

    public List<Activity.SeasonInfo> queryAllSeasonInfo() {
        List<Activity.SeasonInfo> seasonInfos = new ArrayList<>();
        //迷雾森林赛季
        MistSeasonConfigObject curSeason = MistSeasonConfig.getCurSeasonConfig(GlobalTick.getInstance().getCurrentTime());
        if (curSeason != null) {
            Activity.SeasonInfo.Builder builder = Activity.SeasonInfo.newBuilder().setType(Activity.EnumSeasonType.EST_Mist)
                    .setStartTime(curSeason.getStarttime()).setEndTime(curSeason.getEndtime());
            seasonInfos.add(builder.build());
        }
        //远征
        TheWarSeasonConfigObject warOpenConfig = TheWarSeasonConfig.getInstance().getWarOpenConfig();
        if (warOpenConfig != null) {
            Activity.SeasonInfo.Builder builder = Activity.SeasonInfo.newBuilder().setType(Activity.EnumSeasonType.EST_TheWar)
                    .setStartTime(warOpenConfig.getStartplaytime()).setEndTime(warOpenConfig.getEndplaytime());
            seasonInfos.add(builder.build());
        }
        //荣耀之路
        Activity.SeasonInfo.Builder gloryRoadBuilder = Activity.SeasonInfo.newBuilder()
                .setType(EnumSeasonType.EST_GloryRoad)
                .setStartTime(GloryRoadManager.getInstance().getOpenTime())
                .setEndTime(GloryRoadManager.getInstance().getEndTime());
        seasonInfos.add(gloryRoadBuilder.build());

        return seasonInfos;
    }

    public List<ServerActivity> findInOpenRankingActivity(EnumRankingType rankingType) {
        return activities.values().stream().filter(e -> rankingType == e.getRankingType() && ActivityUtil.activityInOpen(e)).collect(Collectors.toList());
    }
}




