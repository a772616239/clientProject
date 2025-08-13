package model.gloryroad;

import cfg.GloryRoadConfig;
import cfg.GloryRoadConfigObject;
import cfg.GloryRoadStageConfig;
import cfg.GloryRoadStageConfigObject;
import cfg.RankRewardTargetConfig;
import cfg.ServerStringRes;
import common.GameConst;
import common.GlobalData;
import common.HttpRequestUtil;
import common.SyncExecuteFunction;
import common.entity.RankingQuerySingleResult;
import common.entity.RankingUpdateRequest;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.gloryroad.GloryRoadConst.BattleType;
import model.gloryroad.dbCache.gloryroadCache;
import model.gloryroad.entity.gloryroadEntity;
import model.player.dbCache.playerCache;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.ranking.settle.GloryRoadRankingSettleHandler;
import model.ranking.settle.MailRankingSettleHandler;
import model.recentpassed.RecentPassedUtil;
import model.team.dbCache.teamCache;
import model.warpServer.crossServer.CrossServerManager;
import model.wordFilter.WordFilterManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.EnumRankingType;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.SC_Tips;
import protocol.GameplayDB.DB_GloryLoad;
import protocol.GameplayDB.DB_GloryRoadSchedule;
import protocol.GameplayDB.EnumGloryRoadOperateType;
import protocol.GameplayDB.GameplayTypeEnum;
import protocol.GameplayDB.GloryRoadTreeContent;
import protocol.GloryRoad.EnumGloryRoadSchedule;
import protocol.GloryRoad.GloryRoadComment;
import protocol.GloryRoad.GloryRoadQuizInfo;
import protocol.GloryRoad.NodeMessage;
import protocol.GloryRoad.NodePlayerInfo;
import protocol.GloryRoad.SC_ClaimGloryRoadBattleTree;
import protocol.GloryRoad.SC_ClaimGloryRoadInfo;
import protocol.GloryRoad.SC_ClaimGloryRoadNodeTeamRecord;
import protocol.GloryRoad.SC_GloryRoadAddComment;
import protocol.GloryRoad.SC_GloryRoadJoinNotice;
import protocol.GloryRoad.SC_GloryRoadPromotionNotice;
import protocol.GloryRoad.SC_RefreshBattleSchedule;
import protocol.MessageId.MsgIdEnum;
import protocol.PrepareWar.TeamTypeEnum;
import protocol.RecentPassedOuterClass.RecentPassed;
import protocol.RetCodeId.RetCodeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.ObjUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2021/3/11
 */
public class GloryRoadManager implements Tickable, GamePlayerUpdate {
    private static GloryRoadManager instance;

    public static GloryRoadManager getInstance() {
        if (instance == null) {
            synchronized (GloryRoadManager.class) {
                if (instance == null) {
                    instance = new GloryRoadManager();
                }
            }
        }
        return instance;
    }

    private GloryRoadManager() {
    }

    private GloryRoadConfigObject config;

    /**
     * 赛程配置
     */
    private List<GloryRoadSchedule> scheduleList;
    private AtomicInteger curScheduleIndex;

    private Set<String> joinPlayerSet;
    /**
     * 战斗树结构, 需要保存到下一个活动开始后才清空
     */
    private GloryRoadCompleteBinaryTree battleTree;

    private RedBag redBag;

    private GloryRoadQuiz quiz;

    /**
     * 战斗管理Map  <parentIndex,>
     */
    private Map<Integer, GloryRoadBattle> indexBattleMap;
    /**
     * <playerIdx,parentIndex>
     */
    private Map<String, Integer> playerIndexMap;

    private long pushAdvanceTime;

    /**
     * 需要推送
     */
    private Set<EnumGloryRoadSchedule> needPushSchedule;

    /**
     * 评论List
     */
    private List<GloryRoadComment> commentList;
    private Map<String, Long> lastCommentTime;

    /**
     * 玩法信息存储
     */
    private DB_GloryLoad.Builder dbGloryLoadBuilder;

    public boolean init() {
        if (!checkConfig()) {
            return false;
        }
        this.scheduleList = Collections.synchronizedList(new ArrayList<>());
        this.curScheduleIndex = new AtomicInteger();
        this.battleTree = new GloryRoadCompleteBinaryTree(this.config.getJoinplayercount());
        this.joinPlayerSet = new ConcurrentSet<>();
        this.indexBattleMap = new ConcurrentHashMap<>();
        this.playerIndexMap = new ConcurrentHashMap<>();
        this.needPushSchedule = new ConcurrentSet<>();
        this.commentList = Collections.synchronizedList(new ArrayList<>());
        this.lastCommentTime = new ConcurrentHashMap<>();

//        gloryroadCache.getInstance().clear();
        loadGloryLoadBuilder();

        boolean initResult = revertFromDB() || initTime();
        return initResult
                && GlobalTick.getInstance().addTick(this)
                && gameplayCache.getInstance().addToUpdateSet(this);

    }

    private boolean checkConfig() {
        this.config = GloryRoadConfig.getById(GameConst.CONFIG_ID);
        int totalRounds = (int) Math.ceil(Math.log(config.getJoinplayercount()) / Math.log(2));
        if (totalRounds != config.getBattleopenday().length) {
//            LogUtil.error("GloryRoadManager.checkConfig, battle day config length is not equal battle totalRounds");
            return false;
        }

        for (int[] openCfg : this.config.getBattleopenday()) {
            if (openCfg.length < 2) {
//                LogUtil.error("GloryRoadManager.checkConfig, battle cfg length is less than 2");
                return false;
            }
        }

        this.pushAdvanceTime = this.config.getPushadvancetime() * TimeUtil.MS_IN_A_MIN;
        return true;
    }

    private boolean initTime() {
        clear();

        //第一个阶段
        long nextOpenTimeStamp = getNextOpenTimeStamp();
        LogUtil.info("model.gloryroad.GloryRoadManager.initTime, next open time stamp:" + nextOpenTimeStamp);
        long nextOpenTime = nextOpenTimeStamp + this.config.getOpentime() * TimeUtil.MS_IN_A_MIN;
        long firstBattleStartTime = nextOpenTime + this.config.getFirstbattletimeoffset() * TimeUtil.MS_IN_A_MIN;
        GloryRoadSchedule firstSchedule
                = new GloryRoadSchedule(EnumGloryRoadSchedule.EGRS_NUll, nextOpenTime, firstBattleStartTime);
        firstSchedule.addOperate(EnumGloryRoadOperateType.EGROT_CLEAR);
        firstSchedule.addOperate(EnumGloryRoadOperateType.EGROT_SELECTED_PLAYER);
        firstSchedule.addOperate(EnumGloryRoadOperateType.EGROT_OPEN_QUIZ);
        if (!addSchedule(firstSchedule)) {
            return false;
        }

        int totalLength = config.getBattleopenday().length;
        for (int i = 0; i < totalLength; i++) {
            //添加战斗阶段
            int[] scheduleConfig = config.getBattleopenday()[i];

            int battleType = scheduleConfig[1];
            EnumGloryRoadSchedule enumSchedule = EnumGloryRoadSchedule.forNumber(totalLength - i);
            long lastScheduleStartTime = getLastScheduleEndTime();
            GloryRoadSchedule battleSchedule
                    = new GloryRoadSchedule(enumSchedule, lastScheduleStartTime, lastScheduleStartTime + getDurationTimeByBattleType(battleType));
            battleSchedule.addOperate(battleType == 0 ? EnumGloryRoadOperateType.EGROT_BATTLE_AUTO : EnumGloryRoadOperateType.EGROT_BATTLE_MANUAL);
            if (!addSchedule(battleSchedule)) {
                return false;
            }

            //添加空闲阶段
            GloryRoadSchedule idleSchedule = new GloryRoadSchedule();
            idleSchedule.setSchedule(EnumGloryRoadSchedule.EGRS_NUll);
            idleSchedule.setStartTime(getLastScheduleEndTime());

            boolean isLastBattleInDay = i >= totalLength - 1 || scheduleConfig[0] != config.getBattleopenday()[i + 1][0];
            if (isLastBattleInDay) {
                idleSchedule.addOperate(EnumGloryRoadOperateType.EGROT_RED_BAG);
                //最后一天最后一场
                if (i >= totalLength - 1) {
                    idleSchedule.setEndTime(idleSchedule.getStartTime() + this.config.getBattleintervalinoneday() * TimeUtil.MS_IN_A_MIN);
                    idleSchedule.addOperate(EnumGloryRoadOperateType.EGROT_SETTLE_RANKING);
                    idleSchedule.addOperate(EnumGloryRoadOperateType.EGROT_OPEN_NEW);
                } else {
                    idleSchedule.setEndTime(firstBattleStartTime + TimeUtil.MS_IN_A_DAY * scheduleConfig[0]);
                    idleSchedule.addOperate(EnumGloryRoadOperateType.EGROT_OPEN_QUIZ);
                }
            } else {
                idleSchedule.setEndTime(idleSchedule.getStartTime() + this.config.getBattleintervalinoneday() * TimeUtil.MS_IN_A_MIN);
            }
            if (!addSchedule(idleSchedule)) {
                return false;
            }

            boolean isFirstBattleInOneDay = i == 0 || scheduleConfig[0] != config.getBattleopenday()[i - 1][0];
            if (isFirstBattleInOneDay) {
                this.needPushSchedule.add(enumSchedule);
            }
        }

        return checkScheduleTime();
    }

