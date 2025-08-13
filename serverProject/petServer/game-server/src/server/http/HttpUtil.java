package server.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import model.activity.ActivityManager;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import protocol.Activity;
import protocol.Activity.ActivityNoticeEnum;
import protocol.Activity.ActivityTagEnum;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.Addition;
import protocol.Activity.AdditionEnumType;
import protocol.Activity.ApposeAddition;
import protocol.Activity.DemonDescendsRandom;
import protocol.Activity.EnumRankingType;
import protocol.Activity.ExchangeSlot;
import protocol.Activity.RankingReward;
import protocol.Activity.RewardList;
import protocol.Activity.RuneTreasurePool;
import protocol.Activity.StageRewards;
import protocol.Common.RandomReward;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.GameplayDB.DailyTimeScope;
import protocol.GameplayDB.MarqueeCycle;
import protocol.Server;
import protocol.Server.ActivityDayDayRecharge;
import protocol.Server.ActivityDayDayRecharge.Builder;
import protocol.Server.DropInfo;
import protocol.Server.ServerActivity;
import protocol.Server.ServerActivityNotice;
import protocol.Server.ServerBuyMission;
import protocol.Server.ServerExMission;
import protocol.Server.ServerPlatformPetAvoidance;
import protocol.Server.ServerSubMission;
import protocol.TargetSystem.TargetTypeEnum;
import server.http.entity.PlatformAddition;
import server.http.entity.PlatformApposeAddition;
import server.http.entity.PlatformBuyMission;
import server.http.entity.PlatformConsume;
import server.http.entity.PlatformDayDayRecharge;
import server.http.entity.PlatformDemonDescendsRandom;
import server.http.entity.PlatformDirectPurchaseGift;
import server.http.entity.PlatformDropInfo;
import server.http.entity.PlatformExchangeSlot;
import server.http.entity.PlatformFestivalBoss;
import server.http.entity.PlatformFestivalBossDamageReward;
import server.http.entity.PlatformFestivalBossTreasure;
import server.http.entity.PlatformPetAvoidanceData;
import server.http.entity.PlatformRandomReward;
import server.http.entity.PlatformRankingReward;
import server.http.entity.PlatformRetCode.PlatformBaseRet;
import server.http.entity.PlatformRetCode.RetCode;
import server.http.entity.PlatformReward;
import server.http.entity.PlatformRichMan;
import server.http.entity.PlatformRuneTreasurePool;
import server.http.entity.PlatformServerActivity;
import server.http.entity.PlatformServerExMission;
import server.http.entity.PlatformServerSubMission;
import server.http.entity.PlatformStageRewards;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huhan
 * @date 2020.02.27
 */
public class HttpUtil {
    public static boolean parseAndAddActivities(String jsonBody) {
        List<PlatformServerActivity> activityList = JSONArray.parseArray(jsonBody, PlatformServerActivity.class);
        List<ServerActivity> platformParam = parseSeverActivity(activityList);
        return ActivityManager.getInstance().addAllActivitiesWithCheck(platformParam);
    }

