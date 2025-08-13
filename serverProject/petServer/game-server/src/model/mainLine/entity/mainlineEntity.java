/**
 * created by tool DAOGenerate
 */
package model.mainLine.entity;

import cfg.GameConfig;
import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointObject;
import cfg.MainLineEpisodeConfig;
import cfg.MainLineEpisodeNodeConfig;
import cfg.MainLineEpisodeNodeConfigObject;
import cfg.MainLineNode;
import cfg.MainLineNodeObject;
import cfg.MainLineNodeOpenEpisode;
import cfg.MainLineNodeOpenEpisodeObject;
import cfg.PointCopyOpenTimeObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.GameConst;
import common.GameConst.RankingName;
import common.GlobalData;
import common.IdGenerator;
import common.tick.GlobalTick;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import model.activity.ActivityManager;
import model.activity.PointCopyManager;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.util.MainLineUtil;
import model.mission.MissionManager;
import model.obj.BaseObj;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import platform.logs.ReasonManager;
import protocol.Activity.EnumRankingType;
import protocol.Common;
import protocol.Common.RandomReward;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.MainLine;
import protocol.MainLine.MainLineProgress;
import protocol.MainLine.PswMap;
import protocol.MainLine.SC_RefreashMainLine;
import protocol.MainLine.SC_RefreshMainLineInfo;
import protocol.MainLineDB.DB_MainLine;
import protocol.MainLineDB.DB_MainLine.Builder;
import protocol.MainLineDB.DB_OnHookIncome;
import protocol.MessageId.MsgIdEnum;
import protocol.Server.DropResourceEnum;
import protocol.TargetSystem;
import util.ArrayUtil;
import util.LogUtil;
import util.TimeUtil;

