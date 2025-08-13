package server.handler.crossarena;

import cfg.*;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.crossarena.CrossArenaManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.RewardSourceEnum;
import protocol.CrossArena;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import protocol.TargetSystem.CS_CrossArenaMissionReward;
import protocol.TargetSystem.SC_CrossArenaMissionReward;
import protocol.TargetSystem.TargetMission;
import util.GameUtil;

import java.util.ArrayList;
import java.util.List;

@MsgId(msgId = MsgIdEnum.CS_CrossArenaMissionReward_VALUE)
public class CrossArenaTaskRewardHandler extends AbstractBaseHandler<CS_CrossArenaMissionReward> {
    @Override
    protected CS_CrossArenaMissionReward parse(byte[] bytes) throws Exception {
        return CS_CrossArenaMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CrossArenaMissionReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_CrossArenaMissionReward.Builder resultBuilder = SC_CrossArenaMissionReward.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_CrossArenaMissionReward_VALUE, resultBuilder);
            return;
        }

        MissionObject missionCfg = Mission.getById(req.getIndex());
        if (missionCfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_CrossArenaMissionReward_VALUE, resultBuilder);
            return;
        }

        // 特殊处理连胜任务
        if (missionCfg.getMissiontype() == TargetSystem.TargetTypeEnum.TTE_CrossArena_COTWin_VALUE) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_CrossArenaMissionReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            TargetMission missionPro = entity.getDb_Builder().getCrossArenaInfoMap().get(req.getIndex());
            if (missionPro == null || missionPro.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_CrossArenaMissionReward_VALUE, resultBuilder);
                return;
            }

            if (missionPro.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_CrossArenaMissionReward_VALUE, resultBuilder);
                return;
            }
            int sceneId = CrossArenaManager.getInstance().getPlayerDBInfo(playerIdx, CrossArena.CrossArenaDBKey.LT_SCENEID);
            List<Common.Reward> rewards = new ArrayList<>();
            CrossArenaTaskAwardObject jl = CrossArenaTaskAward.getInstance().getAward(missionCfg.getId(), sceneId, 0);
            if (null != jl) {
                rewards.addAll(RewardUtil.parseRewardIntArrayToRewardList(jl.getAward()));
            }
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_CrossArenaDailyMission);

            // 奖励根据道场等级获取
            int sid = CrossArenaManager.getInstance().getPlayerDBInfo(playerIdx, CrossArena.CrossArenaDBKey.LT_SCENEID);
            int rate = 10000;
            CrossArenaSceneObject caso = CrossArenaScene.getById(sid);
            if (null != caso) {
                rate = caso.getGraderate();
            }
            List<Common.Reward> rewardsTemp = new ArrayList<>();
            for (Common.Reward ttt : rewards) {
                if (ttt.getRewardType() == Common.RewardTypeEnum.RTE_CrossArenaGrade) {
                    Common.Reward.Builder nett = ttt.toBuilder();
                    nett.setCount(Math.round(ttt.getCount() * (rate *1F / 10000)));
                    rewardsTemp.add(nett.build());
                } else {
                    rewardsTemp.add(ttt);
                }
            }
            RewardManager.getInstance().doRewardByList(playerIdx, rewardsTemp, reason, true);

            TargetMission newdata = missionPro.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
            entity.getDb_Builder().putCrossArenaInfo(missionPro.getCfgId(), newdata);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_CrossArenaMissionReward_VALUE, resultBuilder);

            TargetSystem.SC_RefCrossArenaTaskMission.Builder refreshBuilder = TargetSystem.SC_RefCrossArenaTaskMission.newBuilder();
            refreshBuilder.addMission(newdata);
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefCrossArenaTaskMission_VALUE, refreshBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.CrossArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CrossArenaMissionReward_VALUE, SC_CrossArenaMissionReward.newBuilder().setRetCode(retCode));
    }
}