    /**
     * 将临时的serverActivity对象转化为proto对象
     *
     * @param activityList
     * @return
     */
    private static List<ServerActivity> parseSeverActivity(List<PlatformServerActivity> activityList) {
        List<ServerActivity> result = new ArrayList<>();
        for (PlatformServerActivity activity : activityList) {
            ServerActivity.Builder builder = ServerActivity.newBuilder();
            builder.setActivityId(activity.getActivityId());
            if (activity.getTitle() != null) {
                builder.setTitle(activity.getTitle().toString());
            }
            if (activity.getDesc() != null) {
                builder.setDesc(activity.getDesc().toString());
            }
            if (activity.getPictureName() != null) {
                builder.setPictureName(activity.getPictureName());
            }
            builder.setStartDisTime(activity.getStartDisTime());
            builder.setBeginTime(activity.getBeginTime());
            builder.setEndTime(activity.getEndTime());
            builder.setOverDisTime(activity.getOverDisTime());
            builder.setType(ActivityTypeEnum.forNumber(activity.getType()));
            builder.setTemplateValue(activity.getTemplate());
            builder.setTabTypeValue(activity.getTabType());
            builder.setTag(ActivityTagEnum.forNumber(activity.getTag()));
            builder.setRebateRate(activity.getRebateRate());
            if (activity.getDetail() != null) {
                builder.setDetail(activity.getDetail());
            }

            builder.setRedDotTypeValue(activity.getRedDotType());

            List<ServerSubMission> missions = parseMission(activity.getMissions());
            if (CollectionUtils.isNotEmpty(missions)) {
                for (ServerSubMission mission : missions) {
                    builder.putMissions(mission.getIndex(), mission);
                }
            }

            List<DropInfo> dropInfos = parseDropInfo(activity.getDropInfo());
            if (CollectionUtils.isNotEmpty(dropInfos)) {
                builder.addAllDropInfo(dropInfos);
            }

            List<ServerExMission> exMissions = parseExMission(activity.getExMission());
            if (CollectionUtils.isNotEmpty(exMissions)) {
                for (ServerExMission exMission : exMissions) {
                    builder.putExMission(exMission.getIndex(), exMission);
                }
            }

            builder.setRankingTypeValue(activity.getRankingType());
            List<RankingReward> rankingRewards = parseRankingReward(activity.getRankingReward());
            if (CollectionUtils.isNotEmpty(rankingRewards)) {
                //重排序后加入, 由低到高
                rankingRewards.sort(Comparator.comparingInt(RankingReward::getStartRanking));
                builder.addAllRankingReward(rankingRewards);
            }
            //天天充值活动
            ActivityDayDayRecharge dayDayRecharge = parseDayDayRechargeActivity(activity.getPlatformDailyRecharge());
            if (dayDayRecharge != null) {
                builder.setDayDayRecharge(dayDayRecharge);
            }

            List<DemonDescendsRandom> demonRandom = parseDemonDescendsRandom(activity.getDemonDescentsRandom());
            if (CollectionUtils.isNotEmpty(demonRandom)) {
                builder.addAllDemonDescentsRandom(demonRandom);
            }

            //购买活动
            Map<Integer, ServerBuyMission> buyMissions = parseBuyMissionMap(activity.getBuyMissions());
            if (MapUtils.isNotEmpty(buyMissions)) {
                builder.putAllBuyMission(buyMissions);
            }
            if (!CollectionUtils.isEmpty(activity.getDirectPurchaseGifts())) {
                for (PlatformDirectPurchaseGift purchaseGift : activity.getDirectPurchaseGifts()) {
                    builder.addDirectPurchaseGift(toGiftBuilder(purchaseGift));
                }
            }

            if (activity.getFestivalBossData()!=null){
                builder.setFestivalBoss(parsePlatformFestivalBoss(activity.getFestivalBossData()));
                builder.setRankingType(Activity.EnumRankingType.ERT_FestivalBoss);
            }

            //符文密藏
            Map<Integer, StageRewards> stageRewardsMap = parseStageRewardsMap(activity.getStageRewards());
            if (MapUtils.isNotEmpty(stageRewardsMap)) {
                builder.putAllStageRewards(stageRewardsMap);
            }

            List<RuneTreasurePool> runeTreasurePools = parseRuneTreasurePool(activity.getRuneTreasurePools());
            if (CollectionUtils.isNotEmpty(runeTreasurePools)) {
                builder.addAllRuneTreasurePool(runeTreasurePools);
            }
            //大富翁
            Map<Integer, Server.ServerRichManPoint> richMan = parseRichMan(activity.getRichMan());
            if (MapUtils.isNotEmpty(richMan)) {
                builder.putAllRichManPoint(richMan);
            }

            Map<Integer, ServerBuyMission> buyMissionMap = parseRichManBuyMissionMap(activity.getRichMan());
            if (MapUtils.isNotEmpty(buyMissionMap)) {
                builder.putAllBuyMission(buyMissionMap);
            }
            List<Reward> displayRewards = parseToRewardList(activity.getDisplayRewards());
            if (!CollectionUtils.isEmpty(displayRewards)) {
                builder.addAllDisplayRewards(displayRewards);
            }
            builder.setMazeRefreshInterval(activity.getMazeRefreshInterval());

            if (activity.getDailyEndTime() > activity.getDailyBeginTime()) {
                builder.setDailyBeginTime(activity.getDailyBeginTime());
                builder.setDailyEndTime(activity.getDailyEndTime());
                builder.setDailyEndTime(activity.getDailyEndTime());
            }

            // 魔灵大躲避
            PlatformPetAvoidanceData platformPetAvoidanceData = activity.getPetAvoidanceData();
            if (platformPetAvoidanceData != null) {
                ServerPlatformPetAvoidance.Builder spPetAvoidance = ServerPlatformPetAvoidance.newBuilder();
                spPetAvoidance.setDurationTime(platformPetAvoidanceData.getChallengeTime());
                spPetAvoidance.setDailyChallengeTimes(platformPetAvoidanceData.getDailyChallengeTimes());
                builder.setPetAvoidance(spPetAvoidance);
                builder.setRankingType(EnumRankingType.ERT_PetAvoidance);
            }

            if(activity.getStarTreasure() != null){
                builder.setStarTreasure(activity.getStarTreasure().toBuilder());
            }
            result.add(builder.build());
        }
        return result;
    }