import static protocol.MessageId.MsgIdEnum.SC_KeyNodeMissionInfo_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_RefreshEpisode_VALUE;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class mainlineEntity extends BaseObj {

    public String getClassType() {
        return "mainlineEntity";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private String linkplayeridx;

    /**
     *
     */
    private byte[] mainlinedata;


    /**
     * 获得
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得
     */
    public String getLinkplayeridx() {
        return linkplayeridx;
    }

    /**
     * 设置
     */
    public void setLinkplayeridx(String linkplayeridx) {
        this.linkplayeridx = linkplayeridx;
    }

    /**
     * 获得
     */
    public byte[] getMainlinedata() {
        return mainlinedata;
    }

    /**
     * 设置
     */
    public void setMainlinedata(byte[] mainlinedata) {
        this.mainlinedata = mainlinedata;
    }


    public String getBaseIdx() {
        return idx;
    }

    private mainlineEntity() {
    }

    /**
     * =================================================
     */

    @Override
    public void putToCache() {
        mainlineCache.put(this);
    }

    private DB_MainLine.Builder db_data;

    public void clearDBData() {
        db_data = DB_MainLine.newBuilder();
    }

    public mainlineEntity(String playerIdx) {
        this.idx = IdGenerator.getInstance().generateId();
        this.linkplayeridx = playerIdx;
        getDBBuilder();
        transformDBData();
    }


    public DB_MainLine.Builder getDBBuilder() {
        if (db_data == null) {
            db_data = getDBMainLine();
        }
        return db_data;
    }

    private DB_MainLine.Builder getDBMainLine() {
        try {
            if (!ArrayUtils.isEmpty(this.mainlinedata)) {
                return DB_MainLine.parseFrom(mainlinedata).toBuilder();
            } else {
                LogUtil.info("playerIdx:{} create new mainlineBuilder", getLinkplayeridx());
                return DB_MainLine.newBuilder().setKeyNodeId(1);
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    @Override
    public void transformDBData() {
        this.mainlinedata = getDBBuilder().build().toByteArray();
        if (this.mainlinedata == null || this.mainlinedata.length == 0) {
            LogUtil.error("this.mainlinedata is empty ");
        }
    }

    public MainLineProgress.Builder getMainLineProBuilder() {
        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            LogUtil.error("playerIdx[" + linkplayeridx + "] mainlineDbData is null");
            return null;
        }
        return dbBuilder.getMainLineProBuilder();
    }

    /**
     * 主线闯关，通过当前关卡，
     */
    public void passCurCheckPoint() {
        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            LogUtil.error("playerIdx[" + getLinkplayeridx() + "] dbData is null");
            return;
        }

        MainLineProgress.Builder mainLineProBuilder = dbBuilder.getMainLineProBuilder();
        if (mainLineProBuilder == null) {
            return;
        }

        int curCheckPoint = mainLineProBuilder.getCurCheckPoint();
        mainLineProBuilder.setAlreadyPassed(curCheckPoint);
        //设置通关时间
        dbBuilder.setLastPassedTime(GlobalTick.getInstance().getCurrentTime());
        //清空传送节点
        mainLineProBuilder.clearLastTransferNode();

        mainLineProBuilder.clearUnlockNodes();
        mainLineProBuilder.clearProgress();

        //检查下一个关卡是否解锁
        checkNextPointUnlock();
        updateMistLv(curCheckPoint);
    }

    /**
     * 更新迷雾深林解锁层级
     *
     * @param curCheckPoint
     */
    private void updateMistLv(int curCheckPoint) {
        MainLineCheckPointObject curCfg = MainLineCheckPoint.getById(curCheckPoint);
        if (curCfg == null) {
            return;
        }

        playerEntity player = playerCache.getByIdx(getLinkplayeridx());
        if (player == null) {
            return;
        }

//        Event event = Event.valueOf(EventType.ET_UPDATE_UNLOCK_MIST_LV, this, player);
//        event.pushParam(curCfg.getUnlockmistlv());
//        EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * 计算切换前收益，切换挂机关卡
     */
    public synchronized void changeOnHookNode(int curNode) {

        Builder db_data = getDBBuilder();
        if (db_data == null) {
            LogUtil.error("playerIdx[" + linkplayeridx + "] mainlineDbData is null");
            return;
        }

        DB_OnHookIncome.Builder onHookBuilder = db_data.getOnHookIncomeBuilder();

        if (onHookBuilder.getCurOnHookNode() == curNode) {
            return;
        }
        if (MainLineNode.getById(curNode) == null) {
            return;
        }

        long curTime = GlobalTick.getInstance().getCurrentTime();

        //当前没有挂机关卡,初始化挂机信息
        if (MainLineNode.getById(onHookBuilder.getCurOnHookNode()) == null) {
            onHookBuilder.setStartOnHookTime(curTime);
            onHookBuilder.setLastSettleTime(curTime);
        }

        calculateOnHookReward();
        onHookBuilder.setCurOnHookNode(curNode);
    }

    /**
     * 根据配置解锁对应Node
     *
     * @param unlockIntArr
     * @param needClear    是否清空已经解锁的unlockNodeList
     * @param removeNode   需要移除的nodeId
     */
    public synchronized void addUnlockNode(int[] unlockIntArr, boolean needClear, Integer... removeNode) {
        if (unlockIntArr == null || unlockIntArr.length <= 0) {
            return;
        }

        MainLineProgress.Builder mainLineProBuilder = getMainLineProBuilder();
        if (mainLineProBuilder == null) {
            return;
        }

        ArrayList<Integer> unlock = new ArrayList<>(mainLineProBuilder.getUnlockNodesList());
        if (needClear) {
            unlock.clear();
        }

        for (int node : unlockIntArr) {
            if (!unlock.contains(node)) {
                unlock.add(node);
            }
        }

        if (removeNode != null && removeNode.length > 0) {
            for (Integer node : removeNode) {
                unlock.remove(node);
            }
        }

        mainLineProBuilder.clearUnlockNodes();
        mainLineProBuilder.addAllUnlockNodes(unlock);
    }

    /**
     * 发送刷新的信息
     */
    public void sendRefreshMainLineMsg() {
        DB_MainLine.Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            LogUtil.error("playerIdx[" + getIdx() + "] dbData is null");
            return;
        }

        SC_RefreashMainLine.Builder builder = SC_RefreashMainLine.newBuilder();
        builder.setMainLinePro(dbBuilder.getMainLinePro());
        builder.setCurOnHookNode(dbBuilder.getOnHookIncome().getCurOnHookNode());
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreashMainLine_VALUE, builder);
    }

    /**
     * 检查是否有可以解锁的关卡
     *
     * @return 是否解锁了新的关卡
     */
    public boolean checkPointUnlock() {
        MainLineProgress.Builder mainLineBuilder = getMainLineProBuilder();
        if (mainLineBuilder == null) {
            LogUtil.error("playerIdx[" + linkplayeridx + "] mainLineDbData is null");
            return false;
        }

        int curCheckPoint = mainLineBuilder.getCurCheckPoint();
        if (curCheckPoint == 0) {
            LogUtil.info("playerIdx:{} mainlineEntity checkPointUnlock curCheckPoint is zero ,do mainlineBuilder clear", getLinkplayeridx());
            mainLineBuilder.clear();
            MainLineCheckPointObject checkPointCfg = MainLineCheckPoint.getById(1);
            if (checkPointCfg != null && PlayerUtil.queryPlayerLv(getLinkplayeridx()) >= checkPointCfg.getUnlocklv()) {
                mainLineBuilder.setCurCheckPoint(1);
                int[] nodeList = checkPointCfg.getNodelist();
                if (nodeList != null) {
                    for (int i : nodeList) {
                        mainLineBuilder.clearUnlockNodes();
                        mainLineBuilder.addUnlockNodes(i);
                    }
                    return true;
                }
            }
        } else {
            return checkNextPointUnlock();
        }
        return false;
    }

    /**
     * 检查下一个关卡是否解锁,并添加解锁节点
     *
     * @return 是否解锁了新的关卡
     */
    private boolean checkNextPointUnlock() {
        MainLineProgress.Builder mainLineBuilder = getMainLineProBuilder();
        if (mainLineBuilder == null) {
            LogUtil.error("playerIdx[" + linkplayeridx + "] mainLineDbData is null");
            return false;
        }

        int curCheckPoint = mainLineBuilder.getCurCheckPoint();

        if (mainLineBuilder.getAlreadyPassed() == curCheckPoint) {
            MainLineCheckPointObject checkPointCfg = MainLineCheckPoint.getById(curCheckPoint);
            if (checkPointCfg != null) {
                int nextPoint = checkPointCfg.getAftercheckpoint();
                if (MainLineCheckPoint.getById(nextPoint) == null) {
                    LogUtil.warn("MainLineCheckPoint = " + nextPoint + ", is null");
                    return true;
                }

//                if (!MainLineUtil.CheckPointIsUnlock(PlayerUtil.queryPlayerLv(getLinkplayeridx()), nextPoint)) {
//                    return true;
//                }
                mainLineBuilder.setCurCheckPoint(nextPoint);

                List<Integer> unlock = new ArrayList<>();
                List<Integer> passed = new ArrayList<>();

                getPointUnlockNodeList(nextPoint, unlock, passed);

                mainLineBuilder.clearUnlockNodes();
                if (!unlock.isEmpty()) {
                    mainLineBuilder.addAllUnlockNodes(unlock);
                }

                mainLineBuilder.clearProgress();
                if (!passed.isEmpty()) {
                    mainLineBuilder.addAllProgress(passed);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 当更换关卡时，刷新当前关卡可解锁的所有关卡
     * 根据上一关来确定本关解锁的节点,
     *
     * @param curCheckPoint
     * @param passed        当为传送节点时,且为空白节点时,直接通关
     * @return
     */
    public void getPointUnlockNodeList(int curCheckPoint, List<Integer> unlock, List<Integer> passed) {
        MainLineCheckPointObject curCfg = MainLineCheckPoint.getById(curCheckPoint);
        if (curCfg == null || unlock == null || passed == null) {
            return;
        }

        MainLineCheckPointObject lastCheckPoint = MainLineCheckPoint.getById(curCfg.getBeforecheckpoint());
        if (lastCheckPoint == null) {
            return;
        }

        int[] lastNodeList = lastCheckPoint.getNodelist();
        if (lastNodeList == null || lastNodeList.length <= 0) {
            return;
        }

        unlock.clear();
        passed.clear();

        List<Integer> unlock_part = new ArrayList<>();

        for (int i : lastNodeList) {
            MainLineNodeObject nodeCfg = MainLineNode.getById(i);
            if (nodeCfg == null) {
                continue;
            }

            int[] afterUnlockNode = nodeCfg.getAfternodeid();
            if (afterUnlockNode == null || afterUnlockNode.length <= 0) {
                continue;
            }

            for (int i1 : afterUnlockNode) {
                if (!ArrayUtil.intArrayContain(lastNodeList, i1)) {
                    unlock_part.add(i1);
                }
            }
        }

        List<Integer> needRemove = new ArrayList<>();
        //如果是迷阵型关卡,需要判断最开始解锁的节点是否包含空白节点,空白节点直接添加到进度
        if (curCfg.getType() == 4) {
            for (Integer unlockPoint : unlock_part) {
                MainLineNodeObject byId = MainLineNode.getById(unlockPoint);
                if (byId == null || byId.getNodetype() != 0) {
                    continue;
                }

                passed.add(unlockPoint);
                needRemove.add(unlockPoint);

                if (byId.getAfternodeid() != null) {
                    for (int i2 : byId.getAfternodeid()) {
                        unlock.add(i2);
                    }
                }
            }
        }

        unlock_part.removeAll(needRemove);
        unlock.addAll(unlock_part);
    }

    /**
     * 计算挂机收益
     */
    private void calculateOnHookReward() {
        List<Reward> totalReward;
        //第一次领取直接领取新手挂机奖励
        if (!getDBBuilder().getFirstOnHookClaimed()) {
            int[][] newBeeOnHookRewardsArray = GameConfig.getById(GameConst.CONFIG_ID).getNewbeeonhookrewards();
            totalReward = RewardUtil.parseRewardIntArrayToRewardList(newBeeOnHookRewardsArray);
            getDBBuilder().setFirstOnHookClaimed(true);
            LogUtil.info("mainlineEntity.calculateOnHookReward, player get first on hook rewards, rewards:"
                    + RewardUtil.toJsonStr(totalReward));
        } else {
            totalReward = calculateOnHookReward(getValidOnHookTime());
        }
        if (CollectionUtils.isEmpty(totalReward)) {
            return;
        }

        DB_OnHookIncome.Builder onHookIncomeBuilder = getDBBuilder().getOnHookIncomeBuilder();
        //添加当前已经获得的奖励
        totalReward.addAll(onHookIncomeBuilder.getGainRewardList());
        List<Reward> rewards = RewardUtil.mergeReward(totalReward);
        if (rewards == null || rewards.isEmpty()) {
            return;
        }

        onHookIncomeBuilder.setLastSettleTime(GlobalTick.getInstance().getCurrentTime());
        onHookIncomeBuilder.clearGainReward();
        onHookIncomeBuilder.addAllGainReward(rewards);
    }

    /**
     * 计算挂机收益
     *
     * @param validTime 有效时长
     * @return
     */
    public List<Reward> calculateOnHookReward(long validTime) {
        DB_OnHookIncome.Builder onHookIncomeBuilder = getDBBuilder().getOnHookIncomeBuilder();

        MainLineNodeObject nodeCfg = MainLineNode.getById(onHookIncomeBuilder.getCurOnHookNode());
        if (nodeCfg == null) {
            return null;
        }
        List<Reward> onHookRewards = RewardUtil.parseRewardIntArrayToRewardList(nodeCfg.getOnhookresourceoutput());
        List<RandomReward> onHookRandom = RewardUtil.parseIntArrayToRandomRewardList(nodeCfg.getOnhookrandompool());

        List<Reward> totalReward = new ArrayList<>();
        if (onHookRewards != null && !onHookRewards.isEmpty()) {
            List<Reward> rewards = calculateMustReward(validTime, onHookRewards);
            if (rewards != null && !rewards.isEmpty()) {
                totalReward.addAll(rewards);
            }
        }

        if (onHookRandom != null && !onHookRandom.isEmpty()) {
            List<Reward> randomReward = calculateRandomReward(validTime, onHookRandom);
            if (randomReward != null && !randomReward.isEmpty()) {
                totalReward.addAll(randomReward);
            }
        }

        List<Reward> rewards = calculateSpecialHookDrop(validTime);
        if (rewards != null) {
            totalReward.addAll(rewards);
        }

        List<Reward> dropReward = ActivityManager.getInstance().calculateAllActivityDrop(getLinkplayeridx(),
                DropResourceEnum.DRE_MainLineOnHook, validTime);
        if (dropReward != null) {
            totalReward.addAll(dropReward);
        }

        return totalReward;
    }

    /**
     * 计算特殊挂机掉落
     *
     * @param validTime
     * @return
     */
    private List<Reward> calculateSpecialHookDrop(long validTime) {
        List<Reward> result = new ArrayList<>();
        //积分副本门票,积分副本活动开启才掉落

        PointCopyOpenTimeObject openTimeCfg = PointCopyManager.getInstance().getOpenTimeCfg();
        if (openTimeCfg == null) {
            return null;
        }
        int interval = openTimeCfg.getPointcopyticketdropinterval();
        int times = (int) Math.min(validTime / (interval * TimeUtil.MS_IN_A_S), Integer.MAX_VALUE);
        if (times <= 0) {
            return result;
        }

        int odds = openTimeCfg.getPointcopyticketdropodds();
        Random random = new Random();
        int multi = 0;
        for (int i = 0; i < times; i++) {
            if (odds >= random.nextInt(1000)) {
                multi++;
            }
        }

        Reward reward = RewardUtil.parseAndMulti(openTimeCfg.getDropticket(), multi);
        if (reward != null) {
            result.add(reward);
        }
        return result;
    }

    public long OnHookCompleteTime() {
        long curTime = GlobalTick.getInstance().getCurrentTime();
        long startOnHookTime = getDBBuilder().getOnHookIncomeBuilder().getStartOnHookTime();
        long maxOnHookTime = VIPConfig.getMainLineMaxOnHookTime(getLinkplayeridx());
        return (curTime - startOnHookTime) - maxOnHookTime;
    }


    /**
     * 计算挂机的有效收益时间
     *
     * @return
     */
    private long getValidOnHookTime() {
        DB_OnHookIncome.Builder onHookIncomeBuilder = getDBBuilder().getOnHookIncomeBuilder();
        long curTime = GlobalTick.getInstance().getCurrentTime();
        long startOnHookTime = onHookIncomeBuilder.getStartOnHookTime();
        long lastSettleTime = onHookIncomeBuilder.getLastSettleTime();
        long maxOnHookTime = VIPConfig.getMainLineMaxOnHookTime(getLinkplayeridx());
        long totalOnHookTime = 0;
        //是否到达最大挂机时间
        if ((curTime - startOnHookTime) > maxOnHookTime) {
            totalOnHookTime = startOnHookTime + maxOnHookTime - lastSettleTime;
        } else {
            totalOnHookTime = curTime - lastSettleTime;
        }

        return Math.min(Math.max(totalOnHookTime, 0), maxOnHookTime);
    }

    /**
     * 计算挂机必得奖励
     *
     * @param validOnHookTime
     * @param mustReward
     * @return
     */
    private List<Reward> calculateMustReward(long validOnHookTime, List<Reward> mustReward) {
        if (validOnHookTime <= 0 || mustReward == null || mustReward.isEmpty()) {
            return null;
        }

        int mustMulti = (int) (validOnHookTime / (GameConfig.getById(GameConst.CONFIG_ID).getMainlineonhookrefreash() * TimeUtil.MS_IN_A_S));

        List<Reward> rewards = RewardUtil.multiReward(mustReward, mustMulti);

        int vipLv = PlayerUtil.queryPlayerVipLv(getLinkplayeridx());
        if (vipLv > 0) {
            rewards = onHookVipExAddition(rewards, vipLv);
        }

        return rewards;
    }

    /**
     * 计算vip加成
     *
     * @param rewards
     * @param vipLv
     * @return
     */
    public static List<Reward> onHookVipExAddition(List<Reward> rewards, int vipLv) {
        if (rewards == null || rewards.isEmpty()) {
            return null;
        }

        VIPConfigObject vipCfg = VIPConfig.getById(vipLv);
        if (vipCfg == null) {
            return rewards;
        }

        List<Reward> resultBuilder = new ArrayList<>();
        int[][] onHookExAddition = vipCfg.getOnhookexaddtion();
        for (Reward reward : rewards) {
            int exAdditionValue = getExAdditionValue(reward.getRewardType(), reward.getId(), onHookExAddition);
            if (exAdditionValue <= 0) {
                resultBuilder.add(reward);
            } else {
                Reward.Builder builder = reward.toBuilder();
                builder.setCount((builder.getCount() * (100 + exAdditionValue)) / 100);
                resultBuilder.add(builder.build());
            }
        }
        return resultBuilder;
    }

    /**
     * 获得加成值
     *
     * @param rewardEnum
     * @param rewardId
     * @param onHookExAdditionCfg
     * @return
     */
    public static int getExAdditionValue(RewardTypeEnum rewardEnum, int rewardId, int[][] onHookExAdditionCfg) {
        if (rewardEnum == null || rewardEnum == RewardTypeEnum.RTE_Null || onHookExAdditionCfg == null) {
            return 0;
        }

        for (int[] ints : onHookExAdditionCfg) {
            if (ints.length != 3) {
                continue;
            }

            if (rewardEnum.getNumber() == ints[0] && rewardId == ints[1]) {
                return ints[2];
            }
        }
        return 0;
    }

    public static final int MAINLINE_ON_HOOK_RANDOM_TOTAL_ODDS = 100000;

    /**
     * 计算挂机随机奖励
     *
     * @param validOnHookTime
     * @param randomRewards   随机奖励列表
     * @return
     */
    private List<Reward> calculateRandomReward(long validOnHookTime, List<RandomReward> randomRewards) {
        if (validOnHookTime <= 0 || randomRewards == null || randomRewards.isEmpty()) {
            return null;
        }

        int multi = (int) (validOnHookTime / (GameConfig.getById(GameConst.CONFIG_ID).getMainlinerandominterval() * TimeUtil.MS_IN_A_S));
        return RewardUtil.drawMustRandomReward(randomRewards, MAINLINE_ON_HOOK_RANDOM_TOTAL_ODDS, multi);
    }

    /**
     * 回退关卡,只有密码型关卡支持回退
     *
     * @param targetPoint 回退的目标关卡
     */
    public void fallbackPoint(int targetPoint) {
        MainLineCheckPointObject curCfg = MainLineCheckPoint.getById(targetPoint);
//        if (curCfg == null || curCfg.getType() != 1) {
//            return;
//        }
        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            LogUtil.error("playerIdx[" + linkplayeridx + "] mainlineDbData is null");
            return;
        }

        MainLineProgress.Builder mainLinePro = dbBuilder.getMainLineProBuilder();
        if (mainLinePro == null) {
            return;
        }
        mainLinePro.clearUnlockNodes();
        mainLinePro.setCurCheckPoint(curCfg.getBeforecheckpoint());
        mainLinePro.setAlreadyPassed(curCfg.getBeforecheckpoint());

        //上次操作节点退回到上一个关卡节点
        MainLineCheckPointObject cfg = MainLineCheckPoint.getById(curCfg.getBeforecheckpoint());
        if (cfg != null) {
            if (ArrayUtil.intArrayContain(cfg.getNodelist(), mainLinePro.getLastTransferNode())) {
                mainLinePro.setLastOperationtNode(mainLinePro.getLastTransferNode());
            } else {
                mainLinePro.setLastOperationtNode(ArrayUtil.getMaxInt(cfg.getNodelist(), 0));
            }
        }

        //清除密码
        removePswRecord(targetPoint);
        checkNextPointUnlock();
    }

    /**
     * 更新排行榜
     */
    public void updateRanking(int node) {
        RankingManager.getInstance().updatePlayerRankingScore(getLinkplayeridx(), EnumRankingType.ERT_MainLine,
                RankingName.RN_MainLinePassed, node);
    }

    public void removePswRecord(int point) {
        MainLineProgress.Builder mainLineProBuilder = getMainLineProBuilder();
        List<PswMap> pswRecordBuilderList = mainLineProBuilder.getPswRecordList();
        for (int i = 0; i < pswRecordBuilderList.size(); i++) {
            PswMap builder = pswRecordBuilderList.get(i);
            if (builder == null) {
                continue;
            }
            if (builder.getPointId() == point) {
                mainLineProBuilder.removePswRecord(i);
                break;
            }
        }
    }

    public List<Integer> getPswRecord(int point) {
        MainLineProgress.Builder mainLineProBuilder = getMainLineProBuilder();
        List<PswMap> pswRecordBuilderList = mainLineProBuilder.getPswRecordList();
        for (PswMap pswMap : pswRecordBuilderList) {
            if (pswMap.getPointId() == point) {
                return pswMap.getPswList();
            }
        }
        return null;
    }

    public boolean updatePswRecord(int pointId, List<Integer> pswList) {
        if (MainLineCheckPoint.getById(pointId) == null || pswList == null || pswList.isEmpty()) {
            LogUtil.info("mainlineEntity.updatePswRecord, error param");
            return true;
        }
        MainLineProgress.Builder mainLineProBuilder = getMainLineProBuilder();
        if (mainLineProBuilder == null) {
            return false;
        }
        List<PswMap.Builder> pswRecordBuilderList = mainLineProBuilder.getPswRecordBuilderList();
        for (PswMap.Builder builder : pswRecordBuilderList) {
            if (builder.getPointId() == pointId) {
                builder.clearPsw();
                builder.addAllPsw(pswList);
                return true;
            }
        }
        mainLineProBuilder.addPswRecord(buildPswMap(pointId, pswList));
        return true;
    }

    private PswMap buildPswMap(int pointId, List<Integer> pswList) {
        PswMap.Builder psw = PswMap.newBuilder();
        psw.setPointId(pointId);
        psw.addAllPsw(pswList);
        return psw.build();
    }

    public boolean pswIsRight(int pointId) {
        MainLineProgress.Builder mainLineProBuilder = getMainLineProBuilder();
        if (mainLineProBuilder == null) {
            return false;
        }
        for (PswMap.Builder builder : mainLineProBuilder.getPswRecordBuilderList()) {
            if (builder.getPointId() == pointId) {
                return MainLineUtil.pswIsRight(pointId, builder.getPswList());
            }
        }
        return false;
    }

    public void addProgress(int progress) {
        Builder dbBuilder = getDBBuilder();
        MainLineProgress.Builder mainLineProBuilder = dbBuilder.getMainLineProBuilder();
        if (!mainLineProBuilder.getProgressList().contains(progress)) {
            mainLineProBuilder.addProgress(progress);
        }
    }

    /**
     * 获得挂机收益，
     *
     * @param needRestart 是否需要重新计时
     */
    public List<Reward> getOnHookInCome(boolean needRestart) {
        calculateOnHookReward();

        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            return null;
        }

        DB_OnHookIncome.Builder onHookIncome = dbBuilder.getOnHookIncomeBuilder();
        List<Reward> gainRewardList = onHookIncome.getGainRewardList();
        if (needRestart) {
            long currentTime = GlobalTick.getInstance().getCurrentTime();
            onHookIncome.setLastSettleTime(currentTime);
            onHookIncome.setStartOnHookTime(currentTime);
            onHookIncome.clearGainReward();
        }
        return gainRewardList;
    }

    /**
     * 添加掉落道具
     *
     * @param rewards
     */
    public void addDropReward(List<Reward> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            return;
        }

        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            return;
        }
        DB_OnHookIncome.Builder onHookIncomeBuilder = dbBuilder.getOnHookIncomeBuilder();
        rewards.addAll(onHookIncomeBuilder.getGainRewardList());

        onHookIncomeBuilder.clearGainReward();
        onHookIncomeBuilder.addAllGainReward(RewardUtil.mergeReward(rewards));
    }

    /**
     * 返回当日已经快速挂机次数,当需要刷新时,会刷新次数
     */
    public int getTodayQuickOnHookTimes() {
        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            return 0;
        }
//        long currentTime = GlobalTick.getInstance().getCurrentTime();
//        if (currentTime > dbBuilder.getNextClearQuickOnHookTime()) {
//            dbBuilder.clearTodayQuickOnHookTimes();
//            dbBuilder.setNextClearQuickOnHookTime(TimeUtil.getNextDayResetTime(currentTime));
//        }
        return dbBuilder.getTodayQuickOnHookTimes();
    }

    public boolean canQuickOnHook() {
        Builder dbBuilder = getDBBuilder();
        if (dbBuilder == null) {
            return false;
        }

        MainLineNodeObject nodeCfg = MainLineNode.getById(dbBuilder.getOnHookIncome().getCurOnHookNode());
        return nodeCfg != null && nodeCfg.getOnhookable();
    }

    public void updateDailyData(boolean sendMsg) {
        getDBBuilder().clearTodayQuickOnHookTimes();
        getDBBuilder().clearTodayFreeOnHookTime();
        if (sendMsg) {
            sendMainLineDailyRefreshMsg();
        }
    }

    public void sendMainLineDailyRefreshMsg() {
        SC_RefreshMainLineInfo.Builder dailyRefresh = SC_RefreshMainLineInfo.newBuilder();
        dailyRefresh.setTodayQuickTimes(getTodayQuickOnHookTimes());
        dailyRefresh.setFreeQuickTimes(getDBBuilder().getTodayFreeOnHookTime());
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreshMainLineInfo_VALUE, dailyRefresh);
    }

    public void sendKeyNodeMissions() {
        MainLine.SC_KeyNodeMissionInfo.Builder msg = MainLine.SC_KeyNodeMissionInfo.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(getLinkplayeridx());
        int keyNodeId = getDBBuilder().getKeyNodeId();
        List<Integer> missionIds = MissionManager.getInstance().getKeyNodeMissionsByMissionKeyNode(keyNodeId);
        if (CollectionUtils.isNotEmpty(missionIds)) {
            Map<Integer, TargetSystem.TargetMission> missionMap = target.getDb_Builder().getKeyNodeMissionMap();
            for (Integer keyNodeMissionId : missionIds) {
                TargetSystem.TargetMission targetMission = missionMap.get(keyNodeMissionId);
                if (targetMission != null) {
                    msg.addMissions(targetMission);
                }
            }
        }
        msg.setCurKeyNodeClaim(getDBBuilder().getCurKeyNodeClaim());
        msg.setKeyNodeId(getDBBuilder().getKeyNodeId());
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_KeyNodeMissionInfo_VALUE, msg);
    }


    public void sendKeyNodeMissionUpdate(List<TargetSystem.TargetMission> modifyMissionList) {
        MainLine.SC_KeyNodeMissionInfo.Builder msg = MainLine.SC_KeyNodeMissionInfo.newBuilder();
        msg.addAllMissions(modifyMissionList);
        msg.setKeyNodeId(getDBBuilder().getKeyNodeId());
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_KeyNodeMissionInfo_VALUE, msg);
    }

    public void unlockAutoOnHook() {
        int curCheckPoint = getMainLineProBuilder().getLastOperationtNode();
        MainLineNodeObject cfg = MainLineNode.getById(curCheckPoint);
        if (cfg == null || !cfg.getOnhookable()) {
            return;
        }

        changeOnHookNode(curCheckPoint);
    }

    public void unlockEpisode(int nodeId) {
        MainLineNodeOpenEpisodeObject cfg = MainLineNodeOpenEpisode.getById(nodeId);
        if (cfg == null) {
            return;
        }
        int episodeId = cfg.getEpisodeid();
        if (getDBBuilder().getEpisodeProgressMap().containsKey(episodeId)) {
            return;
        }
        MainLine.EpisodeProgress.Builder dbEpisode = MainLine.EpisodeProgress.newBuilder()
                .setEpisodeId(episodeId).setCurEpisodeId(MainLineEpisodeConfig.getEpisodeStartByNode(episodeId)).setNew(true)
                .addCompleteProgress(MainLine.EpisodeProgressType.EPT_Null);

        getDBBuilder().putEpisodeProgress(dbEpisode.getEpisodeId(), dbEpisode.build());

        sendEpisodeUpdate(episodeId);
    }

    public void sendEpisodeUpdate(int episodeId) {
        MainLine.EpisodeProgress episodeProgress = getDBBuilder().getEpisodeProgressMap().get(episodeId);
        if (episodeProgress == null) {
            return;
        }
        MainLine.SC_RefreshEpisode.Builder msg = MainLine.SC_RefreshEpisode.newBuilder();
        msg.setEpisode(episodeProgress);
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), SC_RefreshEpisode_VALUE, msg);
    }

    public void addEpisodeProgress(int episodeId, MainLine.EpisodeProgressType completeProgress) {
        MainLine.EpisodeProgress episodeProgress = getDBBuilder().getEpisodeProgressMap().get(episodeId);
        if (episodeProgress==null){
            return;
        }
        MainLine.EpisodeProgress.Builder builder = episodeProgress.toBuilder().addCompleteProgress(completeProgress);
        if (builder.getEpisodeFinish()) {
            return;
        }
        if (!finishCurEpisodePoint(builder)) {
            getDBBuilder().putEpisodeProgress(builder.getEpisodeId(), builder.build());
            return;
        }
        int curNodeId = builder.getCurEpisodeId();
        int episodeNextNode = MainLineEpisodeConfig.getEpisodeNextNode(curNodeId);
        if (episodeNextNode == -1) {
            builder.setEpisodeFinish(true);
            getDBBuilder().putEpisodeProgress(builder.getEpisodeId(), builder.build());
            doEpisodeProgressReward(curNodeId);
            return;
        }
        builder.setCurEpisodeId(episodeNextNode).clearCompleteProgress().addCompleteProgress(MainLine.EpisodeProgressType.EPT_Null);
        getDBBuilder().putEpisodeProgress(builder.getEpisodeId(), builder.build());
        doEpisodeProgressReward(curNodeId);

    }

    private void doEpisodeProgressReward(int episodeNodeId) {
        MainLineEpisodeNodeConfigObject cfg = MainLineEpisodeNodeConfig.getById(episodeNodeId);
        if (cfg == null) {
            return;
        }
        RewardManager.getInstance().doRewardByList(getLinkplayeridx(),
                RewardUtil.parseRewardIntArrayToRewardList(cfg.getShowreward()),
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_EpisodePlot)
                , true);

    }

    private boolean finishCurEpisodePoint(MainLine.EpisodeProgress.Builder builder) {
        List<MainLine.EpisodeProgressType> progressList = builder.getCompleteProgressList();
        MainLineEpisodeNodeConfigObject cfg = MainLineEpisodeNodeConfig.getById(builder.getCurEpisodeId());
        if (cfg == null) {
            return false;
        }
        if (cfg.getFightmakeid() > 0 && !progressList.contains(MainLine.EpisodeProgressType.EPT_Fight)) {
            return false;
        }
        if (cfg.getBeforeplot().length > 0 && !progressList.contains(MainLine.EpisodeProgressType.EPT_ChatBeforeFight)) {
            return false;
        }
        if (cfg.getLaterplot().length > 0 && !progressList.contains(MainLine.EpisodeProgressType.EPT_ChatAfterFight)) {
            return false;
        }
        return true;
    }

    /**
     * 查询玩家当前所在插曲节点
     *
     * @return
     */
    public int queryPlayerCurEpisode() {
        int playerCurEpisode = getDBBuilder().getPlayerCurEpisode();
        MainLine.EpisodeProgress episodeProgress = getDBBuilder().getEpisodeProgressMap().get(playerCurEpisode);
        if (episodeProgress == null) {
            return -1;
        }
        return episodeProgress.getCurEpisodeId();
    }

    public void updateMaxPassAbility(long teamAbility) {
        if (teamAbility > getDBBuilder().getMaxPassAbility()) {
            getDBBuilder().setMaxPassAbility(teamAbility);
        }
    }

    public void checkAndFixMainline() {
        for (MainLine.EpisodeProgress value : getDBBuilder().getEpisodeProgressMap().values()) {
            if (value.getEpisodeFinish()) {
                continue;
            }
            if (episodeDataError(value)) {
                getDBBuilder().putEpisodeProgress(value.getEpisodeId(), value.toBuilder().clearCompleteProgress().addCompleteProgress(MainLine.EpisodeProgressType.EPT_Null).build());
            }

        }

    }

    private boolean episodeDataError(MainLine.EpisodeProgress progress) {
        List<MainLine.EpisodeProgressType> progressList = progress.getCompleteProgressList();
        MainLineEpisodeNodeConfigObject nodeCfg = MainLineEpisodeNodeConfig.getById(progress.getCurEpisodeId());
        if (nodeCfg == null) {
            return false;
        }
/*      客户端不管有没有都会发
        if (nodeCfg.getBeforeplot().length == 0 && progressList.contains(MainLine.EpisodeProgressType.EPT_ChatBeforeFight)) {
            return true;
        }*/
        if (nodeCfg.getLaterplot().length == 0 && progressList.contains(MainLine.EpisodeProgressType.EPT_ChatAfterFight)) {
            return true;
        }
        if (nodeCfg.getFightmakeid() == 0 && progressList.contains(MainLine.EpisodeProgressType.EPT_Fight)) {
            return true;
        }
        if (progress.getCompleteProgressCount() > 1 && stepError(progressList)) {
            return true;
        }

        return false;
    }

    private boolean stepError(List<MainLine.EpisodeProgressType> progressList) {
        List<MainLine.EpisodeProgressType> list = progressList.stream().sorted(Comparator.comparingInt(MainLine.EpisodeProgressType::getNumber)).distinct().collect(Collectors.toList());

        //如果玩家进行到战斗
        if (list.contains(MainLine.EpisodeProgressType.EPT_Fight)) {
            //进行到战斗没有触发战前数据就报错
            if (!list.contains(MainLine.EpisodeProgressType.EPT_ChatBeforeFight)) {
                return true;
            }
        }

        //如果玩家触发战斗后对话
        if (list.contains(MainLine.EpisodeProgressType.EPT_ChatAfterFight)) {
            //触发战后对话没有战斗或者战前对话报错
            if (!list.contains(MainLine.EpisodeProgressType.EPT_Fight)) {
                return true;
            }

            if (!list.contains(MainLine.EpisodeProgressType.EPT_ChatBeforeFight)) {
                return true;
            }
        }
        return false;
    }
}