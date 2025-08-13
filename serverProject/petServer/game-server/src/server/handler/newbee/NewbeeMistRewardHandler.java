package server.handler.newbee;

import protocol.Common.EnumFunction;
import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.Newbee.CS_ClaimNewbeeMistReward;
import protocol.Newbee.SC_ClaimNewbeeMistReward;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimNewbeeMistReward_VALUE)
public class NewbeeMistRewardHandler extends AbstractBaseHandler<CS_ClaimNewbeeMistReward> {
    @Override
    protected CS_ClaimNewbeeMistReward parse(byte[] bytes) throws Exception {
        return CS_ClaimNewbeeMistReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimNewbeeMistReward req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerId);
        SC_ClaimNewbeeMistReward.Builder builder = SC_ClaimNewbeeMistReward.newBuilder();
        try {
            if (player == null) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Failure));
                gsChn.send(MsgIdEnum.SC_ClaimNewbeeMistReward_VALUE, builder);
                return;
            }
            if (player.getDb_data().getClaimedMistNewbeeReward()) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_NewBee_ClaimedMistNewbeeReward));
                gsChn.send(MsgIdEnum.SC_ClaimNewbeeMistReward_VALUE, builder);
                return;
            }
            List<Reward> rewardList = RewardUtil.parseRewardIntArrayToRewardList(GameConfig.getById(1).getMistnewbiereward());
            if (CollectionUtils.isEmpty(rewardList)) {
                LogUtil.error("Player claim MistNewbieReward config is null,playerIdx=" + playerId);
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_NewBee_MistRewardCfgError));
                gsChn.send(MsgIdEnum.SC_ClaimNewbeeMistReward_VALUE, builder);
                return;
            }

            for (int type = 0; type < req.getBagCountCount(); type++) {
                if (type > 2 || req.getBagCount(type) <= 0) {
                    break;
                }
                Reward.Builder reward = Reward.newBuilder();
                reward.setCount(req.getBagCount(type));
                if (type == 0) {
                    reward.setRewardType(RewardTypeEnum.RTE_Gold);
                } else if (type == 1) {
                    reward.setRewardType(RewardTypeEnum.RTE_Item);
                    reward.setId(1014); // 写死
                } else if (type == 2) {
                    reward.setRewardType(RewardTypeEnum.RTE_Diamond);
                }
                rewardList.add(reward.build());
            }
            RewardManager.getInstance().doRewardByList(
                    playerId, rewardList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_NewBee), false);

            SyncExecuteFunction.executeConsumer(player, entity -> entity.getDb_data().setClaimedMistNewbeeReward(true));
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimNewbeeMistReward_VALUE, builder);
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimNewbeeMistReward_VALUE, builder);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
