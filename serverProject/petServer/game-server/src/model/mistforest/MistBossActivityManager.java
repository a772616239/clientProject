package model.mistforest;

import cfg.MailTemplateUsed;
import cfg.MistCommonConfig;
import cfg.MistCommonConfigObject;
import cfg.MistTimeLimitActivity;
import cfg.MistTimeLimitActivityObject;
import common.GameConst;
import common.GameConst.EventType;
import common.tick.GlobalTick;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.ServerTransfer.MistBossDmgRankInfo;
import protocol.TransServerCommon.MistActivityBossPlayerData;
import server.event.Event;
import server.event.EventManager;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

public class MistBossActivityManager {
    protected MistTimeLimitActivityObject activityCfg;
    protected Map<String, MistActivityBossPlayerData> playerActivityData;

    protected boolean open;
    protected long updateTime;

    public void init() {
        playerActivityData = new ConcurrentHashMap<>();
        long curTime = GlobalTick.getInstance().getCurrentTime();
        activityCfg = getNextBossActivityCfg(curTime);
        if (activityCfg != null) {
            open = activityCfg.getStarttime() <= curTime && activityCfg.getEndtime() > curTime;
        }
    }

    public MistTimeLimitActivityObject getNextBossActivityCfg(long curTime) {
        MistTimeLimitActivityObject tmpCfg = null;
        for (MistTimeLimitActivityObject cfg : MistTimeLimitActivity._ix_id.values()) {
            if (cfg.getEndtime() <= curTime) {
                continue;
            }
            if (null == tmpCfg || tmpCfg.getStarttime() > cfg.getStarttime()) {
                tmpCfg = cfg;
            }
        }
        return tmpCfg;
    }

    protected List<Reward> getRewardByRank(int mistLevel, int rank) {
        MistCommonConfigObject cfg = MistCommonConfig.getByMistlevel(mistLevel);
        if (cfg == null || cfg.getBossactivityrankreward() == null) {
            return null;
        }
        for (int i = 0; i < cfg.getBossactivityrankreward().length; i++) {
            if (null == cfg.getBossactivityrankreward()[i] || cfg.getBossactivityrankreward()[i].length < 3) {
                continue;
            }
            if (cfg.getBossactivityrankreward()[i][0] <= rank && cfg.getBossactivityrankreward()[i][1] >= rank) {
                return RewardUtil.getRewardsByRewardId(cfg.getBossactivityrankreward()[i][2]);
            }
        }
        return null;
    }

    protected void settleBossActivity() {
        LogUtil.info("SettMistBossActivity reward, rewardSize={}", playerActivityData.size());
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest_BossActivity);
        for (MistActivityBossPlayerData rankData : playerActivityData.values()) {
            List<Reward> rewards = getRewardByRank(rankData.getDmgRankMistLevel(), rankData.getDmgRank());
            if (CollectionUtils.isEmpty(rewards)) {
                LogUtil.error("SettMistBossActivity reward is null,playerId={},mistLevel={},rank={}",rankData.getPlayerId(), rankData.getDmgRankMistLevel(), rankData.getDmgRank());
                continue;
            }
            EventUtil.triggerAddMailEvent(rankData.getPlayerId(), MailTemplateUsed.getById(GameConst.CONFIG_ID).getMistactivitybossreward(),
                    rewards, reason, String.valueOf(rankData.getDmgRank()));
        }
    }

    public void addMistRankData(int mistLevel, MistBossDmgRankInfo rankInfo) {
        MistActivityBossPlayerData.Builder builder;
        MistActivityBossPlayerData playerData = playerActivityData.get(rankInfo.getPlayerIdx());
        if (playerData == null) {
            builder = MistActivityBossPlayerData.newBuilder();
        } else {
            if (playerData.getDmgRank() < rankInfo.getRank()) {
                return;
            }
            builder = playerData.toBuilder();
        }
        builder.setPlayerId(rankInfo.getPlayerIdx());
        builder.setDmgRank(rankInfo.getRank());
        builder.setDmgRankMistLevel(mistLevel);
        playerActivityData.put(rankInfo.getPlayerIdx(), builder.build());
        LogUtil.info("AddMistBossDmgRankData,mistLv={},playerIdx={},rank={}", mistLevel, builder.getPlayerId(), builder.getDmgRank());
    }

    public void addMistGainBossActivityRewardData(String playerIdx, int gainRewardInMistLevel) {
        MistActivityBossPlayerData.Builder builder;
        MistActivityBossPlayerData playerData = playerActivityData.get(playerIdx);
        if (playerData == null) {
            builder = MistActivityBossPlayerData.newBuilder();
        } else {
            builder = playerData.toBuilder();
        }
        builder.setPlayerId(playerIdx);
        builder.setGainBossBoxMistLevel(gainRewardInMistLevel);
        playerActivityData.put(playerIdx, builder.build());
        LogUtil.info("AddMistGainBossActivityRewardData,mistLv={},playerIdx={},rank={}", builder.getGainBossBoxMistLevel(), builder.getPlayerId(), builder.getDmgRank());
    }

    public int getPlayerGainBossRewardMistLevel(String playerIdx) {
        MistActivityBossPlayerData data = playerActivityData.get(playerIdx);
        if (data == null) {
            return 0;
        }
        return data.getGainBossBoxMistLevel();
    }

    public void onTick(long curTime) {
        if (updateTime > curTime) {
            return;
        }
        if (null == activityCfg) {
            return;
        }
        updateTime = curTime + TimeUtil.MS_IN_A_S;
        if (open) {
            if (activityCfg.getEndtime() + (TimeUtil.MS_IN_A_S * 15) <= curTime) { // 15秒后结算
                settleBossActivity();
                open = false;
                activityCfg = getNextBossActivityCfg(curTime);
            }
        } else {
            if (activityCfg.getStarttime() <= curTime) {
                Event event = Event.valueOf(EventType.ET_MistBossActivityBegin, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
                event.pushParam(playerActivityData.values().stream().collect(Collectors.toList()));
                EventManager.getInstance().dispatchEvent(event);
                playerActivityData.clear();
                open = true;
            }
        }
    }
}
