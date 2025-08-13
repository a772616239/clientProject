package model.foreignInvasion.newVersion;

import cfg.NewForeignInvasionBuildingsConfig;
import cfg.NewForeignInvasionBuildingsConfigObject;
import cfg.NewForeignInvasionConfig;
import cfg.NewForeignInvasionConfigObject;
import cfg.NewForeignInvasionRankingReward;
import cfg.ServerStringRes;
import com.google.protobuf.InvalidProtocolBufferException;
import common.GameConst;
import common.GameConst.EventType;
import common.GameConst.RankingName;
import common.GlobalData;
import common.entity.RankingQuerySingleResult;
import common.entity.WorldMapData;
import common.tick.GlobalTick;
import common.tick.Tickable;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import model.FunctionManager;
import model.activity.ActivityManager;
import model.foreignInvasion.dbCache.foreigninvasionCache;
import model.gameplay.GamePlayerUpdate;
import model.gameplay.dbCache.gameplayCache;
import model.gameplay.entity.gameplayEntity;
import model.player.dbCache.playerCache;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.ranking.settle.MailRankingSettleHandler;
import model.ranking.settle.RankingRewards;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.ActivityTime;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.ClientActivity;
import protocol.Activity.CycleTypeEnum;
import protocol.Activity.Cycle_Week;
import protocol.Activity.EnumRankingType;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.GameplayDB.DB_NewForeignInvasion;
import protocol.GameplayDB.DB_NewForeignInvasionBuildingInfo;
import protocol.GameplayDB.DB_NewForeignInvasionBuildingInfo.Builder;
import protocol.GameplayDB.GameplayTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.NewForeignInvasion.NewForeignInvasionBuildingInfo;
import protocol.NewForeignInvasion.NewForeignInvasionStatusEnum;
import protocol.NewForeignInvasion.SC_ClearNewForeignInvasionRanking;
import protocol.NewForeignInvasion.SC_RefreshNewForeignInvasionBuildingInfo;
import protocol.NewForeignInvasion.SC_RefreshNewForeignInvasionStatus;
import util.ArrayUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2020.11.09
 */
public class NewForeignInvasionManager implements Tickable, GamePlayerUpdate {
    private static NewForeignInvasionManager instance;

    public static NewForeignInvasionManager getInstance() {
        if (instance == null) {
            synchronized (NewForeignInvasionManager.class) {
                if (instance == null) {
                    instance = new NewForeignInvasionManager();
                }
            }
        }
        return instance;
    }

    private NewForeignInvasionManager() {
    }

    /**
     * 等级判断condition
     */
    private static final Predicate<String> LV_CONDITION =
            e -> PlayerUtil.queryFunctionUnlock(e, EnumFunction.NewForeignInvasion);


    private NewForeignInvasionConfigObject forInvCfg;

    private DB_NewForeignInvasion.Builder forInvGlobalInfo;

    private long beginTime;
    private long startTime;
    private long endTime;
    private long settleTime;
    private int petLv;
    private int mainlineNode;

    private long nextSendBuildingInfoTime;

    /**
     * 当前状态
     */
    private NewForeignInvasionStatusEnum status = NewForeignInvasionStatusEnum.NFISE_IDLE;

    /**
     * 发送当前阶段状态以及信息
     */
    private boolean sendCurStageStatusAndInfo;


//    private long nextRefreshRankingTime;

    /**
     * 参与活动的玩家
     */
    private final Set<String> joinPlayer = new ConcurrentSet<>();

    private long nextSendActivityRemainMarqueeTime;

    private long nextSendRankingTopMarqueeTime;

    public boolean init() {
        this.forInvCfg = NewForeignInvasionConfig.getById(GameConst.CONFIG_ID);
        this.forInvGlobalInfo = getDbNewForeignInvasionBuilder();
        checkBuildingInfo();
        return refreshForInvConfig()
                && GlobalTick.getInstance().addTick(this)
                && gameplayCache.getInstance().addToUpdateSet(this);
    }

