package model.mistforest.task;

import cfg.MistNpcTaskConfig;
import cfg.MistNpcTaskConfigObject;
import common.GameConst.EventType;
import common.GlobalTick;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.mistforest.MistConst;
import model.mistforest.mistobj.MistFighter;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.EnumNpcTaskState;
import protocol.MistForest.MistNpcTaskData;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_UpdateNpcTask;
import server.event.Event;
import server.event.EventManager;
import util.TimeUtil;

public class MistNpcTask {
    protected MistFighter owner;
    protected Map<Integer, MistTaskEntity> npcTaskMap;
    protected long updateTime;

    public MistNpcTask(MistFighter fighter) {
        this.owner = fighter;
    }

    public void clear() {
        if (npcTaskMap != null) {
            npcTaskMap.clear();
        }
    }

    public void updateAllTask(MistPlayer player) {
        if (player == null) {
            return;
        }
        if (npcTaskMap == null || npcTaskMap.isEmpty()) {
            return;
        }
        SC_UpdateNpcTask.Builder builder = null;
        for (MistTaskEntity npcTask : npcTaskMap.values()) {
            if (npcTask.getTaskState() != EnumNpcTaskState.ENTS_NotFinish_VALUE && npcTask.getTaskState() != EnumNpcTaskState.ENTS_FinishNotClaim_VALUE) {
                continue;
            }
            if (builder == null) {
                builder = SC_UpdateNpcTask.newBuilder();
                builder.setAllFlag(true);
            }
            MistNpcTaskData.Builder taskBuilder = MistNpcTaskData.newBuilder();
            taskBuilder.setTaskId(npcTask.getCfgId());
            taskBuilder.setTaskProgress(npcTask.getTaskProgress());
            taskBuilder.setTaskStateValue(npcTask.getTaskState());
            taskBuilder.setExpireTime(npcTask.getExpireTime());
            builder.addNpcTask(taskBuilder);
        }
        if (builder != null) {
            player.sendMsgToServer(MsgIdEnum.SC_UpdateNpcTask_VALUE, builder);
        }
    }

    public MistRetCode acceptTask(int cfgId) {
        MistNpcTaskConfigObject cfg = MistNpcTaskConfig.getById(cfgId);
        if (cfg == null) {
            return MistRetCode.MRC_NotFoundTask;
        }
        if (npcTaskMap != null && npcTaskMap.containsKey(cfgId)) {
            return MistRetCode.MRC_AcceptedTask;
        }
        MistPlayer player = owner.getOwnerPlayerInSameRoom();
        if (player == null) {
            return MistRetCode.MRC_NotFoundPlayer;
        }
        if (npcTaskMap == null) {
            npcTaskMap = new HashMap<>();
        }
        MistTaskEntity task = new MistTaskEntity(cfgId, GlobalTick.getInstance().getCurrentTime() + cfg.getDuration() * TimeUtil.MS_IN_A_S);
        npcTaskMap.put(cfgId, task);

        SC_UpdateNpcTask.Builder builder = SC_UpdateNpcTask.newBuilder();
        MistNpcTaskData.Builder taskBuilder = MistNpcTaskData.newBuilder();
        taskBuilder.setTaskId(task.getCfgId());
        taskBuilder.setTaskStateValue(EnumNpcTaskState.ENTS_NotFinish_VALUE);
        taskBuilder.setExpireTime(task.getExpireTime());
        builder.addNpcTask(taskBuilder);
        player.sendMsgToServer(MsgIdEnum.SC_UpdateNpcTask_VALUE, builder);
        return MistRetCode.MRC_Success;
    }

    public MistRetCode claimNpcTaskReward(int cfgId) {
        MistNpcTaskConfigObject taskCfg = MistNpcTaskConfig.getById(cfgId);
        if (taskCfg == null) {
            return MistRetCode.MRC_ErrorParam;
        }
        if (npcTaskMap == null) {
            return MistRetCode.MRC_NotAcceptTask;
        }
        MistPlayer player = owner.getOwnerPlayerInSameRoom();
        if (player == null) {
            return MistRetCode.MRC_NotFoundPlayer;
        }
        MistTaskEntity task = npcTaskMap.get(cfgId);
        if (task == null) {
            return MistRetCode.MRC_NotAcceptTask;
        }
        if (task.getExpireTime() > 0 && task.getExpireTime() <= GlobalTick.getInstance().getCurrentTime()) {
            return MistRetCode.MRC_NpcTaskExpire;
        }

        if (task.getTaskState() == EnumNpcTaskState.ENTS_FinishAndClaimed_VALUE) {
            return MistRetCode.MRC_ClaimedTaskReward;
        } else if (task.getTaskState() == EnumNpcTaskState.ENTS_NotFinish_VALUE) {
            return MistRetCode.MRC_NotFinishTask;
        } else if (task.getTaskState() != EnumNpcTaskState.ENTS_FinishNotClaim_VALUE) {
            return MistRetCode.MRC_NotAcceptTask;
        }
        awardToPlayer(task);

        SC_UpdateNpcTask.Builder builder = SC_UpdateNpcTask.newBuilder();
        MistNpcTaskData.Builder taskBuilder = MistNpcTaskData.newBuilder();
        taskBuilder.setTaskId(task.getCfgId());
        taskBuilder.setTaskStateValue(EnumNpcTaskState.ENTS_FinishAndClaimed_VALUE);
        builder.addNpcTask(taskBuilder);
        player.sendMsgToServer(MsgIdEnum.SC_UpdateNpcTask_VALUE, builder);
        return MistRetCode.MRC_Success;
    }

