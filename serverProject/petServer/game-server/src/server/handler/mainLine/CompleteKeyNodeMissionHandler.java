package server.handler.mainLine;

import cfg.KeyNodeConfig;
import cfg.KeyNodeConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import java.util.Map;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mission.MissionManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.MainLine;
import protocol.MessageId;
import protocol.RetCodeId;
import protocol.TargetSystem;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_CompleteKeyNodeMission_VALUE)
public class CompleteKeyNodeMissionHandler extends AbstractBaseHandler<MainLine.CS_CompleteKeyNodeMission> {
    @Override
    protected MainLine.CS_CompleteKeyNodeMission parse(byte[] bytes) throws Exception {
        return MainLine.CS_CompleteKeyNodeMission.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, MainLine.CS_CompleteKeyNodeMission req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        MainLine.SC_CompleteKeyNodeMission.Builder msg = MainLine.SC_CompleteKeyNodeMission.newBuilder();
        mainlineEntity mainLine = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        int keyNodeId = mainLine.getDBBuilder().getKeyNodeId();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);

        if (mainLine.getDBBuilder().getCurKeyNodeClaim()){
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
            gsChn.send(MessageId.MsgIdEnum.SC_CompleteKeyNodeMission_VALUE, msg);
            return;
        }

        if (!keyNodeMissionComplete(keyNodeId, target)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_CompleteKeyNodeMission_VALUE, msg);
            return;
        }
        int nextNode = KeyNodeConfig.getInstance().findNextKeyNodeId(keyNodeId);
        if (nextNode == -1) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MessageId.MsgIdEnum.SC_CompleteKeyNodeMission_VALUE, msg);
            return;
        }

        SyncExecuteFunction.executeConsumer(mainLine, cache -> {
            if (keyNodeId== nextNode){
                mainLine.getDBBuilder().setCurKeyNodeClaim(true);
            }
            mainLine.getDBBuilder().setKeyNodeId(nextNode);
        });
        doKeyNodeReward(playerIdx, keyNodeId);
        mainLine.sendKeyNodeMissions();
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_CompleteKeyNodeMission_VALUE, msg);
        LogUtil.info("player:{} complete keyNode missions,before keyNode:{} ,cur keyNode:{}", playerIdx, keyNodeId, mainLine.getDBBuilder().getKeyNodeId());
    }


    private void doKeyNodeReward(String playerIdx, int keyNodeId) {
        KeyNodeConfigObject keyNodeConfig = KeyNodeConfig.getById(keyNodeId);
        if (keyNodeConfig == null) {
            return;
        }
        List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(keyNodeConfig.getReward());
        if (CollectionUtils.isEmpty(rewards)) {
            return;
        }
        RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_MainLineKeyNode), true);
    }


    private boolean keyNodeMissionComplete(int chapter, targetsystemEntity target) {

        List<Integer> missionIds = MissionManager.getInstance().getKeyNodeMissionsByMissionKeyNode(chapter);

        Map<Integer, TargetSystem.TargetMission> missions = target.getDb_Builder().getKeyNodeMissionMap();

        for (Integer missionId : missionIds) {
            TargetSystem.TargetMission targetMission = missions.get(missionId);
            if (targetMission == null || Common.MissionStatusEnum.MSE_UnFinished == targetMission.getStatus()) {
                return false;
            }

        }
        return true;
    }


    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MessageId.MsgIdEnum.SC_CompleteKeyNodeMission_VALUE, MainLine.SC_CompleteKeyNodeMission.newBuilder());
    }
}