    private static Server.ServerPlatformFestivalBoss.Builder parsePlatformFestivalBoss(PlatformFestivalBoss festivalBoss) {
        Server.ServerPlatformFestivalBoss.Builder builder = Server.ServerPlatformFestivalBoss.newBuilder();
        builder.setFightMakeId(festivalBoss.getFightMakeId());
        builder.setPresentConsume(festivalBoss.getPresentConsume().toConsume());
        builder.addAllTreasures(toServerFestivalTreasure(festivalBoss.getTreasures()));
        builder.addAllPresentReward(parseToRewardList(festivalBoss.getPresentReward()));
        builder.setPresentScore(festivalBoss.getPresentScore());
        builder.setRankMinLimitScore(festivalBoss.getRankMinLimitScore());
        builder.setShowRankNum(festivalBoss.getShowRankNum());
        builder.setDailyChallengeTimes(festivalBoss.getDailyChallengeTimes());
        builder.addAllDamageReward(toDamageReward(festivalBoss.getDamageReward()));
        builder.setPetCfgId(festivalBoss.getPetCfgId());
        builder.setExScoreRate(festivalBoss.getExScoreRate());
        PlatformReward shopCurrency = festivalBoss.getShopCurrency();
        if (shopCurrency !=null) {
            builder.setShopCurrency(RewardUtil.parseReward(shopCurrency.getRewardType(),shopCurrency.getId(),shopCurrency.getCount()));
        }
        builder.setShareLink(festivalBoss.getShareLink());
        for (PlatformRandomReward platformRandomReward : festivalBoss.getPresentRandomReward()) {
            builder.addPresentRandomReward(parseRandomReward(platformRandomReward));
        }
        if (CollectionUtils.isNotEmpty(festivalBoss.getShareReward())) {
            builder.addAllShareReward(parseToRewardList(festivalBoss.getShareReward()));
        }
        return builder;
    }

    private static Iterable<Activity.FestivalBossDamageReward> toDamageReward(List<PlatformFestivalBossDamageReward> damageReward) {
        if (CollectionUtils.isEmpty(damageReward)) {
            return Collections.emptyList();
        }
        List<Activity.FestivalBossDamageReward> result = new ArrayList<>();
        for (PlatformFestivalBossDamageReward reward : damageReward) {
            result.add(Activity.FestivalBossDamageReward.newBuilder()
                    .setId(reward.getId()).setDamageStart(reward.getDamageStart())
                    .setDamageEnd(reward.getDamageEnd()).addAllRewards(parseToRewardList(reward.getRewards())).build());
        }
        return result;
    }

    private static   List<Activity.FestivalBossTreasure> toServerFestivalTreasure(List<PlatformFestivalBossTreasure> treasures) {
        List<Activity.FestivalBossTreasure> result = new ArrayList<>();
        for (PlatformFestivalBossTreasure treasure : treasures) {
            result.add(Activity.FestivalBossTreasure.newBuilder()
                    .setId(treasure.getId()).setTarget(treasure.getTarget()).addAllReward(parseToRewardList(treasure.getRewards())).build());
        }
        return result;
    }