    public DB_NewForeignInvasion.Builder getDbNewForeignInvasionBuilder() {
        DB_NewForeignInvasion.Builder result = null;
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_NewForeignInvasion);
        if (entity != null && entity.getGameplayinfo() != null) {
            try {
                result = DB_NewForeignInvasion.parseFrom(entity.getGameplayinfo()).toBuilder();
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }

        if (result == null) {
            result = DB_NewForeignInvasion.newBuilder();
        }
        return result;
    }

    private void checkBuildingInfo() {
        if (this.forInvGlobalInfo == null) {
            return;
        }
        initForeignInvasionWorldMapInfo();

        Map<Integer, DB_NewForeignInvasionBuildingInfo.Builder> oldBuildingInfoBuilderList
                = this.forInvGlobalInfo.getBuildingInfoBuilderList().stream()
                .collect(Collectors.toMap(Builder::getBuildingId, e -> e));

        List<DB_NewForeignInvasionBuildingInfo> newBuildingInfoBuilderList = new ArrayList<>();
        long cfgTime = TimeUtil.parseTime(this.forInvCfg.getRefreashtime());
        boolean needUpdateWave = cfgTime > this.forInvGlobalInfo.getLastReloadTime();
        LogUtil.info("NewForeignInvasionManager.checkBuildingInfo, updateTime:" + this.forInvCfg.getRefreashtime() + ", need update:" + needUpdateWave);
        for (NewForeignInvasionBuildingsConfigObject value : NewForeignInvasionBuildingsConfig._ix_buildingid.values()) {
            if (value.getBuildingid() <= 0) {
                continue;
            }

            DB_NewForeignInvasionBuildingInfo.Builder oldBuilder = oldBuildingInfoBuilderList.get(value.getBuildingid());
            if (oldBuilder != null) {
                if (needUpdateWave) {
                    oldBuilder.setTotalWave(value.getDefaultmonsterwave());
                    oldBuilder.setRemainWave(value.getDefaultmonsterwave());
                }
            } else {
                oldBuilder = DB_NewForeignInvasionBuildingInfo.newBuilder();
                oldBuilder.setBuildingId(value.getBuildingid());
                oldBuilder.setTotalWave(value.getDefaultmonsterwave());
                oldBuilder.setRemainWave(value.getDefaultmonsterwave());
            }
            newBuildingInfoBuilderList.add(oldBuilder.build());
            LogUtil.info("NewForeignInvasionManager.checkBuildingInfo, building id:" + oldBuilder.getBuildingId()
                    + ", total wave:" + oldBuilder.getTotalWave());
        }

        if (CollectionUtils.isNotEmpty(newBuildingInfoBuilderList)) {
            this.forInvGlobalInfo.clearBuildingInfo();
            this.forInvGlobalInfo.addAllBuildingInfo(newBuildingInfoBuilderList);
            this.forInvGlobalInfo.setLastReloadTime(cfgTime);
        }
    }

    private void initForeignInvasionWorldMapInfo() {
        if (this.forInvGlobalInfo.getPetLv() > 0) {
            this.petLv = this.forInvGlobalInfo.getPetLv();
            this.mainlineNode = this.forInvGlobalInfo.getMainlineNode();
            return;
        }
        if (playerCache.getInstance()._ix_id.size() <= 0) {
            return;
        }
        initDifficult();
    }

    /**
     * 以怪物击杀个数减少
     *
     * @param buildingId
     * @param killCount
     */
    public synchronized void killMonster(String playerIdx, int buildingId, int killCount) {
        recordJoinPlayer(playerIdx);

        DB_NewForeignInvasionBuildingInfo.Builder targetBuilder = null;
        for (DB_NewForeignInvasionBuildingInfo.Builder builder : forInvGlobalInfo.getBuildingInfoBuilderList()) {
            if (builder.getBuildingId() == buildingId) {
                targetBuilder = builder;
                break;
            }
        }

        if (targetBuilder == null) {
            LogUtil.info("model.foreigninvasion.NewForeignInvasionManager.killOneWaveMonster, building info is not exist, building id:" + buildingId);
            NewForeignInvasionBuildingsConfigObject cfg = NewForeignInvasionBuildingsConfig.getByBuildingid(buildingId);
            if (cfg == null) {
                LogUtil.error("model.foreigninvasion.NewForeignInvasionManager.killOneWaveMonster," +
                        " building cfg is not exist, building id:" + buildingId);
                return;
            }
            targetBuilder = DB_NewForeignInvasionBuildingInfo.newBuilder()
                    .setBuildingId(buildingId)
                    .setTotalWave(cfg.getDefaultmonsterwave())
                    .setRemainWave(cfg.getDefaultmonsterwave());

        }
        if (targetBuilder.getRemainWave() <= 0) {
            return;
        }

        targetBuilder.setRemainWave(Math.max(0, targetBuilder.getRemainWave() - killCount));
        if (targetBuilder.getRemainWave() <= 0) {
            sendBuildingInfoMsg();
            sendBuildingFreeMarquee(buildingId);
        }
    }

