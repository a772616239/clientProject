package server.handler.mainLine;

import cfg.MainLineCheckPoint;
import cfg.MainLineCheckPointDesc;
import cfg.MainLineCheckPointDescObject;
import cfg.MainLineCheckPointObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mainLine.util.MainLineUtil;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MainLine.CS_ClaimMainMissionReward;
import protocol.MainLine.MainLineProgress;
import protocol.MainLine.SC_ClaimMainMissionReward;
import protocol.MainLine.SC_ClaimMainMissionReward.Builder;
import protocol.MainLineDB.DB_MainLine;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.ArrayUtil;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.03.03
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimMainMissionReward_VALUE)
public class ClaimMainMissionRewardHandler extends AbstractBaseHandler<CS_ClaimMainMissionReward> {
    @Override
    protected CS_ClaimMainMissionReward parse(byte[] bytes) throws Exception {
        return CS_ClaimMainMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMainMissionReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder builder = SC_ClaimMainMissionReward.newBuilder();
        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimMainMissionReward_VALUE, builder);
            return;
        }

        MainLineCheckPointDescObject rewardCfg = MainLineCheckPointDesc.getById(req.getIndex());
        if (rewardCfg == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimMainMissionReward_VALUE, builder);
            return;
        }

        RetCodeEnum retCode = SyncExecuteFunction.executeFunction(entity, e -> {
            DB_MainLine.Builder dbBuilder = entity.getDBBuilder();
            if (dbBuilder == null) {
                return RetCodeEnum.RCE_UnknownError;
            }

            if (dbBuilder.getAlreadyGetRewardIndexList().contains(req.getIndex())) {
                return RetCodeEnum.RCE_MainLIne_ThisMainMissionRewardClaimed;
            }

            int alreadyPassed = dbBuilder.getMainLineProBuilder().getAlreadyPassed();
            MainLineCheckPointObject byId = MainLineCheckPoint.getById(alreadyPassed);
            if (byId == null) {
                return RetCodeEnum.RCE_UnknownError;
            }

            //奖励幕中最大的节点必须小于当前关卡的最小节点
            // ，否则大于已通关节点的所有节点都必须在
            if (ArrayUtil.getMaxInt(rewardCfg.getNodelist(), 0)
                    <= ArrayUtil.getMaxInt(byId.getNodelist(), 0)) {

            }

            if (!mainMissionRewardCanGet(rewardCfg, dbBuilder.getMainLinePro())) {
                return RetCodeEnum.RCE_Activity_MissionCanNotClaim;
            }

            dbBuilder.addAlreadyGetRewardIndex(req.getIndex());
            return RetCodeEnum.RCE_Success;
        });

        if (retCode != RetCodeEnum.RCE_Success) {
            builder.setRetCode(GameUtil.buildRetCode(retCode));
            gsChn.send(MsgIdEnum.SC_ClaimMainMissionReward_VALUE, builder);
            return;
        }

        RewardManager.getInstance().doRewardByRewardId(playerIdx, rewardCfg.getFinishedreward(),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MainLineMission), true);

        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_ClaimMainMissionReward_VALUE, builder);
    }

    private boolean mainMissionRewardCanGet(MainLineCheckPointDescObject rewardCfg, MainLineProgress mainLinePro) {
        if (mainLinePro == null || rewardCfg == null) {
            return false;
        }

        MainLineCheckPointObject curPoint = MainLineCheckPoint.getById(mainLinePro.getCurCheckPoint());
        if (curPoint == null) {
            return false;
        }

        int rewardMax = getMaxNode(rewardCfg.getNodelist());
        int curMin = getMinNode(curPoint.getNodelist());
        if (rewardMax > curMin) {
            for (int rewardNode : rewardCfg.getNodelist()) {
                if (MainLineUtil.getNodeType(rewardNode) == 0 || rewardNode < curMin) {
                    continue;
                }

                if (!mainLinePro.getProgressList().contains(rewardNode)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 获取最大节点，不包括空白节点
     *
     * @param nodeList
     * @return
     */
    private int getMaxNode(int[] nodeList) {
        if (nodeList == null) {
            return 0;
        }

        int max = Integer.MIN_VALUE;
        for (int i : nodeList) {
            if (MainLineUtil.getNodeType(i) != 0 && i > max) {
                max = i;
            }
        }
        return max;
    }

    /**
     * 获取最小节点，不包括空白节点
     *
     * @param nodeList
     * @return
     */
    private int getMinNode(int[] nodeList) {
        if (nodeList == null) {
            return 0;
        }

        int min = Integer.MAX_VALUE;
        for (int i : nodeList) {
            if (MainLineUtil.getNodeType(i) != 0 && i < min) {
                min = i;
            }
        }
        return min;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimMainMissionReward_VALUE, SC_ClaimMainMissionReward.newBuilder().setRetCode(retCode));
    }
}
