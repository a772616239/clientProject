package server.handler.resourceCopy;

import cfg.FunctionOpenLvConfig;
import cfg.ResourceCopy;
import cfg.ResourceCopyConfig;
import cfg.ResourceCopyObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.battle.pve.ResourceCopyBattleController;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.TargetSystemUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.StatisticsLogUtil;
import platform.logs.entity.GamePlayLog;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_ResourceCopy;
import protocol.ResourceCopy.CS_SweepCopy;
import protocol.ResourceCopy.ResourceCopyTypeEnum;
import protocol.ResourceCopy.SC_SweepCopy;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_SweepCopy_VALUE)
public class SweepCopyHandler extends AbstractBaseHandler<CS_SweepCopy> {

    @Override
    protected CS_SweepCopy parse(byte[] bytes) throws Exception {
        return CS_SweepCopy.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SweepCopy req, int i) {
        ResourceCopyTypeEnum type = req.getType();
        int index = req.getIndex();

        SC_SweepCopy.Builder resultBuilder = SC_SweepCopy.newBuilder();

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("SweepCopyHandler, playerIdx = " + playerIdx + ", entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_SweepCopy_VALUE, resultBuilder);
            return;
        }

        if (PlayerUtil.queryFunctionLock(playerIdx,EnumFunction.ResCopy)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_SweepCopy_VALUE, resultBuilder);
            return;
        }

        ResourceCopyObject cfg = ResourceCopy.getInstance().getCopyCfgByTypeAndIndex(type.getNumber(), index);
        if (cfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_SweepCopy_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_ResourceCopy.Builder resourceCopy = player.getResourceCopyData(type.getNumber());
            if (resourceCopy == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_SweepCopy_VALUE, resultBuilder);
                return;
            }

            if (resourceCopy.getChallengeTimes() >=
                    ResourceCopyConfig.getById(type.getNumber()).getChallengetimes() + resourceCopy.getBuyTimes()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ResCopy_ChallengeTimesLimit));
                gsChn.send(MsgIdEnum.SC_SweepCopy_VALUE, resultBuilder);
                return;
            }

            resourceCopy.setChallengeTimes(resourceCopy.getChallengeTimes() + 1);

            resultBuilder.setNewChallengeTimes(resourceCopy.getChallengeTimes());

            List<Reward> rewardsByFightMakeId = RewardUtil.getRewardsByFightMakeId(cfg.getFightmakeid());

            rewardsByFightMakeId = RewardUtil.additionResourceCopyRewardByVip(player.getVip(), rewardsByFightMakeId);

            if (!RewardManager.getInstance().doRewardByList(playerIdx, rewardsByFightMakeId,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_WipeResCopy,
                            StatisticsLogUtil.getResCopyName(cfg.getType()) + "扫荡"), true)) {
                LogUtil.error("SweepCopyHandler, playerIdx = " + playerIdx + ", doReward error");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_SweepCopy_VALUE, resultBuilder);
                return;
            }
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_SweepCopy_VALUE, resultBuilder);
        });

        //目标：累积通过x次x级副本
        TargetTypeEnum targetTypeEnum = TargetSystemUtil.getTargetTypeByResCopyType(type);
        EventUtil.triggerUpdateTargetProgress(player.getIdx(), targetTypeEnum, 1, index);
        //目标：累积参加资源副本
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuJoinResCopy, 1, 0);
        //玩法统计
        LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.ResCopy));
        LogService.getInstance().submit(new GamePlayLog(playerIdx, ResourceCopyBattleController.getFunctionByResType(req.getTypeValue())));
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ResCopy;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_SweepCopy_VALUE, SC_SweepCopy.newBuilder().setRetCode(retCode));
    }
}
