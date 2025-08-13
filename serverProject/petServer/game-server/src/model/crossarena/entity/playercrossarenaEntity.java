/**
 * created by tool DAOGenerate
 */
package model.crossarena.entity;

import java.util.*;
import java.util.stream.Collectors;

import cfg.*;
import com.google.protobuf.InvalidProtocolBufferException;

import common.GameConst;
import model.crossarena.CrossArenaManager;
import model.crossarena.dbCache.playercrossarenaCache;
import model.obj.BaseObj;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common;
import protocol.Common.RewardTypeEnum;
import protocol.CrossArena;
import protocol.CrossArena.CrossArenaDBKey;
import protocol.CrossArena.CrossArenaTaskWeekBox;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.CrossArenaDB;
import protocol.CrossArenaDB.CrossArenaPlayerDB;
import protocol.CrossArenaDB.CrossArenaHonorDB;
import util.EventUtil;

import static common.GameConst.LtDailyWinTask;
import static common.GameConst.LtWeeklyWinTask;
import static protocol.CrossArena.CrossArenaDBKey.*;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class playercrossarenaEntity extends BaseObj {

    public String getClassType() {
        return "playercrossarenaEntity";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private byte[] data;

    /**
     * 鑾峰緱
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 璁剧疆
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 鑾峰緱
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 璁剧疆
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void putToCache() {
        playercrossarenaCache.put(this);
    }

    @Override
    public void transformDBData() {
        if (null != dataMsg) {
            dataMsg.setHonor(honorMsg);
            data = dataMsg.build().toByteArray();
        }
    }

    public void toBuilder() throws InvalidProtocolBufferException {
        if (data != null) {
            dataMsg = CrossArenaPlayerDB.parseFrom(data).toBuilder();
        } else {
            dataMsg = CrossArenaPlayerDB.getDefaultInstance().toBuilder();
        }
        honorMsg = dataMsg.getHonor().toBuilder();
    }

    public String getBaseIdx() {
        return idx;
    }

    public CrossArenaPlayerDB.Builder getDataMsg() {
        return dataMsg;
    }

    public void setDataMsg(CrossArenaPlayerDB.Builder dataMsg) {
        this.dataMsg = dataMsg;
    }

    /*************************** 分割 **********************************/

    private CrossArenaPlayerDB.Builder dataMsg;
    private CrossArenaHonorDB.Builder honorMsg;

    public void updateDailyData() {
        settleWinTaskReward();
        dataMsg.putDbs( CrossArenaDBKey.LT_WINCOTCUR_VALUE,0);
        dataMsg.putDbs( LT_WINCOTDAY_VALUE,0);

        dataMsg.putDbs(CrossArena.CrossArenaDBKey.LT_ADMIRE_VALUE, 0);
        dataMsg.putDbs(LT_WINCOTDAY_VALUE, 0);
        dataMsg.putDbs(CrossArena.CrossArenaDBKey.LT_BATTLENUM_DAY_VALUE, 0);
        dataMsg.putDbs(CrossArena.CrossArenaDBKey.LT_WINNUM_DAY_VALUE, 0);
        dataMsg.putDbs(LT_LastSerialWinNum_VALUE, 0);
        dataMsg.clearLtCotCoot();
        dataMsg.setLeijiTime(0);
        dataMsg.clearDailyWinTaskScienceId();
        dataMsg.clearTodayUseWinProtect();
        clearDailyWinNum();
        dataMsg.clearNoteAward();

        // 更新事件数据
        Map<Integer, CrossArenaEventObject> eventCfg = CrossArenaEvent._ix_id;
        for (CrossArenaEventObject eventc : eventCfg.values()) {
            if (eventc.getCycletype() == 1) {
                if (dataMsg.getEventFlishNumMap().containsKey(eventc.getId())) {
                    Map<Integer, Integer> temp = new HashMap<>();
                    temp.putAll(dataMsg.getEventFlishNumMap());
                    temp.remove(eventc.getId());
                    dataMsg.clearEventFlishNum();
                    dataMsg.putAllEventFlishNum(temp);
                }
                if (dataMsg.getEventCurMap().containsKey(eventc.getId())) {
                    Map<Integer, Long> temp = new HashMap<>();
                    temp.putAll(dataMsg.getEventCurMap());
                    temp.remove(eventc.getId());
                    dataMsg.clearEventCur();
                    dataMsg.putAllEventCur(temp);
                }
            }
        }
        if (CrossArenaManager.getInstance().isPlayerInLt(getIdx())){
            CrossArenaManager.getInstance().sendMainPanelInfo(getIdx());
        }
    }

    private void clearDailyWinNum() {
        for (Map.Entry<Integer, CrossArenaDB.DB_LTSerialWin> entry : dataMsg.getSerialWinDataMap().entrySet()) {
            if (entry.getValue().getWinDataOrDefault(LT_WINCOTDAY_VALUE, 0) != 0) {
                dataMsg.putSerialWinData(entry.getKey(), entry.getValue().toBuilder().removeWinData(LT_WINCOTDAY_VALUE).build());
            }
        }
    }

    private void clearWeeklyWinNum() {
        for (Map.Entry<Integer, CrossArenaDB.DB_LTSerialWin> entry : dataMsg.getSerialWinDataMap().entrySet()) {
            if (entry.getValue().getWinDataOrDefault(LT_WINCOTWeekly_VALUE, 0) != 0) {
                dataMsg.putSerialWinData(entry.getKey(), entry.getValue().toBuilder().removeWinData(LT_WINCOTWeekly_VALUE).build());
            }
            if (entry.getValue().getWinDataOrDefault(LT_10SerialWinWeek_VALUE, 0) != 0) {
                dataMsg.putSerialWinData(entry.getKey(), entry.getValue().toBuilder().removeWinData(LT_10SerialWinWeek_VALUE).build());
            }
        }
    }


    public void settleWinTaskReward() {
        Integer scienceId = findWinTaskRewardSettleScience(LtDailyWinTask);
        if (scienceId == null) {
            return;
        }
        List<Reward> rewards = CrossArenaManager.getInstance().getDailyWinTaskRewardList(scienceId, getIdx()).getRewards();
        dataMsg.clearWinTask();
        if (CollectionUtils.isEmpty(rewards)) {
            return;
        }
        rewards = RewardUtil.mergeReward(rewards);
        int mailId = MailTemplateUsed.getById(GameConst.CONFIG_ID).getLtserailwintaskdaily();
        Reason borrowReason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossArenaWinTask,"擂台每日连胜奖励补发");
        EventUtil.triggerAddMailEvent(getIdx(), mailId, rewards, borrowReason);
    }

    public void settleWinTaskWeeklyReward() {
        Integer scienceId = findWinTaskRewardSettleScience(LtWeeklyWinTask);
        if (scienceId == null) {
            return;
        }
        int winNum = getWinNumByScienceId(scienceId, LT_WINCOTWeekly);
        if (winNum <= 0) {
            return;
        }
        CrossArenaWinTaskObject config = findMaxWeeklyTaskRewardCfg(scienceId, winNum);
        if (config == null) {
            return;
        }
        List<Reward> rewards = CrossArenaManager.getInstance().parseTaskReward(config);
        if (CollectionUtils.isEmpty(rewards)) {
            return;
        }
        rewards = RewardUtil.mergeReward(rewards);
        int mailId = MailTemplateUsed.getById(GameConst.CONFIG_ID).getLtserailwintaskweekly();
        Reason borrowReason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossArenaWinTask, "周连胜结算");
        EventUtil.triggerAddMailEvent(getIdx(), mailId, rewards, borrowReason);
    }

    private CrossArenaWinTaskObject findMaxWeeklyTaskRewardCfg(int scienceId, int winNum) {
        return CrossArenaWinTask._ix_id.values().stream().filter(cfg ->
                scienceId == cfg.getSceneid() && cfg.getType() == LtWeeklyWinTask && cfg.getWinning() <= winNum
        ).max(Comparator.comparingInt(CrossArenaWinTaskObject::getWinning)).orElse(null);
    }

    private Integer findWinTaskRewardSettleScience(int settleType) {
        if (settleType == LtDailyWinTask && getDataMsg().getDailyWinTaskScienceId() != 0) {
            return getDataMsg().getDailyWinTaskScienceId();
        }
        int key = settleType == LtDailyWinTask ? LT_WINCOTDAY_VALUE : LT_WINCOTWeekly_VALUE;
        return getDataMsg().getSerialWinDataMap().entrySet().stream().filter(v -> v.getValue().getWinDataOrDefault(key, 0) > 0).map(Map.Entry::getKey).max(Integer::compareTo).orElse(null);
    }

    private void clearClaimWinTask(int settleType) {
        if (dataMsg.getWinTaskCount() <= 0) {
            return;
        }
        List<Integer> sourceTaskIds = settleType == LtDailyWinTask ? CrossArenaWinTask.getInstance().getDailyTaskIds() : CrossArenaWinTask.getInstance().getWeekTaskIds();

        List<Integer> unClear = dataMsg.getWinTaskList().stream().filter(taskId -> !sourceTaskIds.contains(taskId)).collect(Collectors.toList());

        dataMsg.clearWinTask().addAllWinTask(unClear);

    }

    public void updateWeeklyData() {
        doWeeklyReward();
        settleWinTaskWeeklyReward();
        clearWeeklyWinNum();
        dataMsg.putDbs(LT_10SerialWinWeek_VALUE,0);
        dataMsg.putDbs(CrossArenaDBKey.LT_WINCOTWeekly_VALUE,0);
        dataMsg.putDbs(CrossArena.CrossArenaDBKey.LT_BATTLENUM_WEEK_VALUE, 0);
        dataMsg.putDbs(CrossArenaDBKey.LT_BATTLEWinNUM_WEEK_VALUE, 0);
        dataMsg.clearWeekBattleRes();
        List<CrossArenaTaskWeekBox> createPreWeekBoxReward = CrossArenaManager.getInstance().createPreWeekBoxReward(getBaseIdx(),dataMsg.getWeekBoxTaskMap().keySet(), dataMsg.getWeekTaskDataMap());
        //CrossArenaManager.getInstance().doWeekGradeReward(getIdx(), dataMsg.getDbsMap().getOrDefault(CrossArena.CrossArenaDBKey.LT_GRADELV_VALUE, 0));
        dataMsg.clearPreWeekbox();
        dataMsg.addAllPreWeekbox(createPreWeekBoxReward);
        dataMsg.clearWeekBoxTask();
        dataMsg.clearWeekTaskData();
        // 更新事件数据
        Map<Integer, CrossArenaEventObject> eventCfg = CrossArenaEvent._ix_id;
        for (CrossArenaEventObject eventc : eventCfg.values()) {
            if (eventc.getCycletype() == 2) {
                if (dataMsg.getEventFlishNumMap().containsKey(eventc.getId())) {
                    Map<Integer, Integer> temp = new HashMap<>();
                    temp.putAll(dataMsg.getEventFlishNumMap());
                    temp.remove(eventc.getId());
                    dataMsg.clearEventFlishNum();
                    dataMsg.putAllEventFlishNum(temp);
                }
                if (dataMsg.getEventCurMap().containsKey(eventc.getId())) {
                    Map<Integer, Long> temp = new HashMap<>();
                    temp.putAll(dataMsg.getEventCurMap());
                    temp.remove(eventc.getId());
                    dataMsg.clearEventCur();
                    dataMsg.putAllEventCur(temp);
                }
            }
        }
        if (CrossArenaManager.getInstance().isPlayerInLt(getIdx())){
            CrossArenaManager.getInstance().sendMainPanelInfo(getIdx());
        }
    }

    private void doWeeklyReward() {
        int expNum = CrossArenaManager.getInstance().computeExpIncr(this);
        if (expNum <= 0) {
            return;
        }
        Reward reward = RewardUtil.parseReward(RewardTypeEnum.RTE_CrossArenaGrade, 0, expNum);

        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_CrossArenaWeeklySettle);
        EventUtil.triggerAddMailEvent(getIdx(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getLtweeklysettle(), Collections.singletonList(reward), reason);
    }

    public CrossArenaHonorDB.Builder getHonorMsg() {
        return honorMsg;
    }

    public int getWinNumByScienceId(int nowInSceneId,CrossArenaDBKey key) {
        CrossArenaDB.DB_LTSerialWin dbWinData = getDataMsg().getSerialWinDataOrDefault(nowInSceneId, CrossArenaDB.DB_LTSerialWin.getDefaultInstance());
        return dbWinData.getWinDataMap().getOrDefault(key.getNumber(), 0);
    }
}