    public void doNpcTask(int taskType, int addProgress, int extParam) {
        if (npcTaskMap == null) {
            return;
        }
        MistNpcTaskConfigObject cfg;
        SC_UpdateNpcTask.Builder builder = null;
        long curTime = GlobalTick.getInstance().getCurrentTime();
        for (MistTaskEntity task : npcTaskMap.values()) {
            if (task.getTaskState() != EnumNpcTaskState.ENTS_NotFinish_VALUE) {
                continue;
            }
            if (task.getExpireTime() > 0 && task.getExpireTime() <= curTime) {
                continue;
            }
            cfg = MistNpcTaskConfig.getById(task.cfgId);
            if (cfg == null) {
                continue;
            }
            if (cfg.getMisttasktype() != taskType) {
                continue;
            }
            if (cfg.getExtparam() > 0 && extParam != cfg.getExtparam()) {
                continue;
            }
            int progress = task.getTaskProgress() + addProgress;
            if (progress >= cfg.getTargetcount()) {
                progress = cfg.getTargetcount();
                task.setTaskState(EnumNpcTaskState.ENTS_FinishNotClaim_VALUE);
            }
            task.setTaskProgress(progress);
            if (builder == null) {
                builder = SC_UpdateNpcTask.newBuilder();
            }
            MistNpcTaskData.Builder taskBuilder = MistNpcTaskData.newBuilder();
            taskBuilder.setTaskId(cfg.getId());
            taskBuilder.setTaskStateValue(task.getTaskState());
            taskBuilder.setTaskProgress(progress);
            taskBuilder.setExpireTime(task.getExpireTime());
            builder.addNpcTask(taskBuilder);
        }
        if (builder != null) {
            MistPlayer player = owner.getOwnerPlayerInSameRoom();
            if (player != null) {
                player.sendMsgToServer(MsgIdEnum.SC_UpdateNpcTask_VALUE, builder);
            }
        }
    }

    protected void awardToPlayer(MistTaskEntity task) {
        MistPlayer player = owner.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        if (task.getTaskState() != EnumNpcTaskState.ENTS_FinishNotClaim_VALUE) {
           return;
        }
        MistNpcTaskConfigObject taskCfg = MistNpcTaskConfig.getById(task.getCfgId());
        if (taskCfg == null) {
            return;
        }
        Map<Integer, Integer> rewardMap = MistConst.buildCommonRewardMap(taskCfg.getFinishrewrad(), EnumMistRuleKind.EMRK_Common_VALUE, player.getLevel());
        if (rewardMap != null) {
            Event event = Event.valueOf(EventType.ET_GainMistCarryReward, owner.getRoom(), player);
            event.pushParam(rewardMap, false);
            EventManager.getInstance().dispatchEvent(event);
        }
        task.setTaskState(EnumNpcTaskState.ENTS_FinishAndClaimed_VALUE);
    }

    public void onTick(long curTime) {
        if (updateTime > curTime) {
            return;
        }
        if (npcTaskMap == null) {
            return;
        }
        MistPlayer player = owner.getOwnerPlayerInSameRoom();
        if(player == null) {
            return;
        }
        MistNpcTaskConfigObject taskCfg;
        List<Integer> removeList = null;
        for (MistTaskEntity task : npcTaskMap.values()) {
            boolean needRemove = false;
            taskCfg = MistNpcTaskConfig.getById(task.getCfgId());
            if (taskCfg == null) {
                needRemove = true;
            } if (task.getTaskState() == EnumNpcTaskState.ENTS_FinishAndClaimed_VALUE) {
                needRemove = true;
            } else if (task.getExpireTime() > 0 && task.getExpireTime() <= curTime) {
                needRemove = true;
                awardToPlayer(task);
            }
            if (needRemove) {
                if (removeList == null) {
                    removeList = new ArrayList<>();
                }
                removeList.add(task.getCfgId());
            }
        }
        if (removeList != null) {
            for (Integer taskCfgId : removeList) {
                npcTaskMap.remove(taskCfgId);
            }
        }
        updateTime = curTime + TimeUtil.MS_IN_A_S;
    }
}
