package server.handler.ancientCall;

import cfg.AltarConfig;
import cfg.AltarConfigObject;
import cfg.BanShuConfig;
import cfg.BanShuConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.ancientCall.AncientCallManager;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import platform.logs.entity.PetCallLog;
import protocol.AncientCall.CS_CallAncient;
import protocol.AncientCall.SC_CallAncient;
import protocol.AncientCall.SC_RefreshAltarMustGetDrawTimes;
import protocol.AncientCall.SC_RefreshAltarMustGetDrawTimes.Builder;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_CallAncient_VALUE)
public class CallAncientHandler extends AbstractBaseHandler<CS_CallAncient> {
    @Override
    protected CS_CallAncient parse(byte[] bytes) throws Exception {
        return CS_CallAncient.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_CallAncient req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");

        SC_CallAncient.Builder resultBuilder = SC_CallAncient.newBuilder();

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_CallAncient_VALUE, resultBuilder);
            return;
        }

        //判断是否超过上限
        BanShuConfigObject banShuCfg = BanShuConfig.getById(GameConst.CONFIG_ID);
        if (banShuCfg.getAltarlimit() != GameConst.UN_LIMIT
                && (entity.getDb_data().getTodayCallTimes() + req.getDoCount()) > banShuCfg.getAltarlimit()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_BanShu_OutOfLimit));
            gsChn.send(MsgIdEnum.SC_CallAncient_VALUE, resultBuilder);
            return;
        }

        Consume allConsume = ConsumeUtil.multiConsume(getEachCallConsume(req.getType()), req.getDoCount());
        if (allConsume == null || req.getDoCount() <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_CallAncient_VALUE, resultBuilder);
            return;
        }

        //召唤
        List<Reward> rewards = AncientCallManager.getInstance().callAncient(playerIdx, req.getType(), req.getDoCount());
        if (rewards == null || rewards.isEmpty()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_CallAncient_VALUE, resultBuilder);
            return;
        }

        //扣除道具
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, allConsume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_AncientCall))) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_CallAncient_VALUE, resultBuilder);
            return;
        }

        RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_AncientCall), false);

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.addAllRewards(rewards);
        gsChn.send(MsgIdEnum.SC_CallAncient_VALUE, resultBuilder);

        //设置召唤次数
        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.getDb_data().setTodayCallTimes(entity.getDb_data().getTodayCallTimes() + req.getDoCount());
        });

        //刷新保底抽次
        Integer newTimes = entity.getDb_data().getAncientAltar().getMustGetDrawTimesMap().get(req.getType());
        Builder refreshBuilder = SC_RefreshAltarMustGetDrawTimes.newBuilder().setType(req.getType()).setNewTimes(newTimes == null ? 0 : newTimes);
        gsChn.send(MsgIdEnum.SC_RefreshAltarMustGetDrawTimes_VALUE, refreshBuilder);

        //目标：累积进行远古召唤
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuCallAncient, req.getDoCount(), 0);
        LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.AncientCall));
        LogService.getInstance().submit(new PetCallLog(playerIdx, rewards, allConsume, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_AncientCall)));
    }

    private Consume getEachCallConsume(int type) {
        AltarConfigObject callCfg = AltarConfig.getById(type);
        if (callCfg == null) {
            return null;
        }

        return ConsumeUtil.parseConsume(callCfg.getPrice());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.AncientCall;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_CallAncient_VALUE, SC_CallAncient.newBuilder().setRetCode(retCode));
    }
}
