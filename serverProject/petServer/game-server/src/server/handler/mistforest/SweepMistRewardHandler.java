package server.handler.mistforest;

import cfg.MistWordMapInfoConfig;
import cfg.MistWordMapInfoConfigObject;
import cfg.MistWorldMapConfig;
import cfg.MistWorldMapConfigObject;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_SweepMistReward;
import protocol.MistForest.EnumMistRuleKind;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_SweepMistReward;
import util.GameUtil;
import util.RandomUtil;

@MsgId(msgId = MsgIdEnum.CS_SweepMistReward_VALUE)
public class SweepMistRewardHandler extends AbstractBaseHandler<CS_SweepMistReward> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_SweepMistReward_VALUE, SC_SweepMistReward.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }

    @Override
    protected CS_SweepMistReward parse(byte[] bytes) throws Exception {
        return CS_SweepMistReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SweepMistReward req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (target == null) {
            return;
        }
        int sweepLevel = target.getDb_Builder().getMistTaskDataBuilder().getCurEnterLevel();
        SC_SweepMistReward.Builder builder = SC_SweepMistReward.newBuilder();
//        Builder curTaskBuilder = target.getCurSweepMistTaskData();
//        int sweepLevel = target.getDb_Builder().getMistTaskDataBuilder().getCurEnterLevel();
//        int sweepLevel = target.getDb_Builder().getMistTaskDataBuilder().getCurEnterLevel() - 1;
//        if (curTaskBuilder != null && curTaskBuilder.getSweepMissionState() == MissionStatusEnum.MSE_Finished) {
//            sweepLevel++;
//        }
        MistWorldMapConfigObject mapCfg = MistWorldMapConfig.getInstance().getCfgByRuleAndLevel(EnumMistRuleKind.EMRK_Common_VALUE, sweepLevel);
        if (mapCfg == null) {
            builder.setRetCode(MistRetCode.MRC_NotFoundMistLevel);
            gsChn.send(MsgIdEnum.SC_SweepMistReward_VALUE, builder);
            return;
        }
        MistWordMapInfoConfigObject cfg = MistWordMapInfoConfig.getByMapid(mapCfg.getMapid());
        if (cfg == null) {
            builder.setRetCode(MistRetCode.MRC_NotFoundMistLevel);
            gsChn.send(MsgIdEnum.SC_SweepMistReward_VALUE, builder);
            return;
        }
        List<Consume> consumes = ConsumeUtil.parseToConsumeList(cfg.getSweepconsume());
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest))) {
            builder.setRetCode(MistRetCode.MRC_StaminaNotEnough);
            gsChn.send(MsgIdEnum.SC_SweepMistReward_VALUE, builder);
            return;
        }
        List<Reward> rewards = getSweepRewards(cfg);
        if (!CollectionUtils.isEmpty(rewards)) {
            RewardManager.getInstance().doRewardByList(playerId, rewards,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest), true);
        }
        builder.setRetCode(MistRetCode.MRC_Success);
        gsChn.send(MsgIdEnum.SC_SweepMistReward_VALUE, builder);
    }

    protected List<Reward> getSweepRewards(MistWordMapInfoConfigObject cfg) {
        if (cfg == null) {
            return null;
        }
        Reward reward;
        List<Reward> rewardList = new ArrayList<>();
        for (int i = 0; i < cfg.getSweeprewardcount(); i++) {
            reward = generateReward(cfg.getSweepreward());
            if (reward != null) {
                rewardList.add(reward);
            }
        }
        return rewardList;
    }

    protected Reward generateReward(int[][] reward) {
        if (reward == null) {
            return null;
        }
        int weight = 0;
        int rand = RandomUtil.getRandom1000();
        for (int i = 0; i < reward.length; i++) {
            if (reward[i] == null || reward[i].length < 4) {
                continue;
            }
            weight += reward[i][3];
            if (weight > rand) {
                return RewardUtil.parseReward(reward[i]);
            }
        }
        return null;
    }
}