    private void recordJoinPlayer(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        this.joinPlayer.add(playerIdx);
    }

    @Override
    public void onTick() {
        if (FunctionManager.getInstance().functionClosed(EnumFunction.NewForeignInvasion)) {
            return;
        }
        statusTick();
        tick();
    }

    private void statusTick() {
        NewForeignInvasionStatusEnum newStatus;

        long nowTime = GlobalTick.getInstance().getCurrentTime();
        if (nowTime < beginTime) {
            newStatus = NewForeignInvasionStatusEnum.NFISE_IDLE;
        } else if (nowTime < startTime) {
            newStatus = NewForeignInvasionStatusEnum.NFISE_PREPARE;
        } else if (nowTime < endTime) {
            newStatus = NewForeignInvasionStatusEnum.NFISE_OPEN;
        } else {
            newStatus = NewForeignInvasionStatusEnum.NFISE_SETTLE;
        }

        setStatus(newStatus);
    }

    private void tick() {
        //所有阶段都需要发送状态
        if (!sendCurStageStatusAndInfo) {
            sendStatusMsg();
        }

        long curTime = GlobalTick.getInstance().getCurrentTime();
        switch (this.status) {
            case NFISE_IDLE:
                //do no thing
                break;
            case NFISE_PREPARE:
                if (!sendCurStageStatusAndInfo) {
                    initDifficult();
                    sendRankingMsg();
                    sendAllAllowPlayerBuildingInfo();
                    sendBuildingInfoMsg();
                }
                break;
            case NFISE_OPEN:
                if (!sendCurStageStatusAndInfo) {
                    clearRanking();
                }

                sendActivityRemainTimeMarquee();
                sendTopRankingMarquee();
                refreshBuildingInfo();
                sendRankingMsg();

                if (finishedFreeAllBuilding()) {
                    this.endTime = curTime;
                    this.settleTime = curTime + forInvCfg.getSettledelaytime() * TimeUtil.MS_IN_A_MIN;
                }
                break;
            case NFISE_SETTLE:
                if (!sendCurStageStatusAndInfo) {
                    sendFreeConditionMarquee();
                }

                if (curTime > this.settleTime) {
                    sendRankingMsg();
                    settleRewards();
                    clearAllPlayerForeignInvasionInfo();
                    resetBuildingInfo();
                    refreshForInvConfig();
                }
                break;
            default:
                break;
        }
        sendCurStageStatusAndInfo = true;
    }

    private void initDifficult() {
        WorldMapData worldMapInfo = GlobalData.getInstance().getWorldMapInfo();
        this.petLv = worldMapInfo.getPetLv();
        this.mainlineNode = worldMapInfo.getMainlineNode();
        this.forInvGlobalInfo.setPetLv(petLv);
        this.forInvGlobalInfo.setMainlineNode(mainlineNode);
        LogUtil.info("init newForeignInvasionManager petLv:{},mainlineNode:{}", petLv, mainlineNode);
    }

    private void sendFreeConditionMarquee() {
        boolean freeAll = true;
        for (DB_NewForeignInvasionBuildingInfo buildingInfo : this.forInvGlobalInfo.getBuildingInfoList()) {
            if (buildingInfo.getRemainWave() > 0) {
                freeAll = false;
                break;
            }
        }

        if (freeAll) {
            GlobalData.getInstance().sendMarqueeToAllSatisfyOnlinePlayer(forInvCfg.getFreeallmarquee(), LV_CONDITION);
        } else {
            GlobalData.getInstance().sendMarqueeToAllSatisfyOnlinePlayer(forInvCfg.getNotfreeallmarquee(), LV_CONDITION);
        }
    }