    private static Map<Integer, ServerBuyMission> parseRichManBuyMissionMap(PlatformRichMan richMan) {
        if (richMan == null) {
            return Collections.emptyMap();
        }
        Map<Integer, ServerBuyMission> result = new HashMap<>();

        for (PlatformRichMan.RichManPoint point : richMan.getPoints()) {
            Map<Integer, ServerBuyMission> missionMap = parseBuyMissionMap(point.getBuyMissions());
            if (MapUtils.isNotEmpty(missionMap)) {
                result.putAll(missionMap);
            }

        }
        return result;

    }

    private static Map<Integer, Server.ServerRichManPoint> parseRichMan(PlatformRichMan richMan) {
        if (richMan == null) {
            return Collections.emptyMap();
        }

        Map<Integer, Server.ServerRichManPoint> result = new HashMap<>();
        for (PlatformRichMan.RichManPoint point : richMan.getPoints()) {
            Server.ServerRichManPoint.Builder builder = Server.ServerRichManPoint.newBuilder();
            int pointId = point.getPointId();
            builder.setPointId(pointId);
            builder.setPointType(Activity.RichManPointType.forNumber(point.getPointType()));
            builder.setRebate(point.getRebate());
            builder.addAllRewardList(RewardUtil.platformRewards2Rewards(point.getFreeRewards()));
            builder.addAllBuyItem(parseBuyMissionMap(point.getBuyMissions()).values());
            result.put(pointId, builder.build());
        }
        return result;

    }

    private static List<RuneTreasurePool> parseRuneTreasurePool(List<PlatformRuneTreasurePool> runeTreasurePool) {
        if (CollectionUtils.isEmpty(runeTreasurePool)) {
            return null;
        }

        List<RuneTreasurePool> result = new ArrayList<>();
        for (PlatformRuneTreasurePool pool : runeTreasurePool) {
            RuneTreasurePool.Builder builder = RuneTreasurePool.newBuilder();
            builder.setLimited(pool.isLimited());
            RandomReward randomReward = parseRandomReward(pool.getReward());
            if (randomReward != null) {
                builder.setReward(randomReward);
            }
            result.add(builder.build());
        }
        return result;
    }

    private static Map<Integer, StageRewards> parseStageRewardsMap(List<PlatformStageRewards> stageRewards) {
        if (CollectionUtils.isEmpty(stageRewards)) {
            return null;
        }

        Map<Integer, StageRewards> result = new HashMap<>();
        for (PlatformStageRewards stageReward : stageRewards) {
            StageRewards.Builder builder = StageRewards.newBuilder();
            builder.setIndex(stageReward.getIndex());
            builder.setNeedDrawTimes(stageReward.getNeedDrawTimes());
            List<Reward> rewards = parseToRewardList(stageReward.getRewards());
            if (CollectionUtils.isNotEmpty(rewards)) {
                builder.addAllRewards(rewards);
            }
            result.put(builder.getIndex(), builder.build());
        }
        return result;
    }


    private static Activity.DirectPurchaseGift toGiftBuilder(PlatformDirectPurchaseGift gift) {
        Activity.DirectPurchaseGift.Builder builder = Activity.DirectPurchaseGift.newBuilder();
        builder.setGiveVipExp(gift.getVipExp());
        builder.setGiftId(gift.getGiftId());
        builder.setLimitBuy(gift.getLimitBuy());
        builder.setNowPrice(gift.getNowPrice());
        builder.setRechargeProductId(gift.getRechargeProductId());
        builder.setOriginalPrice(gift.getOriginalPrice());
        builder.setOverflowValue(gift.getOverflowValue());
        builder.setRechargeAmount(gift.getRechargeAmount());
        builder.setGiftName(gift.getGiftName());
        builder.setDailyReset(gift.isDailyReset());
        for (PlatformReward reward : gift.getRewards()) {
            builder.addReward(platformRewardToReward(reward));
        }
        return builder.build();
    }

    private static Reward platformRewardToReward(PlatformReward reward) {
        return Reward.newBuilder().setCount(reward.getCount()).setId(reward.getId())
                .setRewardType(RewardTypeEnum.forNumber(reward.getRewardType())).build();
    }

