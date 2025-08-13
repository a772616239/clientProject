package model.activity.petAvoidance;

import common.FunctionExclusion;
import common.GameConst.EventType;
import common.GlobalData;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.PetAvoidanceGameSettleLog;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_PetAvoidanceFrameData;
import protocol.Activity.EnumRankingType;
import protocol.Activity.SC_PetAvoidanceEnd;
import protocol.Activity.SC_PetAvoidanceStart;
import protocol.Common;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_PetAvoidance.Builder;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;
import util.LogUtil;

/**
 * 魔灵大躲避游戏管理器
 */
public class PetAvoidanceGameManager implements Tickable {

    public static int TIMEOUT_DELAY_TIME = 30 *1000;

    private static PetAvoidanceGameManager instance;

    public static PetAvoidanceGameManager getInstance() {
        if (instance == null) {
            synchronized (PetAvoidanceGameManager.class) {
                if (instance == null) {
                    instance = new PetAvoidanceGameManager();
                }
            }
        }
        return instance;
    }

    private PetAvoidanceGameManager() {
    }

    private ScoreValidator scoreValidator;
    private Map<String, PetAvoidanceGameData> petAvoidanceDataMap = new ConcurrentHashMap<>();

    public boolean init() {
        return initScoreValidator() && GlobalTick.getInstance().addTick(this);
    }

    public ScoreValidator getScoreValidator() {
        return scoreValidator;
    }

    public boolean addInGamePlayer(String playerIdx, int gameDurationTime) {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        PetAvoidanceGameData petAvoidanceGameData = new PetAvoidanceGameData(playerIdx, 0, currentTime, currentTime + gameDurationTime);
        return petAvoidanceDataMap.putIfAbsent(playerIdx, petAvoidanceGameData) != null;
    }

    public void updateGameData(String playerIdx, CS_PetAvoidanceFrameData frameData) {
        PetAvoidanceGameData petAvoidanceGameData = petAvoidanceDataMap.get(playerIdx);
        if (petAvoidanceGameData != null) {
            petAvoidanceGameData.setCurScore(frameData.getScore());
        }
    }

    public boolean isInGame(String playerIdx) {
        return petAvoidanceDataMap.get(playerIdx) != null;
    }

    public PetAvoidanceGameData getGameData(String playerIdx) {
        return petAvoidanceDataMap.get(playerIdx);
    }

    public PetAvoidanceGameData removeGameData(String playerIdx) {
        return petAvoidanceDataMap.remove(playerIdx);
    }