    private boolean checkScheduleTime() {
        long lastEndTime = 0;
        for (GloryRoadSchedule schedule : this.scheduleList) {
//            LogUtil.debug("GloryRoadManager.checkScheduleTime, schedule:" + schedule);
            if (lastEndTime == 0) {
                lastEndTime = schedule.getEndTime();
                continue;
            }

            if (schedule.getStartTime() != lastEndTime) {
//                LogUtil.error("GloryRoadManager.checkScheduleTime, schedule start time is not equal last schedule end time");
                return false;
            } else {
                lastEndTime = schedule.getEndTime();
            }
        }
        return true;
    }

    private long getDurationTimeByBattleType(int battleType) {
        if (battleType == BattleType.AUTO) {
            return this.config.getAutobattletime() * TimeUtil.MS_IN_A_MIN;
        } else if (battleType == BattleType.MANUAL) {
            return this.config.getManualbattletime() * TimeUtil.MS_IN_A_MIN;
        }
//        LogUtil.error("GloryRoadManager.getDurationTimeByBattleType, unsupported battle type:" + battleType);
        return -1;
    }

    private void clear() {
//        this.joinPlayerSet.clear();
        this.scheduleList.clear();
        this.curScheduleIndex.set(-1);
//        this.battleTree.clear();
        this.quiz = null;
//        this.redBag = null;
        this.playerIndexMap.clear();
        this.indexBattleMap.clear();
        this.needPushSchedule.clear();
    }

    private boolean addSchedule(GloryRoadSchedule schedule) {
        if (schedule == null) {
//            LogUtil.error("GloryRoadManager.addSchedule, param is null");
            return false;
        }
        this.scheduleList.add(schedule);
//        LogUtil.info("GloryRoadManager.addSchedule, success add schedule:" + schedule);
        return true;
    }

    /**
     * 获取最后一个阶段的结束时间
     *
     * @return
     */
    private long getLastScheduleEndTime() {
        if (CollectionUtils.isEmpty(scheduleList)) {
            return getNextOpenTimeStamp();
        }
        GloryRoadSchedule lastSchedule = this.scheduleList.get(this.scheduleList.size() - 1);
        return lastSchedule == null ? -1 : lastSchedule.getEndTime();
    }

    private long getNextOpenTimeStamp() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();

        if (this.dbGloryLoadBuilder.getLastSettleTime() == 0) {
            this.dbGloryLoadBuilder.setLastSettleTime(currentTime + this.config.getOpenoffsetday() * TimeUtil.MS_IN_A_DAY);
        }

        long lastSettleTime = this.dbGloryLoadBuilder.getLastSettleTime();
        LogUtil.info("model.gloryroad.GloryRoadManager.getNextOpenTimeStamp, last settle time:" + lastSettleTime);
        int lastSettleTimeDayInWeek = TimeUtil.getDayOfWeek(lastSettleTime);
        if (lastSettleTimeDayInWeek == this.config.getOpendayofweek()) {
            long lastSettleDayStamp = TimeUtil.getTodayStamp(lastSettleTime);
            long firstBattleTime = lastSettleDayStamp + this.config.getFirstbattletimeoffset() * TimeUtil.MS_IN_A_MIN;

            if (firstBattleTime > lastSettleTime) {
                return lastSettleDayStamp;
            }
        }