    private static Map<Integer, ServerBuyMission> parseBuyMissionMap(List<PlatformBuyMission> buyMissionList) {
        if (CollectionUtils.isEmpty(buyMissionList)) {
            return Collections.emptyMap();
        }
        Map<Integer, ServerBuyMission> result = new HashMap<>();
        for (PlatformBuyMission platformBuyMission : buyMissionList) {
            ServerBuyMission.Builder buyBuilder = ServerBuyMission.newBuilder();
            buyBuilder.setIndex(platformBuyMission.getIndex());
            buyBuilder.setLimitBuy(platformBuyMission.getLimitBuy());
            buyBuilder.setEndTimestamp(platformBuyMission.getEndTimestamp());
            PlatformConsume price = platformBuyMission.getPrice();
            if (price != null) {
                buyBuilder.setPrice(price.toConsume());
            }
            buyBuilder.setDiscount(platformBuyMission.getDiscount());
            List<Reward> rewardList = parseToRewardList(platformBuyMission.getRewards());
            if (CollectionUtils.isNotEmpty(rewardList)) {
                buyBuilder.addAllRewards(rewardList);
            }
            buyBuilder.setTitle(platformBuyMission.getTitle());
            buyBuilder.setSpecialType(platformBuyMission.getSpecialType());

            result.put(buyBuilder.getIndex(), buyBuilder.build());
        }
        return result;
    }

    private static List<DemonDescendsRandom> parseDemonDescendsRandom(List<PlatformDemonDescendsRandom> demonDescentsRandom) {
        if (CollectionUtils.isEmpty(demonDescentsRandom)) {
            return null;
        }

        List<DemonDescendsRandom> result = new ArrayList<>();
        for (PlatformDemonDescendsRandom platformDemonDescendsRandom : demonDescentsRandom) {
            DemonDescendsRandom.Builder demonRandomBuilder = DemonDescendsRandom.newBuilder();
            RandomReward randomReward = parseRandomReward(platformDemonDescendsRandom.getRandomRewards());
            if (randomReward != null) {
                demonRandomBuilder.setRandomRewards(randomReward);
            }
            demonRandomBuilder.setRewardLv(platformDemonDescendsRandom.getRewardLv());
            demonRandomBuilder.setGrandPrize(platformDemonDescendsRandom.isGrandPrize());
            result.add(demonRandomBuilder.build());
        }
        return result;
    }


    private static ActivityDayDayRecharge parseDayDayRechargeActivity(PlatformDayDayRecharge platformDailyRecharge) {
        if (platformDailyRecharge == null || CollectionUtils.isEmpty(platformDailyRecharge.getRechargeRewards())) {
            return null;
        }
        Builder dayDayRecharge = ActivityDayDayRecharge.newBuilder();
        dayDayRecharge.setAdMessage(platformDailyRecharge.getAdMessage());
        dayDayRecharge.setDailyTarget(platformDailyRecharge.getDailyTarget());
        if (CollectionUtils.isNotEmpty(platformDailyRecharge.getFreeRewards())) {
            for (List<PlatformReward> rewards : platformDailyRecharge.getFreeRewards()) {
                dayDayRecharge.addFreeRewards(platformRewardToRewardList(rewards));
            }
        }
        if (CollectionUtils.isEmpty(platformDailyRecharge.getRechargeRewards())) {
            LogUtil.error("platformDailyRecharge rechargeRewards is empty");
            return null;
        }
        for (List<PlatformReward> rewards : platformDailyRecharge.getRechargeRewards()) {
            dayDayRecharge.addRechargeRewards(platformRewardToRewardList(rewards));
        }
        if (!CollectionUtils.isEmpty(platformDailyRecharge.getRewardWorth())) {
            dayDayRecharge.addAllRewardWorth(platformDailyRecharge.getRewardWorth());
        }
        return dayDayRecharge.build();

    }

    private static List<Reward> parseToRewardList(List<PlatformReward> rewards) {
        if (CollectionUtils.isEmpty(rewards)) {
            return null;
        }
        List<Reward> result = new ArrayList<>();
        for (PlatformReward reward : rewards) {
            Reward tempReward = RewardUtil.parseReward(reward.getRewardType(), reward.getId(), reward.getCount());
            if (tempReward != null) {
                result.add(tempReward);
            }
        }
        return result;
    }

    private static RewardList platformRewardToRewardList(List<PlatformReward> rewards) {
        List<Reward> rewardList = parseToRewardList(rewards);
        if (CollectionUtils.isEmpty(rewardList)) {
            return null;
        } else {
            return RewardList.newBuilder().addAllReward(rewardList).build();
        }
    }

