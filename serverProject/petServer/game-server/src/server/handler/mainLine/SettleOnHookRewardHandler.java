package server.handler.mainLine;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MainLine.CS_SettleOnHookReward;
import protocol.MainLine.SC_SettleOnHookReward;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_SettleOnHookReward_VALUE)
public class SettleOnHookRewardHandler extends AbstractBaseHandler<CS_SettleOnHookReward> {
    @Override
    protected CS_SettleOnHookReward parse(byte[] bytes) throws Exception {
        return CS_SettleOnHookReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SettleOnHookReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_SettleOnHookReward.Builder resultBuilder = SC_SettleOnHookReward.newBuilder();

        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.AutoOnHook)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_SettleOnHookReward_VALUE, resultBuilder);
            return;
        }

        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            LogUtil.error("ClaimMainLineHandler, playerIdx[" + playerIdx + "] mainLineEntity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_SettleOnHookReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            List<Reward> gainRewardList = entity.getOnHookInCome(true);
            RewardManager.getInstance().doRewardByList(playerIdx, gainRewardList,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MainLine_OnHook), true);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_SettleOnHookReward_VALUE, resultBuilder);
        });

        //目标：累积进行x次挂机奖励领取
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuSettleOnHookReward, 1, 0);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_SettleOnHookReward_VALUE, SC_SettleOnHookReward.newBuilder().setRetCode(retCode));
    }
}