        return TimeUtil.getNextDayInWeekTime(this.dbGloryLoadBuilder.getLastSettleTime(), this.config.getOpendayofweek());
    }

    /**
     * 选择玩家
     * 先选择活跃的玩家 然后从非活跃玩家中挑选以补充人数
     * <p>
     * 分组时按照实际排行分组  前八位分别在一组内
     *
     * @return
     */
    private synchronized boolean selectedPlayer() {
        List<RankingQuerySingleResult> totalInfo = RankingManager.getInstance().getRankingTotalInfo(EnumRankingType.ERT_ArenaScoreLocal);
        if (CollectionUtils.isEmpty(totalInfo)) {
//            LogUtil.warn("GloryRoadManager.selectedPlayer, query ranking failed, ranking is empty, skip open current activity");
            this.dbGloryLoadBuilder.setLastSettleTime(getLastScheduleEndTime());
            initTime();
//            LogUtil.info("GloryRoadManager.selectedPlayer, player is empty, init next open time finished, openTime:" + getOpenTime());
            return false;
        }
        totalInfo.sort(Comparator.comparingInt(RankingQuerySingleResult::getRanking));

        List<RankingQuerySingleResult> findResults = new ArrayList<>();
        List<RankingQuerySingleResult> inactivePlayer = new ArrayList<>();

        for (RankingQuerySingleResult result : totalInfo) {

            if (PlayerUtil.queryFunctionLock(result.getPrimaryKey(), EnumFunction.EF_GloryRoad)) {
                continue;
            }

            //当前在线直接添加
            if (GlobalData.getInstance().checkPlayerOnline(result.getPrimaryKey())) {
                findResults.add(result);
                continue;
            }

            int offlineDays = PlayerUtil.getPlayerOffLineDays(result.getPrimaryKey());
//            LogUtil.debug("GloryRoadManager.selectedPlayer, playerIdx:"
//                    + result.getPrimaryKey() + ", off line days:" + offlineDays);
            if (offlineDays != -1 && offlineDays <= this.config.getInactiveplayerofflinedays()) {
                findResults.add(result);
            } else {
                inactivePlayer.add(result);
            }

            if (findResults.size() >= this.config.getJoinplayercount()) {
                break;
            }
        }

        //补齐人数
        if (findResults.size() < this.config.getJoinplayercount()
                && !inactivePlayer.isEmpty()) {
            int remainPosition = this.config.getJoinplayercount() - findResults.size();
            findResults.addAll(inactivePlayer.subList(0, Math.min(inactivePlayer.size(), remainPosition)));
        }

        if (findResults.isEmpty()) {
//            LogUtil.warn("GloryRoadManager.selectedPlayer, query ranking failed, ranking is empty, skip open current activity");
            this.dbGloryLoadBuilder.setLastSettleTime(getLastScheduleEndTime());
            initTime();
//            LogUtil.info("GloryRoadManager.selectedPlayer, player is empty, init next open time finished, openTime:" + getOpenTime());
            return true;
        }

        //按照排名排序
        findResults.sort(Comparator.comparingInt(RankingQuerySingleResult::getRanking));

        List<GloryRoadGroup<RankingQuerySingleResult>> groups = GloryRoadUtil.splitGroup(findResults);
        if (CollectionUtils.isEmpty(groups)) {
//            LogUtil.error("GloryRoadManager.selectedPlayer, playerIdx, split group failed");
            return false;
        }
        return putGroupsToTree(groups);
    }

    private void sendJoinNotice() {
        if (CollectionUtils.isEmpty(this.joinPlayerSet)) {
            return;
        }

        SC_GloryRoadJoinNotice.Builder noticeBuilder = SC_GloryRoadJoinNotice.newBuilder();
        noticeBuilder.setStartTime(getFirstBattleTime());
        for (String playerIdx : this.joinPlayerSet) {
            if (GlobalData.getInstance().checkPlayerOnline(playerIdx)) {
                GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_GloryRoadJoinNotice_VALUE, noticeBuilder);
            } else {
                EventUtil.triggerAddMailEvent(playerIdx, GloryRoadConfig.getById(GameConst.CONFIG_ID).getJoinmailtemplate(),
                        null, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GloryRoad));
            }
        }
    }

    public boolean putGroupsToTree(List<GloryRoadGroup<RankingQuerySingleResult>> roadGroups) {
        if (CollectionUtils.isEmpty(roadGroups)) {
            return false;
        }
        if (!this.battleTree.isEmpty()) {
//            LogUtil.error("GloryRoadManager.putGroupsToTree, battle tree is not empty");
            return false;
        }

        int eachGroupSize = this.config.getJoinplayercount() / this.config.getGroupsize();
        for (int i = 0; i < roadGroups.size(); i++) {
            GloryRoadGroup<RankingQuerySingleResult> roadGroup = roadGroups.get(i);
            for (Entry<Integer, RankingQuerySingleResult> entry : roadGroup.getIndexMap().entrySet()) {
                //添加到玩家数据
                String playerIdx = entry.getValue().getPrimaryKey();
                this.joinPlayerSet.add(playerIdx);

                //添加到战斗树
                int place = i * eachGroupSize + entry.getKey();
                if (!this.battleTree.setBottomPlayer(place, playerIdx)) {
                    return false;
                }

                //设置入选排名
                setJoinRank(playerIdx, entry.getValue().getRanking());
            }
        }

        sendJoinNotice();
        refreshBattleTree();
        return true;
    }

    private void setJoinRank(String playerIdx, int rank) {
        if (rank <= 0) {
            return;
        }
        gloryroadEntity entity = gloryroadCache.getInstance().getEntity(playerIdx);
        if (entity != null) {
            SyncExecuteFunction.executeConsumer(entity, e -> e.getDbBuilder().setJoinArenaRank(rank));
        }
    }

    private void refreshBattleTree() {
        for (String onlinePlayerIdx : GlobalData.getInstance().getAllOnlinePlayerIdx()) {
            if (PlayerUtil.queryFunctionLock(onlinePlayerIdx, EnumFunction.EF_GloryRoad)) {
                continue;
            }
            sendBattleTreeMsg(onlinePlayerIdx);
        }
    }

    @Override
    public synchronized void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        GloryRoadSchedule config = getCurScheduleConfig();

        boolean needEnterNextSchedule;
        if (config == null) {
            needEnterNextSchedule = currentTime > getOpenTime();
        } else {
            if (currentTime > config.getStartTime() && !config.operateIsEmpty()) {
                doOperate(config);
            }
            needEnterNextSchedule = config.operateIsEmpty() && currentTime > config.getEndTime();
        }

        if (needEnterNextSchedule) {
            enterNextSchedule(config);
        }

        if (this.quiz != null) {
            this.quiz.onTick();
        }

        if (!this.indexBattleMap.isEmpty()) {
            for (GloryRoadBattle value : this.indexBattleMap.values()) {
                value.onTick();
            }
        }

       // tickPush(currentTime);
    }

    private void doOperate(GloryRoadSchedule config) {
        if (config == null) {
            return;
        }
        EnumGloryRoadOperateType operate = adjustOperate(config.popOperate());
        if (operate == null) {
            return;
        }

//        LogUtil.info("GloryRoadManager.doOperate, schedule:" + config + ", operate:" + operate);

        boolean operateResult;
        if (operate == EnumGloryRoadOperateType.EGROT_SELECTED_PLAYER) {
            operateResult = selectedPlayer();

        } else if (operate == EnumGloryRoadOperateType.EGROT_OPEN_QUIZ) {
            operateResult = openQuiz();

        } else if (operate == EnumGloryRoadOperateType.EGROT_BATTLE_AUTO) {
            operateResult = startBattle(true, config.getSchedule());

        } else if (operate == EnumGloryRoadOperateType.EGROT_BATTLE_MANUAL) {
            operateResult = startBattle(false, config.getSchedule());

        } else if (operate == EnumGloryRoadOperateType.EGROT_RED_BAG) {
            operateResult = openRedBag();

        } else if (operate == EnumGloryRoadOperateType.EGROT_SETTLE_RANKING) {
            operateResult = settleRanking();

        } else if (operate == EnumGloryRoadOperateType.EGROT_CLEAR) {
            operateResult = clearLastSeasonInfo();

        } else if (operate == EnumGloryRoadOperateType.EGROT_OPEN_NEW) {
            operateResult = initTime();
            sendGloryRoadInfoMsgToAlSatisfyPlayer();
            sendAllRefreshBattleScheduleMsg();

        } else {
//            LogUtil.error("GloryRoadManager.doOperate, unsupported operate, operate:" + operate
//                    + ", schedule:" + config.getSchedule());
            operateResult = false;
        }

        if (!operateResult) {
//            LogUtil.error("GloryRoadManager.doOperate, operate failed, operate:" + operate
//                    + ", schedule:" + config.getSchedule());

            config.addOperate(operate);
        } else {
//            LogUtil.info("GloryRoadManager.doOperate, schedule:" + config + ", operate:" + operate + ",success");
        }
    }

    /**
     * 当服务器启动后赛程有可能处于追赶状态,所以需要修改部分操作流程
     *
     * @param operate
     * @return
     */
    private EnumGloryRoadOperateType adjustOperate(EnumGloryRoadOperateType operate) {
        if (operate == null) {
            return null;
        }

        if (GlobalTick.getInstance().getCurrentTime() < getCurScheduleEndTime()) {
            return operate;
        }

        EnumGloryRoadOperateType returnOperate = operate;
        if (operate == EnumGloryRoadOperateType.EGROT_BATTLE_MANUAL) {
            returnOperate = EnumGloryRoadOperateType.EGROT_BATTLE_AUTO;
        }
//        LogUtil.info("GloryRoadManager.adjustOperate, before:" + operate + ", after:" + returnOperate);
        return operate;
    }

    private boolean clearLastSeasonInfo() {
        this.battleTree.clear();
        this.joinPlayerSet.clear();
        gloryroadCache.getInstance().clear();
//        RankingManager.getInstance().clearRanking(EnumRankingType.ERT_GloryRoad);
//        LogUtil.info("GloryRoadManager.clearPlayerLastSeasonInfo, finished");
        return true;
    }

    public EnumGloryRoadSchedule getNextScheduleWithoutNull() {
        GloryRoadSchedule config = getNextScheduleConfigWithoutNull();
        return config == null ? null : config.getSchedule();
    }

    public EnumGloryRoadSchedule getPreviousScheduleWithoutNull() {
        GloryRoadSchedule config = getPreviousScheduleConfigWithoutNull();
        return config == null ? null : config.getSchedule();
    }

    /**
     * 获取下一个赛程配置 不包含空闲阶段
     *
     * @return
     */
    public GloryRoadSchedule getNextScheduleConfigWithoutNull() {
        for (int i = this.curScheduleIndex.get() + 1; i < this.scheduleList.size(); i++) {
            GloryRoadSchedule schedule = this.scheduleList.get(i);
            if (schedule.getSchedule() != null && schedule.getSchedule() != EnumGloryRoadSchedule.EGRS_NUll) {
                return schedule;
            }
        }
//        LogUtil.error("GloryRoadManager.getNextScheduleConfigWithoutNull, can not get next schedule, cur index:" + this.curScheduleIndex.get());
        return null;
    }

    public GloryRoadSchedule getNextScheduleConfig() {
        for (int i = this.curScheduleIndex.get() + 1; i < this.scheduleList.size(); i++) {
            GloryRoadSchedule schedule = this.scheduleList.get(i);
            if (schedule.getSchedule() != null) {
                return schedule;
            }
        }
//        LogUtil.warn("GloryRoadManager.getNextScheduleConfig, can not get next schedule, cur index:" + this.curScheduleIndex.get());
        return null;
    }

    /**
     * 获取上一个赛程配置 不包含空闲阶段
     *
     * @return
     */
    public GloryRoadSchedule getPreviousScheduleConfigWithoutNull() {
        for (int i = this.curScheduleIndex.get(); i > 0; i--) {
            GloryRoadSchedule schedule = this.scheduleList.get(i);
            if (schedule.getSchedule() != null && schedule.getSchedule() != EnumGloryRoadSchedule.EGRS_NUll) {
                return schedule;
            }
        }
//        LogUtil.error("GloryRoadManager.getLastScheduleConfig, can not get last schedule, cur index:" + this.curScheduleIndex.get());
        return null;
    }

    public void enterNextSchedule(GloryRoadSchedule curSchedule) {
        //战斗阶段结束需要检查当前阶段战斗是否已经全部结算
        if (curSchedule != null
                && curSchedule.getSchedule() != EnumGloryRoadSchedule.EGRS_NUll) {
            settleCurScheduleBattle(curSchedule);
        }

        if (!this.indexBattleMap.isEmpty()) {
//            LogUtil.info("GloryRoadManager.enterNextSchedule, index battle map is not empty, wait cur schedule battle settle finished");
            return;
        }

        if (this.curScheduleIndex.get() < (this.scheduleList.size() - 1)) {
            int nextIndex = this.curScheduleIndex.addAndGet(1);
//            LogUtil.info("GloryRoadManager.enterNextSchedule, enter next schedule index:" + nextIndex);
        } else {
//            LogUtil.info("GloryRoadManager.enterNextSchedule, index is reach last index:" + this.curScheduleIndex.get());
//
//            initTime();
//            LogUtil.debug("model.gloryroad.GloryRoadManager.enterNextSchedule, init new activity");
        }

        sendAllRefreshBattleScheduleMsg();
    }

    private void sendAllRefreshBattleScheduleMsg() {
        SC_RefreshBattleSchedule.Builder builder = buildBattleScheduleMsg();
        if (builder == null) {
            return;
        }
        GlobalData.getInstance().sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum.SC_RefreshBattleSchedule,
                buildBattleScheduleMsg(), GloryRoadUtil.LV_CONDITION);
    }

    public void sendRefreshBattleScheduleMsg(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        SC_RefreshBattleSchedule.Builder builder = buildBattleScheduleMsg();
        if (builder == null) {
            return;
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefreshBattleSchedule_VALUE, builder);
    }

    /**
     * 当到达最后一个阶段后,需要再初始化下一个开始时间后再刷新
     *
     * @return
     */
    public SC_RefreshBattleSchedule.Builder buildBattleScheduleMsg() {
        GloryRoadSchedule nextConfig = getNextScheduleConfig();
        if (nextConfig == null) {
            return null;
        }

        SC_RefreshBattleSchedule.Builder refreshBuilder = SC_RefreshBattleSchedule.newBuilder();
        refreshBuilder.setCurSchedule(getCurSchedule());
        refreshBuilder.setNextSchedule(nextConfig.getSchedule());
        refreshBuilder.setNextStartTime(nextConfig.getStartTime());

        return refreshBuilder;
    }

    private void settleCurScheduleBattle(GloryRoadSchedule curSchedule) {
        if (curSchedule == null || curSchedule.getSchedule() == null
                || curSchedule.getSchedule() == EnumGloryRoadSchedule.EGRS_NUll) {
            return;
        }

        List<GloryRoadOpponent> unSettleOpponentList = this.battleTree.getLevelUnSettleOpponent(curSchedule.getSchedule().getNumber() + 1);
        if (CollectionUtils.isNotEmpty(unSettleOpponentList)) {
            for (GloryRoadOpponent unSettleOpponent : unSettleOpponentList) {
                if (unSettleOpponent.getPlayerIdx1() == null || unSettleOpponent.getPlayerIdx2() == null) {
                    continue;
                }

                boolean needCreate = false;
                GloryRoadBattle player1Battle = getBattleByPlayer(unSettleOpponent.getPlayerIdx1());
                GloryRoadBattle player2Battle = getBattleByPlayer(unSettleOpponent.getPlayerIdx2());
                if (unSettleOpponent.getPlayerIdx1() == null) {
                    needCreate = player2Battle == null;
                } else if (unSettleOpponent.getPlayerIdx2() == null) {
                    needCreate = player1Battle == null;
                } else {
                    needCreate = player1Battle == null && player2Battle == null;
                }

                if (needCreate) {
                    GloryRoadBattle entity = GloryRoadBattle.createEntity(unSettleOpponent, 0);
                    if (entity != null && addToBattleMap(entity)) {
//                        LogUtil.info("GloryRoadManager.settleCurScheduleBattle, opponent:" + unSettleOpponent
//                                + ", success add to battle map");
                    } else {
//                        LogUtil.error("GloryRoadManager.settleCurScheduleBattle, opponent:" + unSettleOpponent
//                                + ", create entity faild");
                    }
                } else {
                    if (player1Battle != null) {
                        player1Battle.prepareToDirectCheck();
                    }
                    if (player2Battle != null) {
                        player2Battle.prepareToDirectCheck();
                    }
//                    LogUtil.info("GloryRoadManager.settleCurScheduleBattle, opponent:" + unSettleOpponent
//                            + ", set status to direct check");
                }
            }
        }

//        if (this.indexBattleMap.isEmpty()) {
////            LogUtil.info("GloryRoadManager.settleCurScheduleBattle, battle map is empty");
//            return;
//        }
//
//        for (GloryRoadBattle value : indexBattleMap.values()) {
////            settleBattle(value.getPlayer_1_Idx(), value.getPlayer_2_Idx(), -1, null);
//            value.directCheck();
//            LogUtil.info("GloryRoadManager.settleCurScheduleBattle, playerIdx1:" + value.getPlayer_1_Idx()
//                    + ", playerIdx2:" + value.getPlayer_2_Idx() + ", is out of settle time, direct check");
//        }
//        LogUtil.info("GloryRoadManager.settleCurScheduleBattle finished");
    }

    public GloryRoadSchedule getCurScheduleConfig() {
        if (this.curScheduleIndex.get() >= this.scheduleList.size() || this.curScheduleIndex.get() < 0) {
//            LogUtil.warn("GloryRoadManager.getCurScheduleConfig, can not get cur schedule, total length:"
//                    + this.scheduleList.size() + ", cur index:" + this.curScheduleIndex.get());
            return null;
        }
        return this.scheduleList.get(this.curScheduleIndex.get());
    }

    public EnumGloryRoadSchedule getCurSchedule() {
        GloryRoadSchedule curScheduleConfig = getCurScheduleConfig();
        if (curScheduleConfig == null || curScheduleConfig.getSchedule() == null) {
            return EnumGloryRoadSchedule.EGRS_NUll;
        }
        return curScheduleConfig.getSchedule();
    }

    public long getCurScheduleStartTime() {
        GloryRoadSchedule scheduleConfig = getCurScheduleConfig();
        if (scheduleConfig == null) {
            return -1;
        }
        return scheduleConfig.getStartTime();
    }

    public long getCurScheduleEndTime() {
        GloryRoadSchedule scheduleConfig = getCurScheduleConfig();
        if (scheduleConfig == null) {
            return -1;
        }
        return scheduleConfig.getEndTime();
    }

    public long getOpenTime() {
        if (CollectionUtils.isNotEmpty(scheduleList)) {
            return scheduleList.get(0).getStartTime();
        }
//        LogUtil.error("GloryRoadManager.getOpenTime, scheduleList is empty");
        return -1;
    }

    public long getEndTime() {
        if (CollectionUtils.isEmpty(this.scheduleList)) {
//            LogUtil.error("GloryRoadManager.getOpenTime, scheduleList is empty");
            return -1;
        }
        return this.scheduleList.get(this.scheduleList.size() - 1).getEndTime();
    }

    /**
     * @param playerIdx1 战斗玩家1
     * @param playerIdx2 战斗玩家2
     * @param idx1Result 战斗玩家1结果    -1为平局，PVE中1为玩家胜利，2为怪物胜利，3为玩家投降
     */
    public void settleBattle(String playerIdx1, String playerIdx2, int idx1Result, String linkBattleRecordId) {
        if (StringUtils.isEmpty(playerIdx1) && StringUtils.isEmpty(playerIdx2)) {
            return;
        }
//        LogUtil.info("GloryRoadManager.settleBattle, playerIdx1:" + playerIdx1 + ", playerIdx2:"
//                + playerIdx2 + ", player1Result:" + idx1Result);
        boolean player1Win = idx1Result == 1;
        //平局时, 根据入选时排名高的晋级
        if (idx1Result == -1
                && gloryroadCache.getInstance().getPlayerJoinArenaRank(playerIdx1)
                <= gloryroadCache.getInstance().getPlayerJoinArenaRank(playerIdx2)) {
            player1Win = true;
        }

        if (player1Win) {
            settleBattle(playerIdx1, playerIdx2, linkBattleRecordId);
        } else {
            settleBattle(playerIdx2, playerIdx1, linkBattleRecordId);
        }
    }

    /**
     * 设置晋级树， 加胜利场数
     *
     * @param winPlayerIdx
     * @param failedPlayerIdx
     */
    public synchronized boolean settleBattle(String winPlayerIdx, String failedPlayerIdx, String linkBattleRecordId) {
        GloryRoadBattle battleByPlayer = getBattleByPlayer(winPlayerIdx);
        if (battleByPlayer == null || !battleByPlayer.containsPlayer(failedPlayerIdx)) {
//            LogUtil.error("GloryRoadManager.settleBattle, win playerIdx:" + winPlayerIdx + ", failedIdx:"
//                    + failedPlayerIdx + ", is not match battle info:" + battleByPlayer);
            if (battleByPlayer == null) {
                sendInvalidBattleTipsByPlayerIdx(winPlayerIdx);
                sendInvalidBattleTipsByPlayerIdx(failedPlayerIdx);
            }
            return false;
        }

        if (!this.battleTree.setWinPlayer(battleByPlayer.getParentIndex(), winPlayerIdx, linkBattleRecordId)) {
            return false;
        }

        //增加胜利场次和更新排行榜
        gloryroadEntity winEntity = gloryroadCache.getInstance().getEntity(winPlayerIdx);
        addWinCount(winEntity);

        //所有赛程结束后在更新
//        updateRanking(winEntity);
//        updateRanking(gloryroadCache.getInstance().getEntity(failedPlayerIdx));

        removeFromBattle(battleByPlayer.getParentIndex());

        sendPromotionNotice(winPlayerIdx, true);
        sendPromotionNotice(failedPlayerIdx, false);

        if (this.quiz != null) {
            if (this.quiz.settleQuiz(winPlayerIdx, failedPlayerIdx, linkBattleRecordId)) {
//                LogUtil.info("GloryRoadManager.settleBattle, settle quiz success, playerIdx1:" + winPlayerIdx
//                        + ", playerIdx2:" + failedPlayerIdx + " success, remove quiz, detail:" + this.quiz);
                this.quiz = null;
            }
        }
//        LogUtil.info("GloryRoadManager.settleBattle, settle battle result success, winPlayerIdx:" + winPlayerIdx
//                + ", failedIdx:" + failedPlayerIdx);
        return true;
    }

    private void sendPromotionNotice(String playerIdx, boolean promotionSuccess) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        EnumGloryRoadSchedule schedule = getCurSchedule();
        if (GlobalData.getInstance().checkPlayerOnline(playerIdx)) {
            SC_GloryRoadPromotionNotice.Builder noticeBuilder = SC_GloryRoadPromotionNotice.newBuilder();
            noticeBuilder.setPromotionSuccess(promotionSuccess);
            noticeBuilder.setSchedule(schedule);

            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_GloryRoadPromotionNotice_VALUE, noticeBuilder);
        } else {
            EnumGloryRoadSchedule curSchedule = getCurSchedule();
            if (curSchedule == null) {
//                LogUtil.error("GloryRoadManager.sendPromotionNotice, get cur schedule failed, can not send mail");
                return;
            }

            GloryRoadStageConfigObject stageCfg = GloryRoadStageConfig.getById(curSchedule.getNumber());
            if (stageCfg == null) {
//                LogUtil.error("GloryRoadManager.sendPromotionNotice, can not get stage config, schedule:" + curSchedule);
                return;
            }

            int strId = promotionSuccess ? stageCfg.getServerpromotion() : stageCfg.getServerpromotionfailed();
            String scheduleStr = ServerStringRes.getContentByLanguage(strId, PlayerUtil.queryPlayerLanguage(playerIdx));
            GloryRoadConfigObject config = GloryRoadConfig.getById(GameConst.CONFIG_ID);
            int mailTemplate = promotionSuccess ? config.getPromotionmailtemplate() : config.getPromotionfailedmailtemplate();

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GloryRoad);
            EventUtil.triggerAddMailEvent(playerIdx, mailTemplate, null, reason, scheduleStr);
        }
    }

    private void addWinCount(gloryroadEntity entity) {
        if (entity == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDbBuilder().setWinCount(entity.getDbBuilder().getWinCount() + 1);
        });
    }

    private void updateRanking(gloryroadEntity entity) {
        if (entity == null) {
            return;
        }
        int newWinCount = entity.getDbBuilder().getWinCount();
        int joinRank = entity.getDbBuilder().getJoinArenaRank();
        RankingManager.getInstance().updatePlayerRankingScore(entity.getPlayeridx(), EnumRankingType.ERT_GloryRoad,
                newWinCount, joinRank);
    }

    private synchronized void removeFromBattle(int battleIndex) {
        GloryRoadBattle battle = this.indexBattleMap.remove(battleIndex);
        if (battle == null) {
            return;
        }
        if (battle.getPlayer_1_Idx() != null) {
            this.playerIndexMap.remove(battle.getPlayer_1_Idx());
        }
        if (battle.getPlayer_2_Idx() != null) {
            this.playerIndexMap.remove(battle.getPlayer_2_Idx());
        }
//        LogUtil.info("GloryRoadManager.removeFromBattle, remove, detail:" + battle);
    }

    private void loadGloryLoadBuilder() {
        gameplayEntity gamePlay = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_GloryRoad);
        if (gamePlay.getGameplayinfo() != null) {
            try {
                this.dbGloryLoadBuilder = DB_GloryLoad.parseFrom(gamePlay.getGameplayinfo()).toBuilder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (this.dbGloryLoadBuilder == null) {
            this.dbGloryLoadBuilder = DB_GloryLoad.newBuilder();
        }
    }

    /**
     * ===========================红包 start =========================================
     */
    public synchronized boolean openRedBag() {
        EnumGloryRoadSchedule lastSchedule = getPreviousScheduleWithoutNull();
        if (lastSchedule == null) {
            return false;
        }
        List<String> promotion = this.battleTree.getPlayerIdxList(lastSchedule.getNumber());
        this.redBag = RedBag.createEntity(promotion, lastSchedule, getCurScheduleStartTime());
        return true;
    }

    public RetCodeEnum claimRedBagRewards(String playerIdx, int redBagCount) {
        if (this.redBag == null) {
            return RetCodeEnum.RCE_GloryRoad_RedBag_OutOfTime;
        }
        return this.redBag.claimRewards(playerIdx, redBagCount);
    }

    /**
     * 是否是升级触发
     *
     * @param playerIdx
     * @param lvUp
     */
    public void onPlayerLogin(String playerIdx, boolean lvUp) {
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_GloryRoad)) {
            return;
        }

        if (lvUp) {
            sendGloryRoadInfoMsg(playerIdx);
            sendRefreshBattleScheduleMsg(playerIdx);
        }

        if (this.redBag != null) {
            this.redBag.onPlayerLogin(playerIdx);
        }

        GloryRoadBattle battle = getBattleByPlayer(playerIdx);
        if (battle != null) {
            battle.onPlayerLogin(playerIdx);
        }
    }

    /**
     * ===========================红包 end =========================================
     */

    /**
     * ===========================竞猜 start =========================================
     */

    /**
     * 开启竞猜
     *
     * @return
     */
    private boolean openQuiz() {
        GloryRoadSchedule nextNotNullScheduleConfig = getNextScheduleConfigWithoutNull();
        if (nextNotNullScheduleConfig == null) {
            return false;
        }
        int level = nextNotNullScheduleConfig.getSchedule().getNumber() + 1;
        GloryRoadOpponent choiceOpponent = choiceOneOpponent(this.battleTree.getLevelOpponent(level));
        if (choiceOpponent == null) {
//            LogUtil.error("GloryRoadManager.openQuiz, level opponent is empty, level:" + level);
            return false;
        }

        this.quiz = GloryRoadQuiz.createEntity(choiceOpponent, nextNotNullScheduleConfig.getSchedule(), nextNotNullScheduleConfig.getStartTime());
        LogUtil.info("model.gloryroad.GloryRoadManager.openQuiz, detail:" + this.quiz);
        return true;
    }

    /**
     * 双方阵容不为空>双方玩家一方没有队伍>双方都有玩家>单个玩家
     * 优先选择双方队伍不为空的阵容,其次是双方都有玩家，一方有玩家
     *
     * @return
     */
    public GloryRoadOpponent choiceOneOpponent(List<GloryRoadOpponent> levelOpponentList) {
        if (CollectionUtils.isEmpty(levelOpponentList)) {
//            LogUtil.error("GloryRoadManager.choiceOneOpponent, param list is empty");
            return null;
        }

        List<GloryRoadOpponent> bothPlayerBothTeam = new ArrayList<>();
        List<GloryRoadOpponent> bothPlayerHalfTeam = new ArrayList<>();
        List<GloryRoadOpponent> bothPlayer = new ArrayList<>();
        List<GloryRoadOpponent> halfPlayer = new ArrayList<>();

        for (GloryRoadOpponent opponent : levelOpponentList) {
            if (StringUtils.isEmpty(opponent.getPlayerIdx1()) && StringUtils.isEmpty(opponent.getPlayerIdx2())) {
                continue;
            }

            List<String> player1Pets =
                    teamCache.getInstance().getCurUsedTeamPetIdxList(opponent.getPlayerIdx1(), TeamTypeEnum.TTE_GloryRoad);
            List<String> player2Pets =
                    teamCache.getInstance().getCurUsedTeamPetIdxList(opponent.getPlayerIdx2(), TeamTypeEnum.TTE_GloryRoad);

            if (StringUtils.isNotEmpty(opponent.getPlayerIdx1()) && StringUtils.isNotEmpty(opponent.getPlayerIdx2())) {
                if (CollectionUtils.isNotEmpty(player1Pets) && CollectionUtils.isNotEmpty(player2Pets)) {
                    bothPlayerBothTeam.add(opponent);

                } else if (CollectionUtils.isNotEmpty(player1Pets) || CollectionUtils.isNotEmpty(player2Pets)) {
                    bothPlayerHalfTeam.add(opponent);

                } else {
                    bothPlayer.add(opponent);
                }
            } else {
                halfPlayer.add(opponent);
            }
        }

        Random random = new Random();
        if (CollectionUtils.isNotEmpty(bothPlayerBothTeam)) {
            return bothPlayerBothTeam.get(random.nextInt(bothPlayerBothTeam.size()));
        }

        if (CollectionUtils.isNotEmpty(bothPlayerHalfTeam)) {
            return bothPlayerHalfTeam.get(random.nextInt(bothPlayerHalfTeam.size()));
        }

        if (CollectionUtils.isNotEmpty(bothPlayer)) {
            return bothPlayer.get(random.nextInt(bothPlayer.size()));
        }

        if (CollectionUtils.isNotEmpty(halfPlayer)) {
            return halfPlayer.get(random.nextInt(halfPlayer.size()));
        }

        LogUtil.error("model.gloryroad.GloryRoadManager.choiceOneOpponent, can not select one opponent tp quiz," +
                " detail:" + GameUtil.collectionToString(levelOpponentList));
        return null;
    }

    public RetCodeEnum supportPlayer(String playerIdx, String supportIdx) {
        if (this.quiz == null) {
            return RetCodeEnum.RCE_GloryRoad_Quiz_OutOfTime;
        }
        return this.quiz.supportPlayer(playerIdx, supportIdx);
    }

    public RetCodeEnum addComment(String playerIdx, String comment) {
//        if (GlobalTick.getInstance().getCurrentTime() > this.endTime) {
//            return RetCodeEnum.RCE_GloryRoad_Quiz_OutOfTime;
//        }
        if (StringUtils.isEmpty(playerIdx) || StringUtils.isEmpty(comment)) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        Long lastCommentTime = this.lastCommentTime.get(playerIdx);
        if (lastCommentTime != null
                && GlobalTick.getInstance().getCurrentTime() - lastCommentTime <= this.config.getCommentsendinterval() * TimeUtil.MS_IN_A_S) {
            return RetCodeEnum.RCE_GloryRoad_Quiz_CommentFrequently;
        }

        //长度判断
        if (ObjUtil.getStringWeight(comment) > this.config.getBetflymsgmaxweight()) {
            return RetCodeEnum.RCE_GloryRoad_Quiz_CommentTooLong;
        }
        //合法性检查
        if (!WordFilterManager.getInstance().checkPlatformSensitiveWords(comment)) {
            return RetCodeEnum.RCE_GloryRoad_Quiz_IllegalComment;
        }

        if (this.commentList.size() >= this.config.getQuizcommentcount()) {
            this.commentList.remove(0);
        }

        GloryRoadComment gloryRoadComment = GloryRoadComment.newBuilder().setFromIdx(playerIdx).setContent(comment).build();
        this.commentList.add(gloryRoadComment);
        borderComment(gloryRoadComment);

        return RetCodeEnum.RCE_Success;
    }

    public void borderComment(GloryRoadComment comment) {
        if (comment == null) {
            return;
        }

        GlobalData.getInstance().sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum.SC_GloryRoadAddComment,
                SC_GloryRoadAddComment.newBuilder().setContent(comment), GloryRoadUtil.LV_CONDITION);
    }

    /**
     * ===========================竞猜 end =========================================
     */

    /**
     * ===========================战斗 start =========================================
     */
    public RetCodeEnum manualOperateBattle(String playerIdx, boolean manual) {
        if (StringUtils.isEmpty(playerIdx)) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (CrossServerManager.getInstance().getMistForestPlayerServerInfo(playerIdx) != null) {
            return RetCodeEnum.RCE_MistForest_NotFoundMistFighter;
        }

        GloryRoadBattle battle = getBattleByPlayer(playerIdx);
        if (battle == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        return battle.manualOperate(playerIdx, manual);
    }

    public GloryRoadBattle getBattleByPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        Integer index = this.playerIndexMap.get(playerIdx);
        return index == null ? null : this.indexBattleMap.get(index);
    }

    private boolean startBattle(boolean auto, EnumGloryRoadSchedule schedule) {
        if (schedule == null) {
//            LogUtil.info("GloryRoadManager.startBattle, schedule is null");
            return false;
        }
        List<GloryRoadOpponent> levelOpponent = this.battleTree.getLevelOpponent(schedule.getNumber() + 1);
        if (CollectionUtils.isEmpty(levelOpponent)) {
//            LogUtil.error("GloryRoadManager.startBattle, get level opponent is empty, schedule:" + schedule);
            return false;
        }

        long waitTime = auto ? 0 : this.config.getManualbattlewaittime() * TimeUtil.MS_IN_A_S;
        for (GloryRoadOpponent gloryRoadOpponent : levelOpponent) {
            if (gloryRoadOpponent.isEmpty()) {
//                LogUtil.info("GloryRoadManager.startBattle, opponent is empty, need not enter battle," +
//                        " parent index:" + gloryRoadOpponent.getParentIndex());
                continue;
            }

            GloryRoadBattle battle = GloryRoadBattle.createEntity(gloryRoadOpponent, waitTime);
            if (battle == null) {
//                LogUtil.error("GloryRoadManager.startBattle, create battle failed, detail:" + gloryRoadOpponent);
                return false;
            }

            if (!addToBattleMap(battle)) {
                return false;
            }
        }

        return true;
    }

    private boolean addToBattleMap(GloryRoadBattle battle) {
        if (battle == null) {
//            LogUtil.error("GloryRoadManager.addToBattleMap, params in empty");
            return false;
        }

//        LogUtil.info("GloryRoadManager.addToBattleMap, battle:" + battle);
        GloryRoadBattle oldVal = this.indexBattleMap.get(battle.getParentIndex());
        if (oldVal != null) {
//            LogUtil.error("GloryRoadManager.addToBattleMap, parent index is already in map, oldInfo:" + oldVal);
            return false;
        }

        if (battle.getPlayer_1_Idx() != null) {
            this.playerIndexMap.put(battle.getPlayer_1_Idx(), battle.getParentIndex());
        }
        if (battle.getPlayer_2_Idx() != null) {
            this.playerIndexMap.put(battle.getPlayer_2_Idx(), battle.getParentIndex());
        }
        this.indexBattleMap.put(battle.getParentIndex(), battle);

//        LogUtil.info("GloryRoadManager.addToBattleMap, add battle to map success, detail:" + battle);
        return true;
    }

    /**
     * ===========================战斗 end =========================================
     */

    public boolean settleRanking() {
        updateAllPlayerRanking();

        MailRankingSettleHandler handler = new GloryRoadRankingSettleHandler(EnumRankingType.ERT_GloryRoad,
                RankRewardTargetConfig.getInstance().getRangeRewardsByRankType(EnumRankingType.ERT_GloryRoad),
                this.config.getRankingsettlemailtemplate(), RewardSourceEnum.RSE_GloryRoad);
        handler.settleRanking();

        this.dbGloryLoadBuilder.setLastSettleTime(GlobalTick.getInstance().getCurrentTime());
        LogUtil.info("model.gloryroad.GloryRoadManager.settleRanking, finished settle ranking rewards");
        return true;
    }

    public void sendBattleTreeMsg(String playerIdx) {
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimGloryRoadBattleTree_VALUE, buildBattleTreeMsg(playerIdx));
    }

    private SC_ClaimGloryRoadBattleTree.Builder buildBattleTreeMsg(String playerIdx) {
        SC_ClaimGloryRoadBattleTree.Builder resultBuilder = SC_ClaimGloryRoadBattleTree.newBuilder();

        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_GloryRoad)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            return resultBuilder;
        }

        if (StringUtils.isEmpty(playerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            return resultBuilder;
        }

        if (this.battleTree != null) {
            List<NodeMessage> nodeMessages = this.battleTree.buildNodeMessage();
            if (CollectionUtils.isNotEmpty(nodeMessages)) {
                resultBuilder.addAllNodes(nodeMessages);
            }
        }

        for (String idx : joinPlayerSet) {
            NodePlayerInfo playerInfo = GloryRoadUtil.buildNodePlayerInfo(idx);
            if (playerInfo != null) {
                resultBuilder.addPlayerInfo(playerInfo);
            }
        }

        if (this.quiz != null) {
            GloryRoadQuizInfo.Builder quizPlayerInfo = this.quiz.buildGloryRoadQuizPlayerInfo(playerIdx);
            if (quizPlayerInfo != null) {
                resultBuilder.setQuizInfo(quizPlayerInfo);
            }
        }
        resultBuilder.addAllComment(this.commentList);
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        return resultBuilder;
    }

    public void updateAllPlayerRanking() {
        if (CollectionUtils.isEmpty(this.joinPlayerSet)) {
            return;
        }
        RankingManager.getInstance().clearRanking(EnumRankingType.ERT_GloryRoad);
        RankingUpdateRequest request = new RankingUpdateRequest(RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_GloryRoad),
                ServerConfig.getInstance().getServer(), RankingUpdateRequest.DEFAULT_SORT_RULES);
        for (String idx : this.joinPlayerSet) {

            gloryroadEntity entity = gloryroadCache.getInstance().getEntity(idx);
            if (entity == null) {
//                LogUtil.error("GloryRoadManager.updateAllPlayerRanking, can not get glory entity, idx:" + idx);
                continue;
            }
            int newWinCount = entity.getDbBuilder().getWinCount();
            int joinRank = entity.getDbBuilder().getJoinArenaRank();
            request.addScore(idx, newWinCount, joinRank);
        }
        if (!HttpRequestUtil.updateRanking(request)) {
//            LogUtil.error("GloryRoadManager.updateAllPlayerRanking, update all join player ranking failed");
        }
    }

    public void sendGloryRoadInfoMsg(String playerIdx) {
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimGloryRoadInfo_VALUE, buildGloryRoadInfo(playerIdx));
        sendRefreshBattleScheduleMsg(playerIdx);
    }

    public SC_ClaimGloryRoadInfo.Builder buildGloryRoadInfo(String playerIdx) {
        SC_ClaimGloryRoadInfo.Builder resultBuilder = SC_ClaimGloryRoadInfo.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_GloryRoad)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            return resultBuilder;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        EnumGloryRoadSchedule schedule = GloryRoadManager.getInstance().getCurSchedule();
        if (schedule != null) {
            resultBuilder.setCurSchedule(schedule);
        }

        gloryroadEntity entity = gloryroadCache.getInstance().getEntity(playerIdx);
        if (entity != null) {
            resultBuilder.setTopRank(entity.getDbBuilder().getTopRank());
        }

        String rankingName = RankingUtils.getRankingTypeDefaultName(EnumRankingType.ERT_ArenaScoreLocal);
        int rank = RankingManager.getInstance().queryPlayerRanking(rankingName, playerIdx);
        resultBuilder.setLocalArenaRank(rank);

        resultBuilder.setNextOpenTime(getOpenTime());

        return resultBuilder;
    }

    public void sendGloryRoadInfoMsgToAlSatisfyPlayer() {
        for (String playerIdx : GlobalData.getInstance().getAllOnlinePlayerIdx()) {
            sendGloryRoadInfoMsg(playerIdx);
        }
    }

    public void sendIndexRecordTeamInfoMsg(String playerIdx, int index) {
        SC_ClaimGloryRoadNodeTeamRecord.Builder resultBuilder = SC_ClaimGloryRoadNodeTeamRecord.newBuilder();
        if (StringUtils.isEmpty(playerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimGloryRoadNodeTeamRecord_VALUE, resultBuilder);
            return;
        }

        GloryRoadTreeContent.Builder content = this.battleTree.getContent(index);
        if (content == null || content.getPlayerIdx() == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimGloryRoadNodeTeamRecord_VALUE, resultBuilder);
            return;
        }

        RecentPassed recent = content.hasRecent() ? content.getRecent() : null;
        if (recent == null) {
            recent = RecentPassedUtil.buildRecentPassedInfo(content.getPlayerIdx(), TeamTypeEnum.TTE_GloryRoad);
        }

        if (recent == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
        } else {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setRecentPassed(recent);
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_ClaimGloryRoadNodeTeamRecord_VALUE, resultBuilder);
    }

    public boolean directCheckBattle(int parentIndex) {
        GloryRoadBattle battle = this.indexBattleMap.get(parentIndex);
        if (battle == null) {
//            LogUtil.warn("GloryRoadManager.directCheckBattle, parent index:" + parentIndex + ", battle is not exist");
            return false;
        }
        boolean checkResult = battle.directCheck();
//        LogUtil.info("GloryRoadManager.directCheckBattle, direct check battle, index:" + parentIndex
//                + ", check result:" + checkResult);
        return checkResult;
    }

    public long getFirstBattleTime() {
        for (GloryRoadSchedule schedule : this.scheduleList) {
            if (schedule != null
                    && schedule.getSchedule() != null
                    && schedule.getSchedule() != EnumGloryRoadSchedule.EGRS_NUll) {
                return schedule.getStartTime();
            }
        }
        return -1L;
    }

    private void tickPush(long curTime) {
        if (this.joinPlayerSet.isEmpty()) {
            return;
        }

        GloryRoadSchedule nextScheduleConfig = getNextScheduleConfig();
        if (nextScheduleConfig == null
                || nextScheduleConfig.getSchedule() == null
                || nextScheduleConfig.getSchedule() == EnumGloryRoadSchedule.EGRS_NUll) {
            return;
        }

        if (!this.needPushSchedule.contains(nextScheduleConfig.getSchedule())) {
            return;
        }

        if (nextScheduleConfig.getStartTime() - curTime > pushAdvanceTime) {
            return;
        }

        push(nextScheduleConfig.getSchedule());
        this.needPushSchedule.remove(nextScheduleConfig.getSchedule());
//        LogUtil.info("GloryRoadManager.tickPush, push schedule:" + nextScheduleConfig.getSchedule());
    }

    /**
     * 只推送等级满足条件的玩家, 且已经淘汰的玩家发送普通推送
     * 还未淘汰的玩家发送战斗推送
     *
     * @return
     */
    private void push(EnumGloryRoadSchedule schedule) {
        if (schedule == null) {
            return;
        }
        List<String> allPlayerIdx = playerCache.getInstance().getAllPlayerIdx();
        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();

        Set<String> commonPushUserIdSet = new HashSet<>();
        Set<String> battlePushUserIdSet = new HashSet<>();
        List<String> promotionPlayer = this.battleTree.getPlayerIdxList(schedule.getNumber() + 1);

        for (String playerIdx : allPlayerIdx) {
            if (allOnlinePlayerIdx.contains(playerIdx)
                    || PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_GloryRoad)) {
                continue;
            }

            String userId = PlayerUtil.queryPlayerUserId(playerIdx);
            if (StringUtils.isEmpty(userId)) {
                continue;
            }

            if (promotionPlayer.contains(playerIdx)) {
                battlePushUserIdSet.add(userId);
            } else {
                commonPushUserIdSet.add(userId);
            }
        }

   /*     PushManage.sendPushMsgToUserList(PushMsgIdEnum.GloryRoad_Battle, battlePushUserIdSet);
        PushManage.sendPushMsgToUserList(PushMsgIdEnum.GloryRoad_Common, commonPushUserIdSet);*/
    }

    private boolean revertFromDB() {
        if (this.dbGloryLoadBuilder.getSchedulesCount() <= 0 || this.dbGloryLoadBuilder.getContentsCount() <= 0) {
//            LogUtil.info("GloryRoadManager.revertFromDB, revert failed, schedule list or tree content is empty");
            return false;
        }

        for (DB_GloryRoadSchedule dbGloryLoadSchedule : this.dbGloryLoadBuilder.getSchedulesList()) {
            GloryRoadSchedule schedule = new GloryRoadSchedule();
            schedule.setSchedule(dbGloryLoadSchedule.getSchedule());
            schedule.setStartTime(dbGloryLoadSchedule.getStartTime());
            schedule.setEndTime(dbGloryLoadSchedule.getEndTime());
            schedule.addAllOperate(dbGloryLoadSchedule.getOperateList());

            addSchedule(schedule);
        }

        this.battleTree.revertContent(this.dbGloryLoadBuilder.getContentsList());

        this.curScheduleIndex.set(this.dbGloryLoadBuilder.getCurIndex());

        this.quiz = GloryRoadQuiz.createEntity(this.dbGloryLoadBuilder.getQuiz());

        this.redBag = RedBag.createEntity(this.dbGloryLoadBuilder.getRedBag());

        Set<String> joinSet = this.dbGloryLoadBuilder.getContentsList().stream()
                .map(GloryRoadTreeContent::getPlayerIdx)
                .collect(Collectors.toSet());
        this.joinPlayerSet.addAll(joinSet);

        //当存储的活动时间大于当前时间  属于新活动不需要恢复
        long dbOpenTime = this.dbGloryLoadBuilder.getSchedulesList().get(0).getStartTime();
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (dbOpenTime > currentTime) {
//            LogUtil.info("GloryRoadManager.revertFromDB, next open time :" + dbOpenTime + ", is max  than currentTime:" + currentTime);
            return false;
        }

        return true;
    }

    private synchronized void updateGamePlayDbData() {
        this.dbGloryLoadBuilder.clearContents();
        if (this.battleTree != null) {
            List<GloryRoadTreeContent> contentDBList = this.battleTree.getContentDbList();
            if (CollectionUtils.isNotEmpty(contentDBList)) {
                this.dbGloryLoadBuilder.addAllContents(contentDBList);
            }
        }

        this.dbGloryLoadBuilder.clearSchedules();
        this.dbGloryLoadBuilder.addAllSchedules(getScheduleDbList());

        this.dbGloryLoadBuilder.setCurIndex(this.curScheduleIndex.get());

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        this.dbGloryLoadBuilder.clearQuiz();
        if (this.quiz != null && this.quiz.getEndTime() > currentTime) {
            this.dbGloryLoadBuilder.setQuiz(this.quiz.buildQuizDbData());
        }

        this.dbGloryLoadBuilder.clearRedBag();
        if (this.redBag != null && this.redBag.getEndTime() > currentTime) {
            this.dbGloryLoadBuilder.setRedBag(this.redBag.buildDbData());
        }
    }

    private List<DB_GloryRoadSchedule> getScheduleDbList() {
        return this.scheduleList.stream()
                .map(e -> {
                    DB_GloryRoadSchedule.Builder newBuilder = DB_GloryRoadSchedule.newBuilder();
                    newBuilder.setSchedule(e.getSchedule());
                    newBuilder.setStartTime(e.getStartTime());
                    newBuilder.setEndTime(e.getEndTime());
                    if (e.getOperateList() != null) {
                        newBuilder.addAllOperate(e.getOperateList());
                    }
                    return newBuilder.build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void update() {
        updateGamePlayDbData();

        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_GloryRoad);
        entity.setGameplayinfo(this.dbGloryLoadBuilder.build().toByteArray());
        gameplayCache.put(entity);
        LogUtil.info("model.gloryroad.GloryRoadManager.update, finished");
    }

    private void sendInvalidBattleTipsByPlayerIdx(String playerIdx) {
        if (!GlobalData.getInstance().checkPlayerOnline(playerIdx)) {
            return;
        }
        int invalidTips = GloryRoadConfig.getById(GameConst.CONFIG_ID).getInvailedbattletips();
        String content = ServerStringRes.getContentByLanguage(invalidTips, PlayerUtil.queryPlayerLanguage(playerIdx));
        if (StringUtils.isEmpty(content)) {
            return;
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_Tips_VALUE, SC_Tips.newBuilder().setMsg(content));
    }
}