    public static List<RankingReward> parseRankingReward(List<PlatformRankingReward> rankingRewards) {
        if (CollectionUtils.isEmpty(rankingRewards)) {
            return null;
        }

        List<RankingReward> result = new ArrayList<>();
        for (PlatformRankingReward rankingReward : rankingRewards) {
            RankingReward.Builder subBuilder = RankingReward.newBuilder();
            subBuilder.setStartRanking(rankingReward.getStartRanking());
            subBuilder.setEndRanking(rankingReward.getEndRanking());

            List<PlatformReward> rewards = rankingReward.getRewards();
            if (CollectionUtils.isNotEmpty(rewards)) {
                for (PlatformReward reward : rewards) {
                    Reward tempReward = RewardUtil.parseReward(reward.getRewardType(), reward.getId(), reward.getCount());
                    if (tempReward != null) {
                        subBuilder.addRewards(tempReward);
                    }
                }
            }
            result.add(subBuilder.build());
        }
        return result;
    }

    public static List<DropInfo> parseDropInfo(List<PlatformDropInfo> dropInfos) {
        if (CollectionUtils.isEmpty(dropInfos)) {
            return null;
        }
        List<DropInfo> result = new ArrayList<>();
        for (PlatformDropInfo platformDropInfo : dropInfos) {
            if (platformDropInfo.getDropId() <= 0) {
                continue;
            }
            DropInfo.Builder subBuilder = DropInfo.newBuilder();
            subBuilder.setDropId(platformDropInfo.getDropId());
            subBuilder.setDropOdds(platformDropInfo.getDropOdds());
            subBuilder.setDropDailyLimit(platformDropInfo.getDropDailyLimit());
            if (platformDropInfo.getDropSource() != null) {
                subBuilder.addAllDropSourceValue(platformDropInfo.getDropSource());
            }
            result.add(subBuilder.build());
        }
        return result;
    }

    public static List<ServerSubMission> parseMission(List<PlatformServerSubMission> missions) {
        if (CollectionUtils.isEmpty(missions)) {
            return null;
        }

        List<ServerSubMission> result = new ArrayList<>();
        for (PlatformServerSubMission mission : missions) {
            ServerSubMission.Builder subBuilder = ServerSubMission.newBuilder();
            subBuilder.setIndex(mission.getIndex());
            subBuilder.setSubType(TargetTypeEnum.forNumber(mission.getSubType()));
            subBuilder.setAdditon(mission.getAddition());
            subBuilder.setTarget(mission.getTarget());
            if (mission.getName() != null) {
                subBuilder.setName(mission.getName().toString());
            }
            if (mission.getDesc() != null) {
                subBuilder.setDesc(mission.getDesc().toString());
            }
            if (mission.getReward() != null) {
                for (PlatformReward platformReward : mission.getReward()) {
                    Reward.Builder rewardBuilder = Reward.newBuilder();
                    rewardBuilder.setId(platformReward.getId());
                    rewardBuilder.setRewardType(RewardTypeEnum.forNumber(platformReward.getRewardType()));
                    rewardBuilder.setCount(platformReward.getCount());
                    subBuilder.addReward(rewardBuilder);
                }
            }
            if (mission.getRandoms() != null) {
                for (PlatformRandomReward platformRandomReward : mission.getRandoms()) {
                    RandomReward randomReward = parseRandomReward(platformRandomReward);
                    if (randomReward != null) {
                        subBuilder.addRandoms(randomReward);
                    }
                }
            }
            subBuilder.setRandomTimes(mission.getRandomTimes());
            subBuilder.setEndTimestamp(mission.getEndTimestamp());
            result.add(subBuilder.build());
        }
        return result;
    }

    private static RandomReward parseRandomReward(PlatformRandomReward platformRandomReward) {
        if (platformRandomReward == null) {
            return null;
        }

        RandomReward.Builder randomRewardBuilder = RandomReward.newBuilder();
        randomRewardBuilder.setRewardType(RewardTypeEnum.forNumber(platformRandomReward.getRewardType()));
        randomRewardBuilder.setId(platformRandomReward.getId());
        randomRewardBuilder.setCount(platformRandomReward.getCount());
        randomRewardBuilder.setRandomOdds(platformRandomReward.getRandomOdds());
        return randomRewardBuilder.build();
    }

