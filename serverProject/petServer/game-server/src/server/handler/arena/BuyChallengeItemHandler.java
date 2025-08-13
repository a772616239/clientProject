package server.handler.arena;

import cfg.ArenaConfig;
import cfg.ArenaConfigObject;
import cfg.FunctionOpenLvConfig;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Arena.CS_BuyArenaChallengeItem;
import protocol.Arena.SC_BuyArenaChallengeItem;
import protocol.Arena.SC_BuyArenaChallengeItem.Builder;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.03.10
 */
@MsgId(msgId = MsgIdEnum.CS_BuyArenaChallengeItem_VALUE)
public class BuyChallengeItemHandler extends AbstractBaseHandler<CS_BuyArenaChallengeItem> {
    @Override
    protected CS_BuyArenaChallengeItem parse(byte[] bytes) throws Exception {
        return CS_BuyArenaChallengeItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyArenaChallengeItem req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        Builder resultBuilder = SC_BuyArenaChallengeItem.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.Arena)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_BuyArenaChallengeItem_VALUE, resultBuilder);
            return;
        }

        ArenaConfigObject arenaCfg = ArenaConfig.getById(GameConst.CONFIG_ID);
        arenaEntity entity = arenaCache.getInstance().getEntity(playerIdx);
        if (entity == null || arenaCfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyArenaChallengeItem_VALUE, resultBuilder);
            return;
        }

        if (entity.getDbBuilder().getTodayBuyTicketCount() >= getPlayerBuyLimit(playerIdx)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Store_GoodsBuyUpperLimit));
            gsChn.send(MsgIdEnum.SC_BuyArenaChallengeItem_VALUE, resultBuilder);
            return;
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Arena);
        Consume consume = ConsumeUtil.parseAndMulti(arenaCfg.getTicketprice(), req.getBuyCount());
        if (consume == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyArenaChallengeItem_VALUE, resultBuilder);
            return;
        }

        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_BuyArenaChallengeItem_VALUE, resultBuilder);
            return;
        }

        Reward.Builder rewardBuilder = RewardUtil.parseRewardBuilder(arenaCfg.getChallengeconsume());
        if (rewardBuilder == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_BuyArenaChallengeItem_VALUE, resultBuilder);
            return;
        }

        Reward reward = rewardBuilder.setCount(req.getBuyCount()).build();
        RewardManager.getInstance().doReward(playerIdx, reward, reason, true);

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDbBuilder().setTodayBuyTicketCount(entity.getDbBuilder().getTodayBuyTicketCount() + req.getBuyCount());
        });
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_BuyArenaChallengeItem_VALUE, resultBuilder);
    }

    private int getPlayerBuyLimit(String playerIdx) {
        VIPConfigObject vipConfigObject = VIPConfig.getById(PlayerUtil.queryPlayerVipLv(playerIdx));
        if (vipConfigObject == null) {
            return 0;
        }
        return vipConfigObject.getArenaticketbuylimit();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyArenaChallengeItem_VALUE, SC_BuyArenaChallengeItem.newBuilder().setRetCode(retCode));
    }
}
