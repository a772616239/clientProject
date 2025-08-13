package server.handler.activity.runeTreasure;

import cfg.RuneTreasure;
import cfg.RuneTreasureObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_DrawRuneTreasure;
import protocol.Activity.RuneTreasurePool;
import protocol.Activity.SC_DrawRuneTreasure;
import protocol.Activity.SC_RuneTreasureRecord;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_RuneTreasureInfo;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author huhan
 * @date 2020/11/26
 */
@MsgId(msgId = MsgIdEnum.CS_DrawRuneTreasure_VALUE)
public class DrawRuneTreasureHandler extends AbstractBaseHandler<CS_DrawRuneTreasure> {
    @Override
    protected CS_DrawRuneTreasure parse(byte[] bytes) throws Exception {
        return CS_DrawRuneTreasure.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DrawRuneTreasure req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_DrawRuneTreasure.Builder resultBuilder = SC_DrawRuneTreasure.newBuilder();
        if (!ActivityUtil.activityInOpen(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_DrawRuneTreasure_VALUE, resultBuilder);
            return;
        }
        if (entity == null
                || activityCfg.getType() != ActivityTypeEnum.ATE_RuneTreasure
                || req.getDrawTimes() <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_DrawRuneTreasure_VALUE, resultBuilder);
            return;
        }

        List<RuneTreasurePool> randomRewards = randomRewards(activityCfg.getRuneTreasurePoolList(), req.getDrawTimes());
        if (CollectionUtils.isEmpty(randomRewards)
                || randomRewards.size() != req.getDrawTimes()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DrawRuneTreasure_VALUE, resultBuilder);
            return;
        }

        if (!consumeMaterial(playerIdx, req.getDrawTimes(), req.getUseDiamond())) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_DrawRuneTreasure_VALUE, resultBuilder);
            return;
        }

        List<Reward> rewardsList = new ArrayList<>();
        for (RuneTreasurePool randomReward : randomRewards) {
            Reward reward = RewardUtil.parseRandomRewardToReward(randomReward.getReward());
            rewardsList.add(reward);

            if (randomReward.getLimited()) {
                resultBuilder.addLimit(reward);
            } else {
                resultBuilder.addCommon(reward);
            }
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RuneTreasure);
        RewardManager.getInstance().doRewardByList(playerIdx, rewardsList, reason, false);

        //修改进度
        int newTimes = SyncExecuteFunction.executeFunction(entity, e -> {
            DB_RuneTreasureInfo.Builder builder = entity.getDbRuneTreasureInfoBuilder(req.getActivityId());
            builder.setDrawTimes(builder.getDrawTimes() + req.getDrawTimes());
            entity.putRuneTreasureInfoBuilder(builder);

            return builder.getDrawTimes();
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setNewDrawTimes(newTimes);
        gsChn.send(MsgIdEnum.SC_DrawRuneTreasure_VALUE, resultBuilder);

        if (resultBuilder.getLimitCount() > 0) {
            SC_RuneTreasureRecord.Builder builder = SC_RuneTreasureRecord.newBuilder();
            builder.setPlayerName(PlayerUtil.queryPlayerName(playerIdx));
            builder.addAllRewards(resultBuilder.getLimitList());
            GlobalData.getInstance().sendMsgToAllOnlinePlayer(MsgIdEnum.SC_RuneTreasureRecord, builder);
        }
    }

    private boolean consumeMaterial(String playerIdx, int drawTimes, boolean useDiamond) {
        itembagEntity entity = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (entity == null) {
            return false;
        }
        RuneTreasureObject runeTreasureCfg = RuneTreasure.getById(GameConst.CONFIG_ID);

        Consume firstConsume = ConsumeUtil.parseConsume(runeTreasureCfg.getDrawprice());
        if (firstConsume == null) {
            return false;
        }
        int firstCanConsumeCount = ConsumeManager.getInstance().canConsumeCount(playerIdx, firstConsume);
        if (firstCanConsumeCount < drawTimes && !useDiamond) {
            return false;
        }

        List<Consume> finalConsume = new ArrayList<>();
        int remainUseDiamond = drawTimes;
        if (firstCanConsumeCount > 0) {
            int useItemCount = Math.min(firstCanConsumeCount, drawTimes);
            finalConsume.add(ConsumeUtil.multiConsume(firstConsume, useItemCount));
            remainUseDiamond -= useItemCount;
        }

        if (remainUseDiamond > 0) {
            Consume secondConsume = ConsumeUtil.parseAndMulti(runeTreasureCfg.getKeyprice(), remainUseDiamond);
            if (secondConsume == null) {
                return false;
            }
            finalConsume.add(secondConsume);
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RuneTreasure);
        return ConsumeManager.getInstance().consumeMaterialByList(playerIdx, finalConsume, reason);
    }


    private List<RuneTreasurePool> randomRewards(List<RuneTreasurePool> randomList, int drawCount) {
        if (CollectionUtils.isEmpty(randomList) || drawCount <= 0) {
            return null;
        }

        List<RuneTreasurePool> result = new ArrayList<>();
        if (randomList.size() == 1) {
            for (int i = 0; i < drawCount; i++) {
                result.addAll(randomList);
            }
        } else {
            //计算总概率
            int totalOdds = randomList.stream().map(e -> e.getReward().getRandomOdds()).reduce(Integer::sum).orElse(0);
            if (totalOdds <= 0) {
                LogUtil.error("server.handler.activity.runeTreasure.DrawRuneTreasureHandler.randomRewards, totalOdds is less than 0");
                return null;
            }

            Random random = new Random();
            for (int i = 0; i < drawCount; i++) {
                int randomNum = random.nextInt(totalOdds);
                int curSumNum = 0;
                for (RuneTreasurePool runeTreasurePool : randomList) {
                    if ((curSumNum += runeTreasurePool.getReward().getRandomOdds()) > randomNum) {
                        result.add(runeTreasurePool);
                        break;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