    public static List<ServerExMission> parseExMission(List<PlatformServerExMission> exMissions) {
        if (CollectionUtils.isEmpty(exMissions)) {
            return null;
        }

        List<ServerExMission> result = new ArrayList<>();

        for (PlatformServerExMission platformServerExMission : exMissions) {
            ServerExMission.Builder subBuilder = ServerExMission.newBuilder();
            subBuilder.setIndex(platformServerExMission.getIndex());
            if (platformServerExMission.getName() != null) {
                subBuilder.setName(platformServerExMission.getName().toString());
            }
            if (platformServerExMission.getDesc() != null) {
                subBuilder.setDesc(platformServerExMission.getDesc().toString());
            }
            subBuilder.setExchangeLimit(platformServerExMission.getExchangeLimit());
            if (platformServerExMission.getExSlots() != null) {
                for (PlatformExchangeSlot exSlot : platformServerExMission.getExSlots()) {
                    ExchangeSlot.Builder slotBuilder = ExchangeSlot.newBuilder();
                    if (exSlot.getApposeAddition() == null) {
                        continue;
                    }

                    for (PlatformApposeAddition platformApposeAddition : exSlot.getApposeAddition()) {
                        ApposeAddition.Builder apposeBuilder = ApposeAddition.newBuilder();
                        apposeBuilder.setIndex(platformApposeAddition.getIndex());
                        apposeBuilder.setType(RewardTypeEnum.forNumber(platformApposeAddition.getType()));
                        apposeBuilder.setCount(platformApposeAddition.getCount());
                        if (platformApposeAddition.getAddition() != null) {
                            for (PlatformAddition platformAddition : platformApposeAddition.getAddition()) {
                                AdditionEnumType additionType = AdditionEnumType.forNumber(platformAddition.getAdditionType());
                                if (additionType == null || additionType == AdditionEnumType.AET_Null) {
                                    continue;
                                }
                                Addition.Builder additionBuilder = Addition.newBuilder();
                                additionBuilder.setAdditionType(additionType);
                                additionBuilder.setUpLimit(platformAddition.getUpLimit());
                                additionBuilder.setLowerLimit(platformAddition.getLowerLimit());
                                apposeBuilder.addAddition(additionBuilder);
                            }
                        }
                        slotBuilder.addApposeAddition(apposeBuilder);
                    }
                    subBuilder.addExSlots(slotBuilder);
                }
            }
            if (platformServerExMission.getRewards() != null) {
                for (PlatformReward platformReward : platformServerExMission.getRewards()) {
                    Reward.Builder rewardBuilder = Reward.newBuilder();
                    rewardBuilder.setId(platformReward.getId());
                    rewardBuilder.setRewardType(RewardTypeEnum.forNumber(platformReward.getRewardType()));
                    rewardBuilder.setCount(platformReward.getCount());
                    subBuilder.addRewards(rewardBuilder);
                }
            }
            subBuilder.setEndTimestamp(platformServerExMission.getEndTimestamp());
            subBuilder.setVisualFlag(platformServerExMission.getVisualFlag());
            result.add(subBuilder.build());
        }
        return result;
    }