    private void refreshBuildingInfo() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime >= this.nextSendBuildingInfoTime) {
            sendBuildingInfoMsg();
            this.nextSendBuildingInfoTime = currentTime + forInvCfg.getBuildingrefreshinterval() * TimeUtil.MS_IN_A_S;
        }
    }

    private void clearAllPlayerForeignInvasionInfo() {
        foreigninvasionCache.getInstance().clearAllPlayerBuildingsInfo();
    }

    /**
     * 发送玩家的建筑波次信息
     */
    private void sendPlayerBuildingWaveInfo(Collection<String> playerIdxColl) {
        EventUtil.unlockObjEvent(EventType.ET_NewForeignInvasionSendPlayerBuildingInfo, playerIdxColl);
    }

    private void sendAllAllowPlayerBuildingInfo() {
        List<String> collect = GlobalData.getInstance().getAllOnlinePlayerIdx().stream()
                .filter(e -> PlayerUtil.queryFunctionUnlock(e, EnumFunction.NewForeignInvasion))
                .collect(Collectors.toList());
        sendPlayerBuildingWaveInfo(collect);
    }


    public boolean playerCanJoin(String playerIdx) {
        return PlayerUtil.queryFunctionUnlock(playerIdx, EnumFunction.NewForeignInvasion);
    }

    public void onPlayerLogIn(String playerIdx) {
        if (FunctionManager.getInstance().functionClosed(EnumFunction.NewForeignInvasion)
                || !playerCanJoin(playerIdx)) {
            return;
        }

//        if (this.status != NewForeignInvasionStatusEnum.NFISE_IDLE) {
//            sendRankingInfoToPlayer(playerIdx);
//        }

        if (this.status == NewForeignInvasionStatusEnum.NFISE_IDLE) {
            //do no things
        } else if (this.status == NewForeignInvasionStatusEnum.NFISE_PREPARE) {
            sendBuildingInfoToPlayer(playerIdx);
            sendPlayerBuildingWaveInfo(Collections.singletonList(playerIdx));
        } else if (this.status == NewForeignInvasionStatusEnum.NFISE_OPEN) {
            sendBuildingInfoToPlayer(playerIdx);
            sendPlayerBuildingWaveInfo(Collections.singletonList(playerIdx));
        } else if (this.status == NewForeignInvasionStatusEnum.NFISE_SETTLE) {
            //do no thing
        }

        //状态发送放在最后
        sendStatusMsgToPlayer(playerIdx);
    }

    private void sendStatusMsgToPlayer(String playerIdx) {
        GlobalData.getInstance().sendMsg(playerIdx,
                MsgIdEnum.SC_RefreshNewForeignInvasionStatus_VALUE, buildStatusMsg());
    }

    private void sendBuildingInfoToPlayer(String playerIdx) {
        GlobalData.getInstance().sendMsg(playerIdx,
                MsgIdEnum.SC_RefreshNewForeignInvasionBuildingInfo_VALUE, buildBuildingInfo());
    }