    public SC_PetAvoidanceStart.Builder startPetAvoidGame(String playerIdx) {
        SC_PetAvoidanceStart.Builder resultBuilder = SC_PetAvoidanceStart.newBuilder();

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            return resultBuilder;
        }
        ServerActivity activityCfg = ActivityManager.getInstance().getActivityByType(ActivityTypeEnum.ATE_PetAvoidance);
        if (!ActivityUtil.activityInOpen(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            return resultBuilder;
        }
        if (PetAvoidanceGameManager.getInstance().isInGame(playerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_PetAvoidance_AlreadyInGame));
            return resultBuilder;
        }
        int sf = FunctionExclusion.getInstance().checkExclusionAll(playerIdx);
        if (sf > 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(FunctionExclusion.getInstance().getRetCodeByType(sf)));
            return resultBuilder;
        }
        int gameDurationTime = activityCfg.getPetAvoidance().getDurationTime() * 1000 + TIMEOUT_DELAY_TIME;
        int dailyTimesLimit = activityCfg.getPetAvoidance().getDailyChallengeTimes();
        Builder petAvoidanceBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getPetAvoidanceBuilder();
        // 超过每日次数限制
        if (petAvoidanceBuilder.getChallengedTimes() >= dailyTimesLimit) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_petAvoidance_TimesLimit));
            return resultBuilder;
        }
        boolean repeatStart = PetAvoidanceGameManager.getInstance().addInGamePlayer(playerIdx, gameDurationTime);
        if (repeatStart) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_PetAvoidance_AlreadyInGame));
            return resultBuilder;
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setDurationTime(activityCfg.getPetAvoidance().getDurationTime());
        return resultBuilder;
    }

    public void settle(String playerIdx, SC_PetAvoidanceEnd.Builder clBuilder) {
        PetAvoidanceGameData petAvoidanceGameData = removeGameData(playerIdx);
        settle(petAvoidanceGameData, clBuilder);
    }

    public void settle(PetAvoidanceGameData petAvoidanceGameData, SC_PetAvoidanceEnd.Builder clBuilder) {
        if (petAvoidanceGameData == null) {
            clBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.REC_PetAvoidance_NotInGame));
            LogUtil.warn("PetAvoidanceGameManager.settle err petAvoidanceGameData is null");
            return;
        }

        ServerActivity activity = ActivityManager.getInstance().getActivityByType(ActivityTypeEnum.ATE_PetAvoidance);
        if (!ActivityUtil.activityInOpen(activity)) {
            clBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            return;
        }
        String playerIdx = petAvoidanceGameData.getPlayerIdx();
        int score = petAvoidanceGameData.getCurScore();
        long startTime = petAvoidanceGameData.getStartTime();
        long endTime = petAvoidanceGameData.getEndTime();
        long currentTime = GlobalTick.getInstance().getCurrentTime();

        // 判断score是否异常并返回一个正常的分数
        long gameTime = currentTime - startTime;
        long maxTime = endTime - startTime;
        int realScore = scoreValidator.makeRightScore(score, gameTime, maxTime);

        clBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        clBuilder.setScore(score);

        targetsystemEntity targetsystemEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        boolean updateRanking = SyncExecuteFunction.executePredicate(targetsystemEntity, (entity) -> {
            Builder petAvoidanceBuilder = entity.getDb_Builder().getSpecialInfoBuilder().getPetAvoidanceBuilder();
            petAvoidanceBuilder.setChallengedTimes(petAvoidanceBuilder.getChallengedTimes() + 1);

            clBuilder.setTimes(petAvoidanceBuilder.getChallengedTimes());
            if (realScore > petAvoidanceBuilder.getMaxScore()) {
                petAvoidanceBuilder.setMaxScore(realScore);
                petAvoidanceBuilder.setMaxScoreTime(currentTime);
                return true;
            }
            return false;
        });
        if (updateRanking) {
            RankingManager.getInstance().updatePlayerRankingScore(playerIdx, EnumRankingType.ERT_PetAvoidance,
                    RankingUtils.getActivityRankingName(activity), realScore, currentTime);
        }

        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_PetAvoidance);
//        if (GlobalData.getInstance().checkPlayerOnline(playerIdx)) {
        RewardManager.getInstance().doRewardByList(playerIdx, activity.getDisplayRewardsList(), reason, true);
//        }else {
//            EventUtil.triggerAddMailEvent(playerIdx, MailTemplateUsed.getById(GameConst.CONFIG_ID).getBagfull(),
//                    rewardList, reason, RewardUtil.getBagFullName(playerIdx, rewardList));
//        }

        // 日志
        LogService.getInstance().submit(new PetAvoidanceGameSettleLog(playerIdx, gameTime, maxTime, score, realScore, clBuilder.getTimes()));
    }

    public boolean initScoreValidator() {
        try {
            this.scoreValidator = new ScoreValidator();
            return this.scoreValidator.init();
        }catch (Exception e) {
            LogUtil.error("PetAvoidanceGameManager.initScoreValidator err", e);
            return false;
        }
    }

    public void onPlayerLogin(String playerIdx) {
        // 重新登录 直接结束玩家的游戏
        PetAvoidanceGameData petAvoidanceGameData = removeGameData(playerIdx);
        if (petAvoidanceGameData != null) {
            SC_PetAvoidanceEnd.Builder clBuilder = SC_PetAvoidanceEnd.newBuilder();
            settle(petAvoidanceGameData, clBuilder);
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_PetAvoidanceEnd_VALUE, clBuilder);
        }
    }

    @Override
    public void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        List<PetAvoidanceGameData> needRemoveList = petAvoidanceDataMap.values().stream()
                .filter(petAvoidanceGameData -> petAvoidanceGameData.getEndTime() <= currentTime)
                .collect(Collectors.toList());

        for (PetAvoidanceGameData petAvoidanceGameData : needRemoveList) {
            PetAvoidanceGameData removed = removeGameData(petAvoidanceGameData.getPlayerIdx());
            if (removed != null) {
                Event event = Event.valueOf(EventType.ET_PetAvoidanceGameTimeOver, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
                event.pushParam(removed);
                EventManager.getInstance().dispatchEvent(event);
            }
        }
    }
}