    /**
     * 转化活动公告并添加
     *
     * @param body
     * @return
     */
    public static PlatformBaseRet parseActivityNoticeAndAdd(String body) {
        JSONArray array = JSONObject.parseArray(body);
        List<ServerActivityNotice> notices = new ArrayList<>();
        for (Object obj : array) {
            if (!(obj instanceof JSONObject)) {
                return new PlatformBaseRet(RetCode.failed, "不支持的类型");
            }
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject.containsKey("noticeId") && jsonObject.containsKey("startTime")
                    && jsonObject.containsKey("endTime") && jsonObject.containsKey("sidebar")
                    && jsonObject.containsKey("body") && jsonObject.containsKey("noticeType")
                    && jsonObject.containsKey("annex")) {

                ServerActivityNotice.Builder builder = ServerActivityNotice.newBuilder();
                builder.setNoticeId(jsonObject.getLongValue("noticeId"));
                builder.setStartTime(jsonObject.getLongValue("startTime"));
                builder.setEndTime(jsonObject.getLongValue("endTime"));
                builder.putAllSidebar(GameUtil.parseStrToLanguageNumContentMap(jsonObject.getString("sidebar")));
                builder.putAllBody(GameUtil.parseStrToLanguageNumContentMap(jsonObject.getString("body")));
                builder.setNoticeType(ActivityNoticeEnum.forNumber(jsonObject.getIntValue("noticeType")));

                //设置展示的奖励
                List<PlatformReward> annex = JSONArray.parseArray(jsonObject.getString("annex"), PlatformReward.class);
                if (annex == null) {
                    LogUtil.warn("activity notice id = " + builder.getNoticeId() + "rewardList is empty");
                } else {
                    for (PlatformReward platformReward : annex) {
                        Reward reward = RewardUtil.parseReward(platformReward.getRewardType(), platformReward.getId(), platformReward.getCount());
                        if (reward == null) {
                            LogUtil.warn("activity notice id = " + builder.getNoticeId() + "reward is not exist : " + platformReward.toString());
                        } else {
                            builder.addRewards(reward);
                        }
                    }
                }

                if (builder.getNoticeType() == ActivityNoticeEnum.ANE_PlainText) {
                    if (!jsonObject.containsKey("title")) {
                        return new PlatformBaseRet(RetCode.failed, "字段不完整");
                    }
                    builder.putAllTitle(GameUtil.parseStrToLanguageNumContentMap(jsonObject.getString("title")));
                } else if (builder.getNoticeType() == ActivityNoticeEnum.ANE_Picture) {
                    if (!(jsonObject.containsKey("activityStartTime") && jsonObject.containsKey("activityEndTime")
                            && jsonObject.containsKey("picture"))) {
                        return new PlatformBaseRet(RetCode.failed, "字段不完整");
                    }

                    builder.setPicture(jsonObject.getString("picture"));
                    builder.setActivityStartTime(jsonObject.getLongValue("activityStartTime"));
                    builder.setActicityEndTime(jsonObject.getLongValue("activityEndTime"));
                }

                //非必须字段(hyperlink) 超链接,只允许存在一个超链接
                if (jsonObject.containsKey("innerHyperlink")) {
                    builder.setInnerHyperlink(TargetTypeEnum.forNumber(jsonObject.getInteger("innerHyperlink")));
                } else if (jsonObject.containsKey("httpHyperlink")) {
                    builder.setHttpHyperlink(jsonObject.getString("httpHyperlink"));
                }

                if (!ActivityManager.checkNotice(builder.build())) {
                    return new PlatformBaseRet(RetCode.failed, "参数设置错误");
                }

                notices.add(builder.build());
            } else {
                return new PlatformBaseRet(RetCode.failed, "字段不完整");
            }
        }
        ActivityManager.getInstance().addAllActivityNotice(notices);
        return new PlatformBaseRet(RetCode.success);
    }

    /**
     * 转化跑马灯的循环
     *
     * @param cycle
     * @return
     */
    public static MarqueeCycle parseMarqueeCycle(JSONObject cycle) {
        if (cycle == null) {
            return null;
        }
        if (!cycle.containsKey("cycleType")) {
            return null;
        }

        int cycleValue = cycle.getIntValue("cycleType");
        if (cycleValue == 0) {
            return null;
        }

        MarqueeCycle.Builder result = MarqueeCycle.newBuilder();
        result.setCycleTypeValue(cycleValue);
        if (cycle.containsKey("validDay")) {
            for (Object value : cycle.getJSONArray("validDay")) {
                try {
                    result.addValidDay((Integer) value);
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
            }
        }

        if (cycle.containsKey("scopes")) {
            for (Object scopes : cycle.getJSONArray("scopes")) {
                if (!(scopes instanceof JSONObject)) {
                    continue;
                }
                JSONObject arr = (JSONObject) scopes;
                if (!arr.containsKey("dayOfStartTime") || !arr.containsKey("dayOfEndTime")) {
                    continue;
                }
                try {
                    result.addScopes(DailyTimeScope.newBuilder()
                            .setDayOfStartTime(arr.getLongValue("dayOfStartTime"))
                            .setDayOfEndTime(arr.getLongValue("dayOfEndTime")));
                } catch (Exception e) {
                    LogUtil.printStackTrace(e);
                }
            }
        }
        return result.build();
    }

}
