package server.handler.activity.demonDescends;

import cfg.DemonDescendsConfig;
import cfg.DemonDescendsConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.ranking.RankingUtils;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.ActivityParticipateLog;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_DrawDemonDescends;
import protocol.Activity.DemonDescendsRandom;
import protocol.Activity.SC_DemonDescendsRewardRecord;
import protocol.Activity.SC_DrawDemonDescends;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_DemonDescendsActivityInfo;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020.10.08
 */
@MsgId(msgId = MsgIdEnum.CS_DrawDemonDescends_VALUE)
public class DrawDemonDescendsHandler extends AbstractBaseHandler<CS_DrawDemonDescends> {
    @Override
    protected CS_DrawDemonDescends parse(byte[] bytes) throws Exception {
        return CS_DrawDemonDescends.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DrawDemonDescends req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_DrawDemonDescends.Builder resultBuilder = SC_DrawDemonDescends.newBuilder();
        RetCodeEnum retCode = doFunction(playerIdx, req);
        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_DrawDemonDescends_VALUE, resultBuilder);
    }

    private RetCodeEnum doFunction(String playerIdx, CS_DrawDemonDescends req) {
        if (playerIdx == null || req == null || req.getDrawCount() <= 0) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        if (activity == null || activity.getType() != ActivityTypeEnum.ATE_DemonDescends
                || !ActivityUtil.activityInOpen(activity)) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        List<DemonDescendsRandom> randoms = randomRewards(activity.getDemonDescentsRandomList(), req.getDrawCount());
        if (CollectionUtils.size(randoms) != req.getDrawCount()) {
            return RetCodeEnum.RCE_UnknownError;
        }

        List<Reward> rewards = new ArrayList<>();
        List<Reward> grandPrize = new ArrayList<>();
        for (DemonDescendsRandom random : randoms) {
            Reward reward = RewardUtil.parseRandomRewardToReward(random.getRandomRewards());
            rewards.add(reward);

            if (random.getGrandPrize()) {
                grandPrize.add(reward);
            }
        }

        DemonDescendsConfigObject descendsConfig = DemonDescendsConfig.getById(GameConst.CONFIG_ID);
        Consume consume = ConsumeUtil.parseAndMulti(descendsConfig.getDrawuseitem(), req.getDrawCount());
        if (consume == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DemonDescends);
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }

        RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

        //更新玩家积分
        int newScore = SyncExecuteFunction.executeFunction(entity, e -> {
            DB_DemonDescendsActivityInfo.Builder infoBuilder = entity.getDemonDescendsInfoBuilder(req.getActivityId());
            int addScore = descendsConfig.getEachdrawscore() * req.getDrawCount();
            infoBuilder.setScore(infoBuilder.getScore() + addScore);

            entity.putDemonDescendsInfoBuilder(infoBuilder);

            entity.refreshDemonDescendsActivityInfo(req.getActivityId());
            return infoBuilder.getScore();
        });

        //发送记录
        if (CollectionUtils.isNotEmpty(grandPrize)) {
            SC_DemonDescendsRewardRecord.Builder recordBuilder = SC_DemonDescendsRewardRecord.newBuilder();
            recordBuilder.setPlayerName(PlayerUtil.queryPlayerName(playerIdx));
            recordBuilder.addAllRewards(grandPrize);
            GlobalData.getInstance().sendMsgToAllOnlinePlayer(MsgIdEnum.SC_DemonDescendsRewardRecord, recordBuilder);
        }

        //更新排行榜,最低上榜限制
        if (newScore >= descendsConfig.getRankingneedminscore()) {
            RankingManager.getInstance().updatePlayerRankingScore(playerIdx, activity.getRankingType(),
                    RankingUtils.getActivityRankingName(activity), newScore, 0);
        }
        //魔灵降临活动抽奖次数统计
        LogService.getInstance().submit(new ActivityParticipateLog(playerIdx, activity.getType(), activity.getActivityId(), req.getDrawCount()));
        return RetCodeEnum.RCE_Success;
    }

    private List<DemonDescendsRandom> randomRewards(List<DemonDescendsRandom> randomList, int drawCount) {
        if (CollectionUtils.isEmpty(randomList) || drawCount <= 0) {
            return null;
        }

        List<DemonDescendsRandom> result = new ArrayList<>();
        if (randomList.size() == 1) {
            for (int i = 0; i < drawCount; i++) {
                result.addAll(randomList);
            }
        } else {
            //计算总概率
            int totalOdds = randomList.stream().map(e -> e.getRandomRewards().getRandomOdds()).reduce(Integer::sum).orElse(0);
            if (totalOdds <= 0) {
                LogUtil.error("server.handler.activity.demonDescends.DrawDemonDescendsHandler.randomRewards, totalOdds is less than 0");
                return null;
            }

            Random random = new Random();
            for (int i = 0; i < drawCount; i++) {
                int randomNum = random.nextInt(totalOdds);
                int curSumNum = 0;
                for (DemonDescendsRandom demonDescendsRandom : randomList) {
                    if ((curSumNum += demonDescendsRandom.getRandomRewards().getRandomOdds()) > randomNum) {
                        result.add(demonDescendsRandom);
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
