//package model.foreigninvasion.oldVersion;
//
//
//import cfg.FightMake;
//import cfg.ForInvBossCloneCfg;
//import cfg.ForInvBossCloneCfgObject;
//import cfg.ForeignInvasionParamConfig;
//import cfg.ForeignInvasionParamConfigObject;
//import cfg.FunctionOpenLvConfig;
//import cfg.MailTemplateUsed;
//import cfg.MonsterDifficulty;
//import cfg.MonsterDifficultyObject;
//import cfg.ServerStringRes;
//import com.google.protobuf.GeneratedMessageV3;
//import com.google.protobuf.InvalidProtocolBufferException;
//import common.GameConst;
//import common.GameConst.RankingName;
//import common.GlobalData;
//import common.HttpRequestUtil;
//import common.IdGenerator;
//import common.entity.HttpRankingResponse;
//import common.entity.RankingQueryRequest;
//import common.entity.RankingQueryResult;
//import common.entity.RankingQuerySingleResult;
//import common.entity.RankingUpdateRequest;
//import common.load.ServerConfig;
//import common.tick.GlobalTick;
//import common.tick.Tickable;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Random;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import lombok.Getter;
//import model.activity.ActivityManager;
//import model.gameplay.GamePlayerUpdate;
//import model.gameplay.dbCache.gameplayCache;
//import model.gameplay.entity.gameplayEntity;
//import model.player.dbCache.playerCache;
//import model.player.util.PlayerUtil;
//import model.reward.RewardUtil;
//import org.apache.commons.collections4.MapUtils;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.util.CollectionUtils;
//import platform.logs.ReasonManager;
//import platform.logs.ReasonManager.Reason;
//import protocol.Activity.ActivityTime;
//import protocol.Activity.ActivityTypeEnum;
//import protocol.Activity.ClientActivity;
//import protocol.Activity.CycleTypeEnum;
//import protocol.Activity.Cycle_Week;
//import protocol.Common.EnumFunction;
//import protocol.Common.RewardSourceEnum;
//import protocol.Gameplay.BossCloneInfo;
//import protocol.Gameplay.ForeignInvasionStatusEnum;
//import protocol.Gameplay.MonsterInfo;
//import protocol.Gameplay.RankingPlayerInfo;
//import protocol.Gameplay.SC_BossInfo;
//import protocol.Gameplay.SC_BossKilledBarrage;
//import protocol.Gameplay.SC_ForInvStatus;
//import protocol.Gameplay.SC_MonsterInfos;
//import protocol.Gameplay.SC_MonsterKillCondition;
//import protocol.Gameplay.SC_RankingInfo;
//import protocol.Gameplay.SC_RefreashBossBV;
//import protocol.Gameplay.SC_RefreshBossClone;
//import protocol.Gameplay.SC_RemainMonsterCount;
//import protocol.GameplayDB.DB_ForInvPlayerInfo;
//import protocol.GameplayDB.DB_ForInvPlayerInfo.Builder;
//import protocol.GameplayDB.DB_ForeignInvasion;
//import protocol.GameplayDB.GameplayTypeEnum;
//import protocol.MessageId.MsgIdEnum;
//import protocol.TargetSystem.TargetTypeEnum;
//import server.push.ForeignInvasionPushManage;
//import util.ArrayUtil;
//import util.EventUtil;
//import util.GameUtil;
//import util.LogUtil;
//import util.TimeUtil;
//
//public class ForeignInvasionManager implements Tickable, GamePlayerUpdate {
//    private static ForeignInvasionManager ourInstance = new ForeignInvasionManager();
//
//    private static final String FOR_INV_IDX = String.valueOf(GameplayTypeEnum.ATE_ForeignInvasion_VALUE);
//
//    private ForeignInvasionParamConfigObject forInvCfg;
//    private DB_ForeignInvasion.Builder foreignInvasionInfo;
//
//    /**
//     * 活动开始时间
//     **/
//    @Getter
//    private long startTime;
//    /**
//     * 第一阶段开始时间
//     **/
//    private long firstStageTime;
//    /**
//     * 过渡阶段开始时间
//     **/
//    private long transitionStageTime;
//    /**
//     * 第二阶段开始时间
//     **/
//    private long secondStageTime;
//    /**
//     * 活动结束时间
//     **/
//    @Getter
//    private long endTime;
//    /**
//     * 结算时间
//     **/
//    private long settleTime;
//    /**
//     * 第一阶段小怪剩余数刷新时间
//     **/
//    private long nextSendRemainMonsterCountTime;
//    /**
//     * 刷新boss血量
//     **/
//    private long nextRefreshBossInfoTime;
//    /**
//     * 下次更新排行榜时间
//     **/
//    private static long nextRefreshRankingTime;
//
//    private ForeignInvasionStatusEnum status;
//
//    //发送当前阶段状态以及信息
//    private boolean sendCurStageStatusAndInfo;
//
//    private static int openLvLimit;
//
//    /**
//     * 玩家击杀情况以及玩家的buffer情况等，需要进数据库
//     **/
//    private Map<String, DB_ForInvPlayerInfo.Builder> playerInfoMap = new ConcurrentHashMap<>();
//    /**
//     * Map<playerIdx,<monsterIdx,monsterInfo>> //保存玩家小怪信息，不进数据库
//     **/
//    private Map<String, Map<String, MonsterInfo>> monsterInfoMap = new ConcurrentHashMap<>();
//    private Map<String, Integer> bossFightMake = new ConcurrentHashMap<>();
//    /**
//     * Map<PlayerIdx,List<bossCloneInfo>>  保存玩家boss分身信息,不进数据库
//     **/
//    private Map<String, List<BossCloneInfo>> bossCloneInfoMap = new ConcurrentHashMap<>();
//    /**
//     * 排行榜信息列表,根据玩家保存
//     **/
//    private Map<String, RankingQuerySingleResult> rankingPlayerInfo = new ConcurrentHashMap<>();
//    /**
//     * 排行榜信息,需要发送给玩家的预先封装的排行榜信息
//     **/
//    private List<RankingPlayerInfo> rankingInfo = new ArrayList<>();
//
//    private int bossCloneTotalRate;
//
//    /**
//     * 创建小怪的最小间隔时间
//     **/
//    private long minRecreateTime;
//    /**
//     * 用于伪造玩家的名字使用,只读
//     **/
//    private final List<String> randomNameList = new ArrayList<>();
//    private AtomicInteger curIndex = new AtomicInteger();
//
//    private ForeignInvasionManager() {
//    }
//
//    public static ForeignInvasionManager getInstance() {
//        return ourInstance;
//    }
//
//    private void clear() {
//        startTime = 0;
//        firstStageTime = 0;
//        transitionStageTime = 0;
//        secondStageTime = 0;
//        endTime = 0;
//        settleTime = 0;
//
//        status = ForeignInvasionStatusEnum.FISE_IdleState;
//
//        nextRefreshRankingTime = 0;
//        nextRefreshBossInfoTime = 0;
//
//        sendCurStageStatusAndInfo = false;
//
//        playerInfoMap.clear();
//        monsterInfoMap.clear();
//        bossCloneInfoMap.clear();
//        bossFightMake.clear();
//
//        rankingPlayerInfo.clear();
//        rankingInfo.clear();
//        randomNameList.clear();
//
//        //清空排行榜
//        clearRanking();
//    }
//
//    public boolean init() {
//        gameplayEntity forInvInfo = gameplayCache.getByIdx(FOR_INV_IDX);
//        if (forInvInfo != null) {
//            try {
//                foreignInvasionInfo = DB_ForeignInvasion.parseFrom(forInvInfo.getGameplayinfo()).toBuilder();
//            } catch (InvalidProtocolBufferException e) {
//                LogUtil.printStackTrace(e);
//                foreignInvasionInfo = DB_ForeignInvasion.newBuilder();
//            }
//        } else {
//            foreignInvasionInfo = DB_ForeignInvasion.newBuilder();
//        }
//
//        ForeignInvasionParamConfigObject foreignInvasionConfig = ForeignInvasionParamConfig.getById(GameConst.CONFIG_ID);
//        if (foreignInvasionConfig == null || !GameUtil.checkCfgParams(foreignInvasionConfig)) {
//            LogUtil.error("ForeignInvasionConfigObject is null");
//            return false;
//        }
//        forInvCfg = foreignInvasionConfig;
//
//        status = ForeignInvasionStatusEnum.FISE_IdleState;
//        minRecreateTime = ArrayUtil.getMinInt(forInvCfg.getDelaytime(), 10) * TimeUtil.MS_IN_A_S;
//        openLvLimit = FunctionOpenLvConfig.getOpenLv(EnumFunction.ForeignInvasion);
//
//        if (!checkCfg() || !initBossCloneRate()) {
//            return false;
//        }
//
//        if (!refreshForInvConfig()) {
//            return false;
//        }
//
//        if (foreignInvasionInfo.getPlayerInfosCount() > 0) {
//            for (Builder builder : foreignInvasionInfo.getPlayerInfosBuilderList()) {
//                playerInfoMap.put(builder.getPlayerIdx(), builder);
//            }
//        }
//        initForeignInvasionPushManage();
//        return GlobalTick.getInstance().addTick(this) && gameplayCache.getInstance().addToUpdateSet(this);
//    }
//
//    private void initForeignInvasionPushManage() {
//        ForeignInvasionPushManage.getInstance();
//    }
//
//    private boolean checkCfg() {
//        Map<Integer, MonsterDifficultyObject> ix_id = MonsterDifficulty._ix_id;
//        if (MapUtils.isEmpty(ix_id)) {
//            LogUtil.error("forInv monster cf is null");
//            return false;
//        }
//
//        for (MonsterDifficultyObject value : ix_id.values()) {
//            if (value.getId() < openLvLimit) {
//                continue;
//            }
//
//            int[][] monsterCfg = value.getForeigninvasionmonstercfg();
//            if (monsterCfg == null || monsterCfg.length <= 0) {
//                LogUtil.error("line up is null, id = " + value.getId());
//                return false;
//            }
//
//            for (int[] ints : monsterCfg) {
//                if (ints.length != 4
//                        || FightMake.getById(ints[0]) == null
//                        || (ints[1] != 1 && ints[1] != 2)
//                        || ints[2] <= 0
//                        || ints[3] <= 0) {
//                    LogUtil.error("line up param cfg error");
//                    return false;
//                }
//            }
//
//            if (FightMake.getById(value.getForeigninvasionbosscfg()) == null) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public synchronized boolean initBossCloneRate() {
//        Map<Integer, ForInvBossCloneCfgObject> ix_id = ForInvBossCloneCfg._ix_id;
//        if (ix_id == null || ix_id.isEmpty()) {
//            LogUtil.error("forInv boss clone cfg is null");
//            return false;
//        }
//
//        for (ForInvBossCloneCfgObject value : ix_id.values()) {
//            bossCloneTotalRate += value.getAppearrate();
//        }
//        return true;
//    }
//
//    /**
//     * 刷新外敌入侵玩法配置
//     *
//     * @return
//     */
//    private synchronized boolean refreshForInvConfig() {
//        clear();
//
//        refreshBossBlood();
//
//        long nextOpenDayStamp = getNextOpenDayStamp();
//        //开始时间
//        startTime = TimeUtil.getTodayStamp(nextOpenDayStamp) + forInvCfg.getBegintime() * TimeUtil.MS_IN_A_MIN;
//        //第一阶段时间
//        firstStageTime = TimeUtil.getTodayStamp(nextOpenDayStamp) + forInvCfg.getFirststagestarttime() * TimeUtil.MS_IN_A_MIN;
//        //下次发送剩余怪物数时间
//        nextSendRemainMonsterCountTime = firstStageTime;
//        //第一阶段最大持续时间
//        long firstStageMaxTime = forInvCfg.getFirststagemaxtime() * TimeUtil.MS_IN_A_MIN;
//        //过渡阶段开始时间
//        transitionStageTime = firstStageTime + firstStageMaxTime;
//        //第二阶段开始时间
//        secondStageTime = transitionStageTime + forInvCfg.getTransitiontime() * TimeUtil.MS_IN_A_MIN;
//        //刷新boss信息时间
//        nextRefreshBossInfoTime = secondStageTime + forInvCfg.getBossbvrefreashinterval() * TimeUtil.MS_IN_A_S;
//        //第二阶段最长持续时间
//        long secondStageMaxTime = forInvCfg.getSecondstagemaxtime() * TimeUtil.MS_IN_A_MIN;
//        //下次发送排行榜时间
//        nextRefreshRankingTime = secondStageTime + forInvCfg.getRankingrefreashinterval() * TimeUtil.MS_IN_A_S;
//        //活动结束时间
//        endTime = firstStageTime + firstStageMaxTime + forInvCfg.getTransitiontime() * TimeUtil.MS_IN_A_MIN + secondStageMaxTime;
//        //结算时间
//        settleTime = endTime + forInvCfg.getSettledelaytime() * TimeUtil.MS_IN_A_MIN;
//
//        updateActivity();
//
//        setFirstOpenTime();
//        return true;
//    }
//
//    /**
//     * 设置第一次开放时间用于计算排行榜奖励发放
//     */
//    private void setFirstOpenTime() {
//        if (this.foreignInvasionInfo.getFirstOpenTime() == 0) {
//            this.foreignInvasionInfo.setFirstOpenTime(this.startTime);
//        }
//    }
//
//    /**
//     * 初始化玩家monster，并放入相应的队列
//     *
//     * @param playerIdx
//     * @return
//     */
//    private Map<String, MonsterInfo> initPlayerMonster(String playerIdx) {
//        if (StringUtils.isBlank(playerIdx)) {
//            return null;
//        }
//        Map<String, MonsterInfo> monsterInfo = randomMonster(forInvCfg.getFirststagemonstercount(), playerIdx);
//
//        if (monsterInfo != null) {
//            monsterInfoMap.put(playerIdx, monsterInfo);
//            //设置玩家bossId
//            MonsterDifficultyObject diffCfg = MonsterDifficulty.getByPlayerIdx(playerIdx);
//            if (diffCfg != null) {
//                bossFightMake.put(playerIdx, diffCfg.getForeigninvasionbosscfg());
//            }
//            return monsterInfo;
//        }
//
//        return null;
//    }
//
//    /**
//     * 刷新怪物血量
//     */
//    private void refreshBossBlood() {
//        long updateTime = TimeUtil.parseTime(forInvCfg.getRefreashtime());
//        if ((GlobalTick.getInstance().getCurrentTime() > updateTime && updateTime != foreignInvasionInfo.getLastRefreashTime())
//                || foreignInvasionInfo.getBossBloodVolume() <= 0) {
//            foreignInvasionInfo.clear();
//            foreignInvasionInfo.setBossBloodVolume(forInvCfg.getBossbloodvolume());
//            foreignInvasionInfo.setLastRefreashTime(updateTime);
//        }
//
//        if (foreignInvasionInfo.getDefeatConditionCount() >= forInvCfg.getBloodvolumechangesneedcount()) {
//            List<Boolean> conditionList = foreignInvasionInfo.getDefeatConditionList();
//            List<Boolean> defeats = new ArrayList<>();
//
//            for (int i = conditionList.size() - 1; i >= 0; i--) {
//                if (defeats.size() == 0) {
//                    defeats.add(conditionList.get(i));
//                    continue;
//                }
//
//                if (!conditionList.get(i).equals(defeats.get(0))) {
//                    foreignInvasionInfo.clearDefeatCondition();
//                    foreignInvasionInfo.addAllDefeatCondition(defeats);
//                    return;
//                }
//                defeats.add(conditionList.get(i));
//            }
//
//            long bossBloodVolume = foreignInvasionInfo.getBossBloodVolume();
//            if (conditionList.get(0)) {
//                bossBloodVolume = (bossBloodVolume * (100 + forInvCfg.getRiseratio())) / 100;
//            } else {
//                bossBloodVolume = (bossBloodVolume * (100 - forInvCfg.getLowerratio())) / 100;
//            }
//            foreignInvasionInfo.setBossBloodVolume(Math.max(bossBloodVolume, forInvCfg.getBossminbloodvolume()));
//        }
//    }
//
//    /**
//     * 获取下一个开放日的当天时间时间戳(如当前8:00,获取下一个开放日8:00)
//     *
//     * @param curTime
//     * @return
//     */
//    public long getNextOpenDayCurStamp(long curTime) {
//        int todayOfWeek = TimeUtil.getDayOfWeek(curTime);
//        int[] openDayArr = forInvCfg.getOpenday();
//        return TimeUtil.getNextDayInWeekTime(curTime, model.foreignInvasion.ForeignInvasionUtil.getNextOpenDay(openDayArr, todayOfWeek));
//    }
//
//    public long getNextOpenDayStamp() {
//        int[] openDayArr = forInvCfg.getOpenday();
//        long curTime = GlobalTick.getInstance().getCurrentTime();
//
//        long lastSettleTime = foreignInvasionInfo.getLastSettleTime();
//
//        long todayStamp = TimeUtil.getTodayStamp(curTime);
//        long todayBeginTime = todayStamp + forInvCfg.getFirststagestarttime() * TimeUtil.MS_IN_A_MIN;
////        long todayEndTime = todayBeginTime + (forInvCfg.getFirststagemaxtime() + forInvCfg.getTransitiontime()
////                + forInvCfg.getSecondstagemaxtime()) * TimeUtil.MS_IN_A_MIN;
//
//        //如果当前时间在开放日且上次结算时间在当前开放时间和之前
//        int todayOfWeek = TimeUtil.getDayOfWeek(curTime);
//        if (ArrayUtil.intArrayContain(openDayArr, todayOfWeek)
//                && lastSettleTime < todayBeginTime) {
//            return todayStamp;
//        }
//
//        int lastSettleDayInWeek = TimeUtil.getDayOfWeek(lastSettleTime);
//        int nextOpenDay = model.foreignInvasion.ForeignInvasionUtil.getNextOpenDay(openDayArr, lastSettleDayInWeek);
//        return TimeUtil.getNextDayInWeekTime(lastSettleTime, nextOpenDay);
//    }
//
//    /**
//     * 初始化玩家的分身
//     *
//     * @return
//     */
//    private List<BossCloneInfo> initPlayerBossClone(String playerIdx) {
//        if (playerIdx == null) {
//            return null;
//        }
//
//        List<BossCloneInfo> builders = randomBossClone(forInvCfg.getBossclonecount());
//        if (builders == null) {
//            return null;
//        }
//
//        bossCloneInfoMap.put(playerIdx, builders);
//        return builders;
//    }
//
//    private synchronized void doReward(boolean bossKilled) {
//        if (rankingPlayerInfo == null || rankingPlayerInfo.size() <= 0) {
//            LogUtil.info("ForeignInvasionManager.doReward, bossDamageRanking is null");
//            return;
//        }
//
//        doRankingReward();
//        doBossReward(bossKilled);
//    }
//
//    /**
//     * 结算排行奖励
//     *
//     * @return
//     */
//    private synchronized void doRankingReward() {
//        //获取当前是第一次开启的第几周
//        long currentTime = GlobalTick.getInstance().getCurrentTime();
//        int weekNum = getWeekNum(currentTime);
//        int dayOfWeek = TimeUtil.getDayOfWeek(currentTime);
//
//        LogUtil.info("ForeignInvasionManager.doReward, do ranking reward, ranking total size:"
//                + rankingPlayerInfo.size() + ",weekNum:" + weekNum + ",day of week:" + dayOfWeek);
//        int rankingMailTemplate = MailTemplateUsed.getById(GameConst.CONFIG_ID).getForinvranking();
//        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ForeignInvasion, "排名");
//
//        for (Entry<String, RankingQuerySingleResult> entry : rankingPlayerInfo.entrySet()) {
//            EventUtil.triggerAddMailEvent(entry.getKey(), rankingMailTemplate,
//                    model.foreignInvasion.ForeignInvasionUtil.getRankingReward(entry.getValue().getRanking(), weekNum, dayOfWeek),
//                    reason, String.valueOf(entry.getValue().getRanking()));
//        }
//    }
//
//    /**
//     * 获取当前开启的时间距离第一次开启时间是第几周
//     *
//     * @return
//     */
//    private int getWeekNum(long currentTime) {
//        long firstOpenTime = this.foreignInvasionInfo.getFirstOpenTime();
//        long toWeekStamp = TimeUtil.getToWeekStamp(firstOpenTime);
//        return (int) Math.ceil((currentTime * 1.0 - toWeekStamp) / TimeUtil.MS_IN_A_WEEK);
//    }
//
//    /**
//     * boss奖励
//     *
//     * @param bossKilled
//     */
//    private synchronized void doBossReward(boolean bossKilled) {
//        if (!bossKilled) {
//            LogUtil.info("ForeignInvasionManager.doBossKillReward, boss is not killed, skip do boos kill rewards");
//            return;
//        }
//        for (String playerIdx : playerInfoMap.keySet()) {
//            EventUtil.triggerAddMailEvent(playerIdx, MailTemplateUsed.getById(GameConst.CONFIG_ID).getForinvbosskilled(),
//                    RewardUtil.parseRewardIntArrayToRewardList(forInvCfg.getBosskillreward()),
//                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ForeignInvasion, "boss击杀"));
//        }
//        LogUtil.info("ForeignInvasionManager.doBossKillReward, do boss kill reward finished");
//    }
//
//    /**
//     * 增加boss击杀状态，清除无用数据
//     */
//    private synchronized void addBossKillStatusAndClearData(boolean bossKilled) {
//        foreignInvasionInfo.addDefeatCondition(bossKilled);
//        foreignInvasionInfo.setLastSettleTime(System.currentTimeMillis());
//        //清除数据
//        foreignInvasionInfo.clearPlayerInfos();
//        foreignInvasionInfo.clearBossDamage();
//        foreignInvasionInfo.clearFirstStageKillCount();
//    }
//
//    @Override
//    public synchronized void onTick() {
//        statusTick();
//        tick();
//    }
//
//    private void statusTick() {
//        ForeignInvasionStatusEnum newStatus;
//
//        long nowTime = GlobalTick.getInstance().getCurrentTime();
//        if (nowTime < startTime) {
//            newStatus = ForeignInvasionStatusEnum.FISE_IdleState;
//        } else if (nowTime < firstStageTime) {
//            newStatus = ForeignInvasionStatusEnum.FISE_Prepare;
//        } else if (nowTime < transitionStageTime) {
//            newStatus = ForeignInvasionStatusEnum.FISE_FirstStage;
//        } else if (nowTime < secondStageTime) {
//            newStatus = ForeignInvasionStatusEnum.FISE_Transition;
//        } else if (nowTime < endTime) {
//            newStatus = ForeignInvasionStatusEnum.FISE_SecondStage;
//        } else {
//            newStatus = ForeignInvasionStatusEnum.FISE_Settle;
//        }
//
//        setStatus(newStatus);
//    }
//
//    private void setStatus(ForeignInvasionStatusEnum status) {
//        if (this.status != status) {
//            sendCurStageStatusAndInfo = false;
//            this.status = status;
//        }
//    }
//
//    private void tick() {
//        long curTime = GlobalTick.getInstance().getCurrentTime();
//        switch (status) {
//            case FISE_IdleState:
//                if (!sendCurStageStatusAndInfo) {
//                    sendMsgToAllAllowPlayer(MsgIdEnum.SC_ForInvStatus, buildForInvStatusMsg());
//                    sendCurStageStatusAndInfo = true;
//                }
//                break;
//            case FISE_Prepare:
//                if (!sendCurStageStatusAndInfo) {
//                    sendMsgToAllAllowPlayer(MsgIdEnum.SC_ForInvStatus, buildForInvStatusMsg());
//                    sendMarqueeToAllowPlayer(forInvCfg.getBeginmaqueeid());
//                    sendCurStageStatusAndInfo = true;
//                }
//                break;
//            case FISE_FirstStage:
//                if (!sendCurStageStatusAndInfo) {
//                    sendAllPlayerMonsterMsg();
//                    sendMsgToAllAllowPlayer(MsgIdEnum.SC_ForInvStatus, buildForInvStatusMsg());
//                    sendCurStageStatusAndInfo = true;
//                }
//
//                //检查是否已经击杀到目标值
//                if (foreignInvasionInfo.getFirstStageKillCount() >= forInvCfg.getFirststagekilltargetcount()) {
//
//                    transitionStageTime = curTime;
//                    secondStageTime = curTime + forInvCfg.getTransitiontime() * TimeUtil.MS_IN_A_MIN;
//                    endTime = secondStageTime + forInvCfg.getSecondstagemaxtime() * TimeUtil.MS_IN_A_MIN;
//                    settleTime = endTime + forInvCfg.getSettledelaytime() * TimeUtil.MS_IN_A_MIN;
//                    nextRefreshRankingTime = secondStageTime + forInvCfg.getRankingrefreashinterval() * TimeUtil.MS_IN_A_S;
//
//                    sendMarqueeToAllowPlayer(forInvCfg.getFirststagefinishtargetstrid());
//                }
//
//                if (curTime > nextSendRemainMonsterCountTime) {
//                    sendMsgToAllAllowPlayer(MsgIdEnum.SC_RemainMonsterCount, buildRemainMonsterMsg());
//                    nextSendRemainMonsterCountTime = curTime + forInvCfg.getRemainmonsterrefreashinterval() * TimeUtil.MS_IN_A_S;
//                }
//                break;
//            case FISE_Transition:
//                if (!sendCurStageStatusAndInfo) {
//                    sendAllPlayerBossInfoMsg();
//                    sendMsgToAllAllowPlayer(MsgIdEnum.SC_ForInvStatus, buildForInvStatusMsg());
//                    sendCurStageStatusAndInfo = true;
//                }
//                break;
//            case FISE_SecondStage:
//                //发送bossInfo以及第二阶段状态
//                if (!sendCurStageStatusAndInfo) {
//                    //检查第一阶段是否是超时完成
//                    if (foreignInvasionInfo.getFirstStageKillCount() < forInvCfg.getFirststagekilltargetcount()) {
//                        sendMarqueeToAllowPlayer(forInvCfg.getFirststagetimeoverstrid());
//                    }
//
//                    sendAllPlayerBossInfoMsg();
//                    sendMsgToAllAllowPlayer(MsgIdEnum.SC_ForInvStatus, buildForInvStatusMsg());
//                    sendCurStageStatusAndInfo = true;
//                }
//
//                //是否需要刷新boss血量
//                if (curTime > nextRefreshBossInfoTime) {
//                    sendRefreshBossBVMsg();
//                    nextRefreshBossInfoTime = curTime + forInvCfg.getBossbvrefreashinterval() * TimeUtil.MS_IN_A_S;
//                }
//
//                refreshRanking(false);
//
//                //boss是否已经击败
//                if (foreignInvasionInfo.getBossDamage() >= foreignInvasionInfo.getBossBloodVolume()) {
//                    foreignInvasionInfo.setBossDamage(foreignInvasionInfo.getBossBloodVolume());
//
//                    endTime = curTime;
//                    settleTime = curTime + forInvCfg.getSettledelaytime() * TimeUtil.MS_IN_A_MIN;
//
//                    sendMarqueeToAllowPlayer(forInvCfg.getBosskilledmarqueeid());
//                }
//                break;
//            case FISE_Settle:
//                if (!sendCurStageStatusAndInfo) {
//                    sendMsgToAllAllowPlayer(MsgIdEnum.SC_ForInvStatus, buildForInvStatusMsg());
//                    refreshRanking(true);
//                    sendCurStageStatusAndInfo = true;
//
//                    //检查第二节点是否是超时结束
//                    if ((foreignInvasionInfo.getBossDamage() < foreignInvasionInfo.getBossBloodVolume())) {
//                        sendMarqueeToAllowPlayer(forInvCfg.getBossunkilledmarqueeid());
//                    }
//                }
//
//                refreshRanking(false);
//
//                if (curTime > settleTime) {
//                    boolean bossKilled = foreignInvasionInfo.getBossDamage() >= foreignInvasionInfo.getBossBloodVolume();
//                    refreshRanking(true);
//                    //发放奖励
//                    doReward(bossKilled);
//                    //更新本次boss击杀状态和清除多余数据
//                    addBossKillStatusAndClearData(bossKilled);
//                    //更新下次配置
//                    refreshForInvConfig();
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
//    public void KillOneMonster() {
//        if (status == ForeignInvasionStatusEnum.FISE_FirstStage) {
//            synchronized (foreignInvasionInfo) {
//                foreignInvasionInfo.setFirstStageKillCount(foreignInvasionInfo.getFirstStageKillCount() + 1);
//            }
//        }
//    }
//
//    /**
//     * @param direct 是否直接刷新，不关心是否达到刷新时间
//     */
//    private void refreshRanking(boolean direct) {
//        long curTime = GlobalTick.getInstance().getCurrentTime();
//        if (GlobalTick.getInstance().getCurrentTime() > nextRefreshRankingTime || direct) {
//            updateRankingInfo();
//            sendDamageRankingMsg();
//            nextRefreshRankingTime = curTime + forInvCfg.getRankingrefreashinterval() * TimeUtil.MS_IN_A_S;
//        }
//    }
//
//    /**
//     * 添加boss血量,且更新排行榜
//     *
//     * @param playerIdx
//     * @param damage
//     * @param bossCloneIdx
//     */
//    public void addBossDamageCount(String playerIdx, long damage, String bossCloneIdx) {
//        if (playerIdx == null || damage <= 0 || bossCloneIdx == null) {
//            return;
//        }
//
//        Builder playerForInvInfo = getPlayerForInvInfo(playerIdx);
//        if (playerForInvInfo == null) {
//            return;
//        }
//
//        //限制最大基础伤害
//        int realDamage = (int) Math.min(Math.min(damage, forInvCfg.getEachbattlemaxdamage()), Integer.MAX_VALUE);
//
//        //boss分身加成
//        ForInvBossCloneCfgObject byId = ForInvBossCloneCfg.getById(getBossCloneCfgId(playerIdx, bossCloneIdx));
//        if (byId != null) {
//            int integralAddition = byId.getIntegraladdition();
//            int firstStageAddition = playerForInvInfo.getDamageAddition();
//            realDamage = (int) Math.min(Integer.MAX_VALUE, (realDamage * 1.0 * (integralAddition + firstStageAddition)) / 1000);
//
//            LogUtil.debug("ForeignInvasionManager.addBossDamageCount, playerIdx:" + playerIdx + ", baseDamage:"
//                    + damage + ", boss addition:" + integralAddition + ", first stage addition：" + firstStageAddition
//                    + ", finally damage:" + realDamage);
//
//        }
//
//        synchronized (playerForInvInfo) {
//            playerForInvInfo.setBossDamage(playerForInvInfo.getBossDamage() + realDamage);
//        }
//
//        if (status == ForeignInvasionStatusEnum.FISE_SecondStage) {
//            synchronized (foreignInvasionInfo) {
//                foreignInvasionInfo.setBossDamage(foreignInvasionInfo.getBossDamage() + realDamage);
//            }
//        }
//
//        updatePlayerRanking(playerIdx);
//        sendBossKilledBarrage(playerIdx, realDamage);
//
//        //目标：外敌入侵累积对boss造成伤害
//        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_Foreign_CumuBossDamage, realDamage, 0);
//    }
//
//    /**
//     * 发送boss击杀弹幕
//     *
//     * @param playerIdx
//     * @param damage
//     */
//    private void sendBossKilledBarrage(String playerIdx, long damage) {
//        //伤害值小于不发放
//        if (playerIdx == null || damage <= forInvCfg.getBossdamagebarragelimit()) {
//            return;
//        }
//
//        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
//        if (allOnlinePlayerIdx == null || allOnlinePlayerIdx.isEmpty()) {
//            return;
//        }
//
//        SC_BossKilledBarrage.Builder builder = SC_BossKilledBarrage.newBuilder();
//        builder.setPlayerIdx(playerIdx);
//        for (String idx : allOnlinePlayerIdx) {
//            if (PlayerUtil.queryPlayerLv(idx) < openLvLimit) {
//                continue;
//            }
//            String msg = ServerStringRes.getContentByLanguage(forInvCfg.getBosskilledbarrageid(),
//                    PlayerUtil.queryPlayerLanguage(idx), PlayerUtil.queryPlayerName(playerIdx), damage);
//            builder.setStr(msg);
//            GlobalData.getInstance().sendMsg(idx, MsgIdEnum.SC_BossKilledBarrage_VALUE, builder);
//        }
//    }
//
//    public ForeignInvasionStatusEnum getStatus() {
//        return status;
//    }
//
//    public void onPlayerLogin(String playerIdx) {
//        if (playerIdx == null || PlayerUtil.queryPlayerLv(playerIdx) < openLvLimit) {
//            return;
//        }
//
//        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ForInvStatus_VALUE, buildForInvStatusMsg());
//        switch (status) {
//            case FISE_IdleState:
//                //doNothing
//                break;
//            case FISE_Prepare:
//                GlobalData.getInstance().sendMarqueeToPlayer(playerIdx, forInvCfg.getBeginmaqueeid());
//                break;
//            case FISE_FirstStage:
//                sendPlayerMonsterMsg(playerIdx);
//                sendKillConditionMsg(playerIdx);
//                GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RemainMonsterCount_VALUE, buildRemainMonsterMsg());
//                break;
//            case FISE_Transition:
//            case FISE_SecondStage:
//                sendBossInfoMsg(playerIdx, true);
//                sendDamageRankingMsg(playerIdx);
//                break;
//            case FISE_Settle:
//                sendBossInfoMsg(playerIdx, false);
//                break;
//            default:
//                break;
//        }
//    }
//
//    private SC_ForInvStatus.Builder buildForInvStatusMsg() {
//        SC_ForInvStatus.Builder builder = SC_ForInvStatus.newBuilder();
//        builder.setStatus(status);
//        if (status == ForeignInvasionStatusEnum.FISE_Prepare) {
//            builder.setCurStatusStartTime(startTime);
//            builder.setCurStatusExpireTime(firstStageTime);
//        } else if (status == ForeignInvasionStatusEnum.FISE_FirstStage) {
//            builder.setCurStatusStartTime(firstStageTime);
//            builder.setCurStatusExpireTime(transitionStageTime);
//        } else if (status == ForeignInvasionStatusEnum.FISE_Transition) {
//            builder.setCurStatusStartTime(transitionStageTime);
//            builder.setCurStatusExpireTime(secondStageTime);
//        } else if (status == ForeignInvasionStatusEnum.FISE_SecondStage) {
//            builder.setCurStatusStartTime(secondStageTime);
//            builder.setCurStatusExpireTime(endTime);
//        } else if (status == ForeignInvasionStatusEnum.FISE_Settle) {
//            builder.setCurStatusStartTime(endTime);
//            builder.setCurStatusExpireTime(settleTime);
//        }
//
//        long currentTime = GlobalTick.getInstance().getCurrentTime();
//        builder.setCurOpenWeekNum(getWeekNum(currentTime));
//        builder.setDayOfWeek(TimeUtil.getDayOfWeek(currentTime));
//        return builder;
//    }
//
//    private SC_RemainMonsterCount.Builder buildRemainMonsterMsg() {
//        SC_RemainMonsterCount.Builder builder = SC_RemainMonsterCount.newBuilder();
//        builder.setAlreadyKilledCount(foreignInvasionInfo.getFirstStageKillCount());
//        builder.setTargetCount(forInvCfg.getFirststagekilltargetcount());
//        return builder;
//    }
//
//    private void sendAllPlayerBossInfoMsg() {
//        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
//        if (allOnlinePlayerIdx != null) {
//            for (String onlinePlayerIdx : allOnlinePlayerIdx) {
//                sendBossInfoMsg(onlinePlayerIdx, true);
//            }
//        }
//    }
//
//    private void sendBossInfoMsg(String playerIdx, boolean containClone) {
//        if (playerIdx == null || PlayerUtil.queryPlayerLv(playerIdx) < openLvLimit) {
//            return;
//        }
//        SC_BossInfo.Builder builder = SC_BossInfo.newBuilder();
//        if (containClone) {
//            List<BossCloneInfo> playerBossCloneInfo = getPlayerBossCloneInfo(playerIdx);
//            if (playerBossCloneInfo != null) {
//                builder.addAllBossClones(playerBossCloneInfo);
//            }
//        }
//
//        builder.setBossFightMakeId(getBossFightMakeId(playerIdx));
//
//        builder.setTotalBloodVolume(foreignInvasionInfo.getBossBloodVolume());
//        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_BossInfo_VALUE, builder);
//    }
//
//
//    public void recreateBossClone(String playerIdx) {
//        if (playerIdx == null) {
//            return;
//        }
//
//        List<BossCloneInfo> bossCloneInfos = randomBossClone(forInvCfg.getBossclonecount());
//        if (bossCloneInfos != null) {
//            List<BossCloneInfo> playerBossCloneInfo = getPlayerBossCloneInfo(playerIdx);
//            if (playerBossCloneInfo != null) {
//                synchronized (playerBossCloneInfo) {
//                    playerBossCloneInfo.clear();
//                    playerBossCloneInfo.addAll(bossCloneInfos);
//                }
//
//                SC_RefreshBossClone.Builder builder = SC_RefreshBossClone.newBuilder();
//                builder.addAllBossClones(bossCloneInfos);
//                GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefreshBossClone_VALUE, builder);
//            }
//        }
//    }
//
//    private void sendAllPlayerMonsterMsg() {
//        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
//        if (allOnlinePlayerIdx != null) {
//            for (String onlinePlayerIdx : allOnlinePlayerIdx) {
//                sendPlayerMonsterMsg(onlinePlayerIdx);
//            }
//        }
//    }
//
//    /**
//     * 发送玩家monster消息
//     *
//     * @param playerIdx
//     */
//    private void sendPlayerMonsterMsg(String playerIdx) {
//        if (playerIdx == null || PlayerUtil.queryPlayerLv(playerIdx) < openLvLimit) {
//            return;
//        }
//
//        Map<String, MonsterInfo> monsterMap = getPlayerMonsterMap(playerIdx);
//        if (monsterMap == null) {
//            return;
//        }
//
//        //发送小怪信息
//        SC_MonsterInfos.Builder builder = SC_MonsterInfos.newBuilder();
//        for (MonsterInfo value : monsterMap.values()) {
//            builder.addMonsters(value);
//        }
//        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_MonsterInfos_VALUE, builder);
////        LogUtil.debug("send playerIdx[" + playerIdx + "] send monsterInfo, monsterInfoSize = " + monsterMap.size() + ", " + builder.toString());
//    }
//
//    private void sendRefreshBossBVMsg() {
//        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
//        if (allOnlinePlayerIdx == null) {
//            return;
//        }
//
//        SC_RefreashBossBV.Builder builder = SC_RefreashBossBV.newBuilder();
//        long remainBV = foreignInvasionInfo.getBossBloodVolume() - foreignInvasionInfo.getBossDamage();
//        if (remainBV < 0) {
//            remainBV = 0;
//        }
//        builder.setRemainBloodVolume(remainBV);
//
//        for (String onlinePlayerIdx : allOnlinePlayerIdx) {
//            if (PlayerUtil.queryPlayerLv(onlinePlayerIdx) < openLvLimit) {
//                return;
//            }
//            GlobalData.getInstance().sendMsg(onlinePlayerIdx, MsgIdEnum.SC_RefreashBossBV_VALUE, builder);
//        }
//    }
//
//    /**
//     * 获取玩家的monsterInfo,不存在是会创建
//     *
//     * @param playerIdx
//     * @return
//     */
//    private Map<String, MonsterInfo> getPlayerMonsterMap(String playerIdx) {
//        if (playerIdx == null) {
//            return null;
//        }
//
//        if (monsterInfoMap.containsKey(playerIdx)) {
//            return monsterInfoMap.get(playerIdx);
//        } else {
//            return initPlayerMonster(playerIdx);
//        }
//    }
//
//    private List<BossCloneInfo> getPlayerBossCloneInfo(String playerIdx) {
//        if (playerIdx == null) {
//            return null;
//        }
//
//        if (bossCloneInfoMap.containsKey(playerIdx)) {
//            return bossCloneInfoMap.get(playerIdx);
//        } else {
//            return initPlayerBossClone(playerIdx);
//        }
//    }
//
//    /**
//     * 发送消息到所有可以参加该玩法的玩家
//     *
//     * @param msgId
//     * @param builder
//     */
//    private void sendMsgToAllAllowPlayer(MsgIdEnum msgId, GeneratedMessageV3.Builder builder) {
//        if (msgId == null || msgId == MsgIdEnum.CS_Null || builder == null) {
//            return;
//        }
//
//        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
//        if (allOnlinePlayerIdx == null || allOnlinePlayerIdx.isEmpty()) {
//            return;
//        }
//
//        for (String onlinePlayerIdx : allOnlinePlayerIdx) {
//            if (PlayerUtil.queryPlayerLv(onlinePlayerIdx) < openLvLimit) {
//                continue;
//            }
//            GlobalData.getInstance().sendMsg(onlinePlayerIdx, msgId.getNumber(), builder);
//        }
//    }
//
//    /**
//     * 更新排行榜数据,主分数伤害+加成伤害，副分数，主伤害
//     *
//     * @param playerIdx
//     */
//    public void updatePlayerRanking(String playerIdx) {
//        if (playerIdx == null) {
//            return;
//        }
//        Builder playerForInvInfo = getPlayerForInvInfo(playerIdx);
//        if (playerForInvInfo == null) {
//            return;
//        }
//
//        RankingUpdateRequest request = new RankingUpdateRequest(RankingName.RN_ForInv_bossDamage);
//        request.addScore(playerIdx, playerForInvInfo.getBossDamage());
//        HttpRequestUtil.asyncUpdateRanking(request);
//    }
//
//    private List<RankingQuerySingleResult> queryRanking() {
//        RankingQueryRequest query = new RankingQueryRequest();
//        query.setRank(RankingName.RN_ForInv_bossDamage);
//        query.setServerIndex(ServerConfig.getInstance().getServer());
//        query.setPage(1);
//        query.setSize(Math.min(model.foreignInvasion.ForeignInvasionUtil.getMaxRanking(), ServerConfig.getInstance().getMaxRankingSize()));
//        HttpRankingResponse result = HttpRequestUtil.queryRanking(query);
//        if (result == null) {
//            LogUtil.error("query forInv ranking result is null");
//            return null;
//        }
//        RankingQueryResult data = result.getData();
//        if (data == null) {
//            LogUtil.error("query forInv ranking data is null");
//            return null;
//        }
//
//        List<RankingQuerySingleResult> pageInfo = data.getPageInfo();
//        if (pageInfo == null) {
//            LogUtil.error("query forInv ranking page data is null, curTime = " + System.currentTimeMillis());
//            return null;
//        }
//        return pageInfo;
//    }
//
//    /**
//     * 获取需要展示的玩家数量
//     *
//     * @return
//     */
//    private int getDisRankingSize() {
//        if (getStatus() == ForeignInvasionStatusEnum.FISE_SecondStage) {
//            return forInvCfg.getSecondrankingcount();
//        } else {
//            return forInvCfg.getSettlerankingcount();
//        }
//    }
//
//    /**
//     * 刷新排行榜
//     */
//    public void updateRankingInfo() {
//        List<RankingQuerySingleResult> results = queryRanking();
//        if (CollectionUtils.isEmpty(results)) {
//            return;
//        }
//
//        //清空排行榜相关信息
//        rankingPlayerInfo.clear();
//        rankingInfo.clear();
//
//        for (RankingQuerySingleResult result : results) {
//            rankingPlayerInfo.put(result.getPrimaryKey(), result);
//
//            if (result.getRanking() <= getDisRankingSize()) {
//                RankingPlayerInfo.Builder builder = RankingPlayerInfo.newBuilder();
//                builder.setRankingIndex(result.getRanking());
//                builder.setTotalDamage(result.getIntPrimaryScore());
//                builder.setPlayerName(PlayerUtil.queryPlayerName(result.getPrimaryKey()));
//                rankingInfo.add(builder.build());
//            }
//        }
//        LogUtil.info("query forInv  bossRanking");
//    }
//
//    public void sendDamageRankingMsg() {
//        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
//        if (allOnlinePlayerIdx == null) {
//            return;
//        }
//        if (rankingInfo.isEmpty()) {
//            LogUtil.info("Ranking info is null, skip send this ranking msg");
//            return;
//        }
//        SC_RankingInfo.Builder rankingResult = SC_RankingInfo.newBuilder();
//        rankingResult.addAllOtherPlayerInfos(rankingInfo);
//
//        for (String onlinePlayerIdx : allOnlinePlayerIdx) {
//            if (PlayerUtil.queryPlayerLv(onlinePlayerIdx) < openLvLimit) {
//                continue;
//            }
//
//            //与玩家相关
//            RankingQuerySingleResult result = rankingPlayerInfo.get(onlinePlayerIdx);
//            if (result != null) {
//                rankingResult.setBaseDamage(result.getIntPrimaryScore());
//                rankingResult.setRankingNum(result.getRanking());
////                rankingResult.setAddtionDamage(result.getIntPrimaryScore() - result.getSubsidiaryScore());
//            } else {
//                rankingResult.clearRankingNum();
//                rankingResult.clearAddtionDamage();
//                rankingResult.clearBaseDamage();
//            }
//
//            GlobalData.getInstance().sendMsg(onlinePlayerIdx, MsgIdEnum.SC_RankingInfo_VALUE, rankingResult);
//        }
//    }
//
//    public void sendDamageRankingMsg(String playerIdx) {
//        if (playerIdx == null || rankingInfo.isEmpty()) {
//            LogUtil.info("Ranking info is null, skip send this ranking msg");
//            return;
//        }
//        //与玩家相关
//        RankingQuerySingleResult result = rankingPlayerInfo.get(playerIdx);
//
//        if (result == null) {
//            return;
//        }
//
//        SC_RankingInfo.Builder rankingResult = SC_RankingInfo.newBuilder();
//        rankingResult.addAllOtherPlayerInfos(rankingInfo);
//
//        rankingResult.setBaseDamage(result.getSubsidiaryScore());
//        rankingResult.setRankingNum(result.getRanking());
//        rankingResult.setAddtionDamage(result.getIntPrimaryScore() - result.getSubsidiaryScore());
//
//        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RankingInfo_VALUE, rankingResult);
//    }
//
//    public void clearRanking() {
//     /*   if (!HttpRequestUtil.clearRanking(RankingName.RN_ForInv_bossDamage, ServerConfig.getInstance().getServer())) {
//            LogUtil.error("clear forInv bossDamage ranking error");
//        }*/
//        HttpRequestUtil.asyncClearRanking(RankingName.RN_ForInv_bossDamage, ServerConfig.getInstance().getServer());
//    }
//
//    /**
//     * 发送跑马灯到所有的可参与玩家玩家
//     *
//     * @param marqueeId
//     */
//    public void sendMarqueeToAllowPlayer(int marqueeId) {
//        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
//        if (allOnlinePlayerIdx == null || allOnlinePlayerIdx.isEmpty()) {
//            return;
//        }
//
//        for (String onlinePlayerIdx : allOnlinePlayerIdx) {
//            if (PlayerUtil.queryPlayerLv(onlinePlayerIdx) >= openLvLimit) {
//                GlobalData.getInstance().sendMarqueeToPlayer(onlinePlayerIdx, marqueeId);
//            }
//        }
//    }
//
//    private DB_ForInvPlayerInfo.Builder getPlayerForInvInfo(String playerIdx) {
//        if (playerIdx == null || PlayerUtil.queryPlayerLv(playerIdx) < openLvLimit) {
//            return null;
//        }
//        if (playerInfoMap.containsKey(playerIdx)) {
//            return playerInfoMap.get(playerIdx);
//        } else {
//            DB_ForInvPlayerInfo.Builder builder = DB_ForInvPlayerInfo.newBuilder();
//            builder.setPlayerIdx(playerIdx);
//            playerInfoMap.put(playerIdx, builder);
//
//            //初始化玩家难度
//            EventUtil.recreateMonsterDiff(playerIdx, EnumFunction.ForeignInvasion);
//            return builder;
//        }
//    }
//
//    public int getPlayerTransitionRewardCount(String playerIdx) {
//        Builder playerForInvInfo = getPlayerForInvInfo(playerIdx);
//        if (playerForInvInfo == null) {
//            return 0;
//        } else {
//            return playerForInvInfo.getTransitionRewardCount();
//        }
//    }
//
//    public synchronized void increaseClaimRewardCount(String playerIdx) {
//        Builder playerForInvInfo = getPlayerForInvInfo(playerIdx);
//        if (playerForInvInfo != null) {
//            playerForInvInfo.setTransitionRewardCount(playerForInvInfo.getTransitionRewardCount() + 1);
//        }
//    }
//
//    /**
//     * 移除指定玩家的指定Idx宠物
//     *
//     * @param playerIdx
//     * @param removeIdxList
//     * @return 移除成功的Idx
//     */
//    public List<String> removeMonster(String playerIdx, List<String> removeIdxList) {
//        if (playerIdx == null || removeIdxList == null || removeIdxList.isEmpty()) {
//            return null;
//        }
//
//        Map<String, MonsterInfo> playerMonsterMap = getPlayerMonsterMap(playerIdx);
//        if (playerMonsterMap == null) {
//            return null;
//        }
//
//        long currentTime = GlobalTick.getInstance().getCurrentTime();
//        List<String> successRemove = new ArrayList<>();
//        synchronized (playerMonsterMap) {
//            for (String idx : removeIdxList) {
//                if (playerMonsterMap.containsKey(idx)) {
//                    MonsterInfo monsterInfo = playerMonsterMap.get(idx);
//                    //小于最小移除时间
//                    if (currentTime - monsterInfo.getCreateTime() < minRecreateTime) {
//                        continue;
//                    }
//                    playerMonsterMap.remove(idx);
//                    successRemove.add(idx);
//                }
//            }
//        }
//        return successRemove;
//    }
//
//    /**
//     * 创建小怪并添加到玩家的小怪信息队列中
//     *
//     * @param playerIdx
//     * @param createSize
//     * @return 返回生成的小怪
//     */
//    public Map<String, MonsterInfo> createMonster(String playerIdx, int createSize) {
//        if (playerIdx == null || createSize <= 0 || createSize > forInvCfg.getFirststagemonstercount()) {
//            LogUtil.info("createMonster, error params");
//            return null;
//        }
//
//        Map<String, MonsterInfo> newMonsterMap = randomMonster(createSize, playerIdx);
//        if (newMonsterMap == null || newMonsterMap.isEmpty()) {
//            LogUtil.error("create monster failed");
//            return null;
//        }
//
//        Map<String, MonsterInfo> playerMonsterMap = getPlayerMonsterMap(playerIdx);
//        synchronized (playerMonsterMap) {
//            playerMonsterMap.putAll(newMonsterMap);
//        }
//
//        return newMonsterMap;
//    }
//
//    private Map<String, MonsterInfo> randomMonster(int needCount, String playerIdx) {
//        if (needCount <= 0 || needCount > forInvCfg.getFirststagemonstercount()) {
//            return null;
//        }
//        MonsterDifficultyObject diffCfg = MonsterDifficulty.getByPlayerIdx(playerIdx);
//        if (diffCfg == null) {
//            return null;
//        }
//        //{fightMake，monsterType，boss伤害加成（千分比），出现概率}
//        //monsterType:1为普通,2为小怪
//        int[][] lineup = diffCfg.getForeigninvasionmonstercfg();
//        if (lineup == null || lineup.length <= 0) {
//            return null;
//        }
//
//        int totalOdds = 0;
//        for (int[] ints : lineup) {
//            if (ints.length == 4) {
//                totalOdds += ints[3];
//            }
//        }
//
//        long currentTime = GlobalTick.getInstance().getCurrentTime();
//        Map<String, MonsterInfo> resultMap = new HashMap<>();
//        Random random = new Random();
//        for (int i = 0; i < needCount; i++) {
//            int[] result = null;
//            if (totalOdds <= 0) {
//                result = lineup[random.nextInt(lineup.length)];
//            } else {
//                int curRate = 0;
//                int num = random.nextInt(totalOdds);
//                for (int[] ints : lineup) {
//                    if ((curRate += ints[3]) >= num) {
//                        result = ints;
//                        break;
//                    }
//                }
//            }
//
//            if (result != null && result.length == 4) {
//                MonsterInfo.Builder builder = MonsterInfo.newBuilder();
//                builder.setMonsterIdx(IdGenerator.getInstance().generateId());
//                builder.setFightMakeId(result[0]);
//                builder.setMonsterType(result[1]);
//                builder.setAddtion(result[2]);
//                builder.setName(getRandomName());
//                builder.setCreateTime(currentTime);
//                resultMap.put(builder.getMonsterIdx(), builder.build());
//            }
//        }
//
//        if (needCount != resultMap.size()) {
//            LogUtil.error("======================recreate monster failed================================");
//        }
//
//        return resultMap;
//    }
//
//    private List<BossCloneInfo> randomBossClone(int needCount) {
//        if (needCount <= 0 || needCount > forInvCfg.getBossclonecount()) {
//            return null;
//        }
//
//        Map<Integer, ForInvBossCloneCfgObject> ix_id = ForInvBossCloneCfg._ix_id;
//        List<BossCloneInfo> resultList = new ArrayList<>();
//
//        Random random = new Random();
//        int curRate = 0;
//        for (int i = 0; i < needCount; i++) {
//            int num = random.nextInt(bossCloneTotalRate);
//            for (ForInvBossCloneCfgObject value : ix_id.values()) {
//                if ((curRate += value.getAppearrate()) >= num) {
//                    BossCloneInfo.Builder builder = BossCloneInfo.newBuilder();
//                    builder.setCloneIdx(IdGenerator.getInstance().generateId());
//                    builder.setCloneCfgId(value.getId());
//                    resultList.add(builder.build());
//                    break;
//                }
//            }
//        }
//
//        if (needCount != resultList.size()) {
//            LogUtil.error("======================recreate bossClone failed================================");
//        }
//        return resultList;
//    }
//
//    @Override
//    public synchronized void update() {
//        if (foreignInvasionInfo == null) {
//            return;
//        }
//
//        gameplayEntity foreignInvasion = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.ATE_ForeignInvasion);
//        if (foreignInvasion == null) {
//            foreignInvasion = new gameplayEntity();
//        }
//        foreignInvasionInfo.clearPlayerInfos();
//        for (Builder value : playerInfoMap.values()) {
//            foreignInvasionInfo.addPlayerInfos(value.build());
//        }
//        foreignInvasion.setGameplayinfo(foreignInvasionInfo.build().toByteArray());
//        foreignInvasion.putToCache();
//    }
//
//    public MonsterInfo getMonsterInfo(String playerIdx, String monsterIdx) {
//        if (playerIdx == null || monsterIdx == null) {
//            return null;
//        }
//        Map<String, MonsterInfo> playerMonsterMap = getPlayerMonsterMap(playerIdx);
//        return playerMonsterMap.get(monsterIdx);
//    }
//
//    public int getBossCloneCfgId(String playerIdx, String booCloneIdx) {
//        if (playerIdx == null || booCloneIdx == null) {
//            return 0;
//        }
//
//        List<BossCloneInfo> playerBossCloneInfo = getPlayerBossCloneInfo(playerIdx);
//        if (playerBossCloneInfo == null) {
//            return 0;
//        } else {
//            for (BossCloneInfo bossCloneInfo : playerBossCloneInfo) {
//                if (booCloneIdx.equalsIgnoreCase(bossCloneInfo.getCloneIdx())) {
//                    return bossCloneInfo.getCloneCfgId();
//                }
//            }
//        }
//        return 0;
//    }
//
//    /**
//     * 计算小怪积分加成,添加玩家小怪击杀情况, 并发放奖励
//     *
//     * @param playerIdx
//     */
//    public synchronized void addIntegralAddition(String playerIdx) {
//        DB_ForInvPlayerInfo.Builder playerForInvInfo = getPlayerForInvInfo(playerIdx);
//        if (playerForInvInfo == null) {
//            return;
//        }
//
//        synchronized (playerForInvInfo) {
//            MonsterInfo curBattleMonster = playerForInvInfo.getCurBattleMonster();
//            if (curBattleMonster == null) {
//                return;
//            }
//            int monsterType = curBattleMonster.getMonsterType();
//            if (monsterType == 1) {
//                playerForInvInfo.setCommon(playerForInvInfo.getCommon() + 1);
//            } else if (monsterType == 2) {
//                playerForInvInfo.setElite(playerForInvInfo.getElite() + 1);
//            }
//
//            playerForInvInfo.setDamageAddition(playerForInvInfo.getDamageAddition() + curBattleMonster.getAddtion());
//            playerForInvInfo.clearCurBattleMonster();
//            sendKillConditionMsg(playerIdx);
//        }
//    }
//
//
//    /**
//     * 更新玩家的小怪击杀情况
//     *
//     * @param playerIdx
//     */
//    private void sendKillConditionMsg(String playerIdx) {
//        Builder playerForInvInfo = getPlayerForInvInfo(playerIdx);
//        //发送击杀情况
//        SC_MonsterKillCondition.Builder resultBuilder = SC_MonsterKillCondition.newBuilder();
//        resultBuilder.setCommon(playerForInvInfo.getCommon());
//        resultBuilder.setElite(playerForInvInfo.getElite());
//        resultBuilder.setAddition(playerForInvInfo.getDamageAddition());
//        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_MonsterKillCondition_VALUE, resultBuilder);
//    }
//
//    /**
//     * 标记玩家战斗的小怪
//     *
//     * @param playerIdx
//     * @param monster
//     * @return
//     */
//    public synchronized boolean markMonsterInBattle(String playerIdx, MonsterInfo monster) {
//        if (playerIdx == null || monster == null) {
//            return false;
//        }
//        Builder playerForInvInfo = getPlayerForInvInfo(playerIdx);
//        if (playerForInvInfo != null) {
//            playerForInvInfo.setCurBattleMonster(monster);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 获取玩家第一阶段当前正在战斗的monster
//     *
//     * @param playerIdx
//     * @return
//     */
//    public MonsterInfo getPlayerCurBattleMonster(String playerIdx) {
//        Builder playerForInvInfo = getPlayerForInvInfo(playerIdx);
//        if (playerForInvInfo != null) {
//            return playerForInvInfo.getCurBattleMonster();
//        }
//        return null;
//    }
//
//    public String getRandomName() {
//        if (randomNameList.isEmpty()) {
//            Collection allPlayerName = playerCache.getInstance().getAllPlayerName();
//            if (allPlayerName != null) {
//                synchronized (randomNameList) {
//                    randomNameList.addAll(allPlayerName);
//                }
//            }
//        }
//
//        if (curIndex.get() >= randomNameList.size()) {
//            curIndex.set(0);
//        }
//
//        return randomNameList.get(curIndex.getAndIncrement());
//    }
//
//    public int getOpenLvLimit() {
//        return openLvLimit;
//    }
//
//    public int getBossFightMakeId(String playerIdx) {
//        Integer integer = bossFightMake.get(playerIdx);
//        if (integer == null) {
//            MonsterDifficultyObject diffCfg = MonsterDifficulty.getByPlayerIdx(playerIdx);
//            if (diffCfg == null) {
//                LogUtil.error("can not find player monster diff cfg: playerIdx: " + playerIdx);
//                return 0;
//            }
//            integer = diffCfg.getForeigninvasionbosscfg();
//            bossFightMake.put(playerIdx, integer);
//        }
//        return integer;
//    }
//
//    public void updateActivity() {
//        ClientActivity.Builder builder = ClientActivity.newBuilder();
//        builder.setActivityType(ActivityTypeEnum.ATE_ForInv);
//        ActivityTime.Builder timeBuilder = ActivityTime.newBuilder();
//
//        Cycle_Week.Builder cycle = Cycle_Week.newBuilder();
//        for (int dayOfWeek : this.forInvCfg.getOpenday()) {
//            cycle.addDayOfWeekList(dayOfWeek);
//        }
//        cycle.setStartOfDay(TimeUtil.getMin(this.firstStageTime));
//        cycle.setEndOfDay(TimeUtil.getMin(this.settleTime));
//
//        timeBuilder.setTimeType(CycleTypeEnum.CTE_Week);
//        timeBuilder.setTimeContent(cycle.build().toByteString());
//        builder.setCycleTime(timeBuilder);
//        builder.setDetail(ServerStringRes.buildLanguageNumContentJson(forInvCfg.getHelp()));
//        ActivityManager.getInstance().addSpecialActivity(builder.build());
//    }
//}