//    public void sendRankingInfoToPlayer(String playerIdx) {
//        RankingManager.getInstance().sendRankingInfoToPlayer(EnumRankingSenderType.ERST_NewForeignInvasion,
//                EnumRankingType.ERT_NewForeignInvasion, RankingName.RN_New_ForInv_Score, playerIdx);
//    }

    /**
     * 结算奖励
     */
    private void settleRewards() {
        settleRankingRewards();
        settleBuildingFreeRewards();
    }

    private synchronized void resetBuildingInfo() {
        this.forInvGlobalInfo.setLastSettleTime(GlobalTick.getInstance().getCurrentTime());
        this.forInvGlobalInfo.getBuildingInfoBuilderList().forEach(
                e -> {
                    NewForeignInvasionBuildingsConfigObject buildingCfg = NewForeignInvasionBuildingsConfig.getByBuildingid(e.getBuildingId());
                    if (buildingCfg == null) {
                        LogUtil.error("model.foreignInvasion.newVersion.NewForeignInvasionManager.resetBuildingInfo, building cfg is not exist, buildingId:" + e.getBuildingId());
                        return;
                    }
                    //添加解放情况
                    boolean curFree = e.getRemainWave() <= 0;
                    LogUtil.info("model.foreignInvasion.newVersion.NewForeignInvasionManager.resetBuildingInfo," +
                            " building id:" + e.getBuildingId() + ",free condition:" + curFree);

                    if (e.getFreeConditionCount() > 0
                            && e.getFreeCondition(e.getFreeConditionCount() - 1) != curFree) {
                        e.clearFreeCondition();
                    }
                    e.addFreeCondition(curFree);

                    if (e.getFreeConditionCount() >= buildingCfg.getWavechangesneedcount()) {
                        int newWave = e.getTotalWave();
                        boolean increase = e.getFreeCondition(0);
                        if (increase) {
                            newWave = (newWave * (100 + buildingCfg.getRiseratio())) / 100;
                        } else {
                            newWave = (newWave * (100 - buildingCfg.getLowerratio())) / 100;
                        }
                        e.setTotalWave(Math.max(newWave, buildingCfg.getWavelowerlimit()));
                        e.clearFreeCondition();

                        LogUtil.info("NewForeignInvasionManager.resetBuildingInfo, buildingId:" + e.getBuildingId()
                                + ", increase:" + increase + ", new wave:" + e.getTotalWave());
                    }

                    e.setRemainWave(e.getTotalWave());
                }
        );
    }

    /**
     * 建筑解放奖励,只发放参与活动的玩家
     */
    private void settleBuildingFreeRewards() {
        List<Integer> freeBuildings = this.forInvGlobalInfo.getBuildingInfoList().stream()
                .filter(e -> e.getRemainWave() <= 0)
                .map(DB_NewForeignInvasionBuildingInfo::getBuildingId)
                .collect(Collectors.toList());

        LogUtil.info("model.foreigninvasion.NewForeignInvasionManager.settleBuildingFreeRewards, free building:"
                + GameUtil.collectionToString(freeBuildings) + ", join player size:" + joinPlayer.size());

        //单个建筑的解放奖励
        for (Integer freeBuilding : freeBuildings) {
            NewForeignInvasionBuildingsConfigObject buildingCfg = NewForeignInvasionBuildingsConfig.getByBuildingid(freeBuilding);
            if (buildingCfg == null) {
                LogUtil.error("model.foreigninvasion.NewForeignInvasionManager.settleBuildingFreeRewards," +
                        " building id cfg is not exist:" + freeBuilding);
                continue;
            }

            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(buildingCfg.getFreerewards());
            if (CollectionUtils.isEmpty(rewards)) {
                LogUtil.error("model.foreigninvasion.NewForeignInvasionManager.settleBuildingFreeRewards," +
                        " building free reward cfg error:" + freeBuilding);
                continue;
            }

            addMailByPlayerIdxList(joinPlayer, buildingCfg.getFreemailtemplate(), rewards);
            LogUtil.info("model.foreigninvasion.NewForeignInvasionManager.settleBuildingFreeRewards," +
                    " finished settle building free rewards, buildingId:" + freeBuilding);
        }

        //全建筑解放奖励
        if (freeBuildings.size() >= NewForeignInvasionBuildingsConfig._ix_buildingid.size()) {
            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(forInvCfg.getFreeallrewrds());
            if (CollectionUtils.isEmpty(rewards)) {
                LogUtil.error("model.foreigninvasion.NewForeignInvasionManager.settleBuildingFreeRewards," +
                        " free all building rewards cfg error");
                return;
            }

            addMailByPlayerIdxList(joinPlayer, forInvCfg.getFreeallmailtemplate(), rewards);
            LogUtil.info("model.foreigninvasion.NewForeignInvasionManager.settleBuildingFreeRewards," +
                    " finished settle free all building rewards,");
        }
    }

    public void addMailByPlayerIdxList(Collection<String> playerIdxList, int mailTemplateId, List<Reward> rewards) {
        if (CollectionUtils.isEmpty(joinPlayer)) {
            return;
        }
        playerIdxList.forEach(e -> {
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ForeignInvasion);
            EventUtil.triggerAddMailEvent(e, mailTemplateId, rewards, reason);
        });
    }

    private void settleRankingRewards() {
        List<RankingRewards> rewardsList = NewForeignInvasionRankingReward.getInstance().getRankingRewardsList();
        MailRankingSettleHandler handler = new MailRankingSettleHandler(EnumRankingType.ERT_NewForeignInvasion, rewardsList,
                forInvCfg.getRankingmailtemplate(), RewardSourceEnum.RSE_ForeignInvasion);
        handler.settleRanking();
        LogUtil.info("model.foreigninvasion.NewForeignInvasionManager.settleRankingRewards, finished settle ranking rewards");
    }

    /**
     * 是否已经完成解放所有建筑
     *
     * @return
     */
    private synchronized boolean finishedFreeAllBuilding() {
        for (DB_NewForeignInvasionBuildingInfo info : forInvGlobalInfo.getBuildingInfoList()) {
            if (info.getRemainWave() > 0) {
                return false;
            }
        }
        return true;
    }

    private synchronized void setStatus(NewForeignInvasionStatusEnum status) {
        if (this.status != status) {
            sendCurStageStatusAndInfo = false;
            this.status = status;
        }
    }

    /**
     * 客户端主动请求排行榜数据,不在主动推送
     */
    private void sendRankingMsg() {
//        long currentTime = GlobalTick.getInstance().getCurrentTime();
//        if (currentTime < nextRefreshRankingTime) {
//            return;
//        }
//
//        for (String playerIdx : GlobalData.getInstance().getAllOnlinePlayerIdx()) {
//            if (!LV_CONDITION.test(playerIdx)) {
//                continue;
//            }
//            sendRankingInfoToPlayer(playerIdx);
//        }
//        this.nextRefreshRankingTime = currentTime + forInvCfg.getRankingrefreshinterval() * TimeUtil.MS_IN_A_S;
    }

    private SC_RefreshNewForeignInvasionBuildingInfo.Builder buildBuildingInfo() {
        SC_RefreshNewForeignInvasionBuildingInfo.Builder builder = SC_RefreshNewForeignInvasionBuildingInfo.newBuilder();
        for (DB_NewForeignInvasionBuildingInfo value : this.forInvGlobalInfo.getBuildingInfoList()) {
            NewForeignInvasionBuildingInfo.Builder buildingInfoBuilder = NewForeignInvasionBuildingInfo.newBuilder();
            buildingInfoBuilder.setBuildingId(value.getBuildingId());
            buildingInfoBuilder.setRemainWave(value.getRemainWave());
            buildingInfoBuilder.setTotalWave(value.getTotalWave());
            builder.addBuildingInfo(buildingInfoBuilder);
        }
        return builder;
    }

    private void sendBuildingInfoMsg() {
        GlobalData.getInstance().sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum.SC_RefreshNewForeignInvasionBuildingInfo,
                buildBuildingInfo(), LV_CONDITION);
    }

    private void sendStatusMsg() {
        GlobalData.getInstance().sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum.SC_RefreshNewForeignInvasionStatus,
                buildStatusMsg(), LV_CONDITION);
    }

    private SC_RefreshNewForeignInvasionStatus.Builder buildStatusMsg() {
        SC_RefreshNewForeignInvasionStatus.Builder builder = SC_RefreshNewForeignInvasionStatus.newBuilder();
        builder.setNewStatus(this.status);
        if (this.status == NewForeignInvasionStatusEnum.NFISE_PREPARE) {
            builder.setCurStatusStartTime(this.beginTime);
            builder.setCurStatusExpireTime(this.startTime);
        } else if (this.status == NewForeignInvasionStatusEnum.NFISE_OPEN) {
            builder.setCurStatusStartTime(this.startTime);
            builder.setCurStatusExpireTime(this.endTime);
        } else if (this.status == NewForeignInvasionStatusEnum.NFISE_SETTLE) {
            builder.setCurStatusStartTime(this.endTime);
            builder.setCurStatusExpireTime(this.settleTime);
        }
        return builder;
    }

    private void clearRanking() {
        RankingManager.getInstance().clearRanking(EnumRankingType.ERT_NewForeignInvasion);
        GlobalData.getInstance().sendMsgToAllSatisfyOnlinePlayer(MsgIdEnum.SC_ClearNewForeignInvasionRanking
                , SC_ClearNewForeignInvasionRanking.newBuilder(), LV_CONDITION);
    }


    private synchronized void clear() {
        this.beginTime = 0;
        this.startTime = 0;
        this.endTime = 0;
        this.settleTime = 0;

        this.joinPlayer.clear();
    }

    /**
     * 刷新外敌入侵玩法配置
     *
     * @return
     */
    private synchronized boolean refreshForInvConfig() {
        clear();

        long nextOpenDayStamp = getNextOpenDayStamp();
        long todayStamp = TimeUtil.getTodayStamp(nextOpenDayStamp);
        //准备开始时间
        this.beginTime = todayStamp + forInvCfg.getBegintime() * TimeUtil.MS_IN_A_MIN;
        //开始时间
        this.startTime = todayStamp + forInvCfg.getStarttime() * TimeUtil.MS_IN_A_MIN;
        //结束时间
        this.endTime = startTime + forInvCfg.getOpentime() * TimeUtil.MS_IN_A_MIN;
        //结算时间
        this.settleTime = endTime + forInvCfg.getSettledelaytime() * TimeUtil.MS_IN_A_MIN;

        updateActivity();
        LogUtil.info("NewForeignInvasionManager.refreshForInvConfig, refresh finished, next open time:" + this.beginTime);
        return true;
    }

    public long getNextOpenDayStamp() {
        int[] openDayArr = this.forInvCfg.getOpenday();
        long curTime = GlobalTick.getInstance().getCurrentTime();

        long lastSettleTime = this.forInvGlobalInfo.getLastSettleTime();

        long todayStamp = TimeUtil.getTodayStamp(curTime);
        long todayBeginTime = todayStamp + forInvCfg.getBegintime() * TimeUtil.MS_IN_A_MIN;
        long todayEndTime = todayStamp + (forInvCfg.getStarttime() + forInvCfg.getOpentime()) * TimeUtil.MS_IN_A_MIN;

        //如果当前时间在开放日且上次结算时间在当前开放时间之前
        int todayOfWeek = TimeUtil.getDayOfWeek(curTime);
        if (ArrayUtil.intArrayContain(openDayArr, todayOfWeek)
                && lastSettleTime < todayBeginTime
                && todayEndTime > curTime) {
            return todayStamp;
        }

        int nextOpenDay = NewForeignInvasionUtil.getNextOpenDay(openDayArr, todayOfWeek);
        return TimeUtil.getNextDayInWeekTime(Math.max(curTime, lastSettleTime), nextOpenDay);
    }

    public void updateActivity() {
        ClientActivity.Builder builder = ClientActivity.newBuilder();
        builder.setActivityType(ActivityTypeEnum.ATE_ForInv);
        ActivityTime.Builder timeBuilder = ActivityTime.newBuilder();

        Cycle_Week.Builder cycle = Cycle_Week.newBuilder();
        for (int dayOfWeek : this.forInvCfg.getOpenday()) {
            cycle.addDayOfWeekList(dayOfWeek);
        }
        cycle.setStartOfDay(TimeUtil.getMin(this.startTime));
        cycle.setEndOfDay(TimeUtil.getMin(this.settleTime));

        timeBuilder.setTimeType(CycleTypeEnum.CTE_Week);
        timeBuilder.setTimeContent(cycle.build().toByteString());
        builder.setCycleTime(timeBuilder);
        //帮助客户端处理
//        builder.setDetail(ServerStringRes.buildLanguageNumContentJson(this.forInvCfg.getHelp()));
        ActivityManager.getInstance().addSpecialActivity(builder.build());
    }

    @Override
    public void update() {
        gameplayEntity entity = gameplayCache.getInstance().getByGamePlayType(GameplayTypeEnum.GTE_NewForeignInvasion);
        entity.setGameplayinfo(this.forInvGlobalInfo.build().toByteArray());
        gameplayCache.put(entity);
    }


    public NewForeignInvasionStatusEnum getCurStatus() {
        return this.status;
    }

    public int getRemainWave(int buildingId) {
        for (DB_NewForeignInvasionBuildingInfo info : this.forInvGlobalInfo.getBuildingInfoList()) {
            if (info.getBuildingId() == buildingId) {
                return info.getRemainWave();
            }
        }
        return 0;
    }

    public long getNextOpenDayCurStamp(long curTime) {
        int todayOfWeek = TimeUtil.getDayOfWeek(curTime);
        int[] openDayArr = forInvCfg.getOpenday();
        return TimeUtil.getNextDayInWeekTime(curTime, NewForeignInvasionUtil.getNextOpenDay(openDayArr, todayOfWeek));
    }

    /**
     * 外敌入侵倒计时跑马灯,只在活动进行阶段发送
     */
    private synchronized void sendActivityRemainTimeMarquee() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        long remainMin = (this.endTime - currentTime) / TimeUtil.MS_IN_A_MIN;
        if (currentTime > this.nextSendActivityRemainMarqueeTime) {
            GlobalData.getInstance().sendMarqueeToAllSatisfyOnlinePlayer(forInvCfg.getCountdownmarquee(), LV_CONDITION, String.valueOf(remainMin));
            this.nextSendActivityRemainMarqueeTime = currentTime + forInvCfg.getCountdowninterval() * TimeUtil.MS_IN_A_S;
        }
    }

    /**
     * 玩家排名跑马灯
     */
    public void sendPlayerRankingMarquee(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return;
        }
        RankingQuerySingleResult result = RankingManager.getInstance().getPlayerRankingResult(RankingName.RN_New_ForInv_Score, playerIdx);
        if (result == null) {
            return;
        }
        GlobalData.getInstance().sendMarqueeToPlayer(playerIdx, forInvCfg.getPlayerrankingmarquee(),
                PlayerUtil.queryPlayerName(playerIdx), result.getIntPrimaryScore(), result.getRanking());
    }

    private void sendTopRankingMarquee() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (currentTime > this.nextSendRankingTopMarqueeTime) {
            RankingQuerySingleResult ranking = RankingManager.getInstance().getPlayerInfoByRanking(EnumRankingType.ERT_NewForeignInvasion, 1);
            if (ranking != null) {
                GlobalData.getInstance().sendMarqueeToAllSatisfyOnlinePlayer(forInvCfg.getFirstrankingmarquee(), LV_CONDITION,
                        PlayerUtil.queryPlayerName(ranking.getPrimaryKey()), ranking.getPrimaryScore());
            }
            this.nextSendRankingTopMarqueeTime = currentTime + forInvCfg.getFirstrankinginterval() * TimeUtil.MS_IN_A_S;
        }
    }

    private void sendBuildingFreeMarquee(int buildingId) {
        NewForeignInvasionBuildingsConfigObject buildingCfg = NewForeignInvasionBuildingsConfig.getByBuildingid(buildingId);
        if (buildingCfg == null) {
            return;
        }
        Map<Integer, String> nameMap = ServerStringRes.buildLanguageContentMap(buildingCfg.getBuildingname());
        for (String onlinePlayerIdx : GlobalData.getInstance().getAllOnlinePlayerIdx()) {
            if (PlayerUtil.queryFunctionLock(onlinePlayerIdx, EnumFunction.NewForeignInvasion)) {
                continue;
            }
            String playerBuildingName = nameMap.get(PlayerUtil.queryPlayerLanguage(onlinePlayerIdx).getNumber());
            if (playerBuildingName == null) {
                playerBuildingName = "";
            }
            GlobalData.getInstance().sendMarqueeToPlayer(onlinePlayerIdx, forInvCfg.getBuildingfreemarquee(), playerBuildingName);
        }
    }

    public void gmOpen(int n) {
        clear();
        long timeCur = System.currentTimeMillis();
        //准备开始时间
        this.beginTime = timeCur;
        //开始时间
        this.startTime = timeCur + 1 * TimeUtil.MS_IN_A_MIN;
        //结束时间
        this.endTime = startTime + n * TimeUtil.MS_IN_A_MIN;
        //结算时间
        this.settleTime = endTime + 1 * TimeUtil.MS_IN_A_MIN;
        updateActivity();
    }

    public int getPetLv() {
        return this.petLv;
    }

    public int getMainlineNode() {
        return this.mainlineNode;
    }
}
