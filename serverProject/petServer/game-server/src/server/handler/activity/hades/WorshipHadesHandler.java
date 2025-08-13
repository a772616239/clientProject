package server.handler.activity.hades;

import cfg.HadesWorshipConfig;
import cfg.HadesWorshipConfigObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Random;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.CS_WorshipHades;
import protocol.Activity.SC_HadesRewardsRecord;
import protocol.Activity.SC_WorshipHades;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward.Builder;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_HadesActivityInfo;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020.10.12
 */
@MsgId(msgId = MsgIdEnum.CS_WorshipHades_VALUE)
public class WorshipHadesHandler extends AbstractBaseHandler<CS_WorshipHades> {
    @Override
    protected CS_WorshipHades parse(byte[] bytes) throws Exception {
        return CS_WorshipHades.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_WorshipHades req, int i) {
        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        SC_WorshipHades.Builder resultBuilder = SC_WorshipHades.newBuilder();
        if (!ActivityUtil.activityInOpen(activity)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_WorshipHades_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_WorshipHades_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_HadesActivityInfo.Builder infoBuilder = entity.getHadesActivityInfoBuilder(req.getActivityId());
            if (infoBuilder.getRemainTimes() <= 0) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Hades_RemainNoWorshipTimes));
                gsChn.send(MsgIdEnum.SC_WorshipHades_VALUE, resultBuilder);
                return;
            }

            HadesWorshipConfigObject config = HadesWorshipConfig.getNextWorshipConfig(infoBuilder.getAlreadyWorshipTimes());
            int[] result = doWorship(config);
            if (result == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_WorshipHades_VALUE, resultBuilder);
                return;
            }

            Consume consume = ConsumeUtil.parseConsume(config.getConsume());
            if (consume == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
                gsChn.send(MsgIdEnum.SC_WorshipHades_VALUE, resultBuilder);
                return;
            }

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_HadesTreasure);
            if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
                gsChn.send(MsgIdEnum.SC_WorshipHades_VALUE, resultBuilder);
                return;
            }

            int rewardCount = (consume.getCount() * result[0]) / 100;
            Builder rewardBuilder = RewardUtil.parseRewardBuilder(config.getConsume());
            rewardBuilder.setCount(rewardCount);

            RewardManager.getInstance().doReward(playerIdx, rewardBuilder.build(), reason, true);

            //修改次数
            infoBuilder.setAlreadyWorshipTimes(infoBuilder.getAlreadyWorshipTimes() + 1);
            infoBuilder.setRemainTimes(infoBuilder.getRemainTimes() - 1);
            entity.putHadesActivityInfoBuilder(infoBuilder);

            entity.sendHadesActivityInfo(req.getActivityId());

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_WorshipHades_VALUE, resultBuilder);

            //发送记录
            SC_HadesRewardsRecord.Builder builder = SC_HadesRewardsRecord.newBuilder();
            builder.setPlayerIdx(playerIdx);
            builder.setPlayerName(PlayerUtil.queryPlayerName(playerIdx));
            builder.setConsume(consume);
            builder.setReward(rewardBuilder);
            builder.setNeedMarquee(result[2] == 1);
            GlobalData.getInstance().sendMsgToAllOnlinePlayer(MsgIdEnum.SC_HadesRewardsRecord, builder);
        });
    }

    private int[] doWorship(HadesWorshipConfigObject config) {
        if (config == null) {
            return null;
        }

        int totalOdds = calculateAllOdds(config.getReturnrewards());
        if (totalOdds == -1) {
            return null;
        }

        int randomNumber = new Random().nextInt(totalOdds);
        int curNum = 0;
        for (int[] returnReward : config.getReturnrewards()) {
            if ((curNum += returnReward[1]) > randomNumber) {
                return returnReward;
            }
        }
        return null;
    }

    /**
     * {{返还倍数(百分比),概率,是否需要播放跑马灯(1是,0否)}}
     *
     * @param returnConfig
     * @return
     */
    private int calculateAllOdds(int[][] returnConfig) {
        if (returnConfig == null) {
            return -1;
        }
        int totalOdds = 0;
        for (int[] ints : returnConfig) {
            if (ints.length < 3) {
                LogUtil.error("server.handler.activity.hades.WorshipHadesHandler.calculateAllOdds, please check returnRewards cfg, cfg length is less than 3");
                return -1;
            }
            totalOdds += ints[1];
        }
        return totalOdds;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
