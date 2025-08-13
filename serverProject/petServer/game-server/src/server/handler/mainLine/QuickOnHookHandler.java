package server.handler.mainLine;

import cfg.MainLineQuickOnHook;
import cfg.MainLineQuickOnHookObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Comparator;
import java.util.List;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MainLine.CS_QuickOnHook;
import protocol.MainLine.SC_QuickOnHook;
import protocol.MainLine.SC_QuickOnHook.Builder;
import protocol.MainLineDB.DB_MainLine;
import protocol.MessageId.MsgIdEnum;
import protocol.MonthCard;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_QuickOnHook_VALUE)
public class QuickOnHookHandler extends AbstractBaseHandler<CS_QuickOnHook> {
    @Override
    protected CS_QuickOnHook parse(byte[] bytes) throws Exception {
        return CS_QuickOnHook.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_QuickOnHook req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        mainlineEntity mainLine = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(playerIdx);
        playerEntity player = playerCache.getByIdx(playerIdx);
        Builder resultBuilder = SC_QuickOnHook.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.QuickOnHook)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_QuickOnHook_VALUE, resultBuilder);
            return;
        }

        if (mainLine == null || player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_QuickOnHook_VALUE, resultBuilder);
            return;
        }
        int playerOnHookUpperLimit = getPlayerOnHookUpperLimit(player);
        int todayQuickOnHookTimes = SyncExecuteFunction.executeFunction(mainLine, m -> {
            //检查玩家是否能快速挂机
            if (!mainLine.canQuickOnHook()) {
                return -1;
            }

            return mainLine.getTodayQuickOnHookTimes();
        });

        if (todayQuickOnHookTimes < 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_PlayerHaveNoOnhook));
            gsChn.send(MsgIdEnum.SC_QuickOnHook_VALUE, resultBuilder);
            return;
        }

        int canFreeOnHookTimes = player.queryPrivilegedCardNum(MonthCard.PrivilegedCardFunction.PCF_FreeOnHook);

        if (todayQuickOnHookTimes >= playerOnHookUpperLimit) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MainLine_QuickOnHookLimit));
            gsChn.send(MsgIdEnum.SC_QuickOnHook_VALUE, resultBuilder);
            return;
        }

        int nextTimes = todayQuickOnHookTimes + 1;

        int todayFreeOnHookTime = mainLine.getDBBuilder().getTodayFreeOnHookTime();
        RetCodeEnum consumeMaterial = consumeMaterial(playerIdx, nextTimes, canFreeOnHookTimes, todayFreeOnHookTime);

        if (consumeMaterial != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(consumeMaterial));
            gsChn.send(MsgIdEnum.SC_QuickOnHook_VALUE, resultBuilder);
            return;
        }

        List<Reward> rewards = mainLine.calculateOnHookReward(getOnHookTime(nextTimes) * TimeUtil.MS_IN_A_HOUR);

//        //快速挂机不包括经验值
//        List<Reward> resultRewards = RewardUtil.removeRewardType(rewards, RewardTypeEnum.RTE_EXP);
        RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MainLine_OnHook, "快速"), true);

        //修改当日已挂机次数
        SyncExecuteFunction.executeConsumer(mainLine, m -> {
            boolean useFreeOnHookTimes = todayFreeOnHookTime < canFreeOnHookTimes;
            DB_MainLine.Builder dbBuilder = mainLine.getDBBuilder();
            dbBuilder.setTodayQuickOnHookTimes(nextTimes);
            if (useFreeOnHookTimes) {
                dbBuilder.setTodayFreeOnHookTime(dbBuilder.getTodayFreeOnHookTime() + 1);
            }
        });
        resultBuilder.setFreeQuickTimes(mainLine.getDBBuilder().getTodayFreeOnHookTime());
        resultBuilder.setTodayOnHookTime(mainLine.getDBBuilder().getTodayQuickOnHookTimes());
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_QuickOnHook_VALUE, resultBuilder);

        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_MainLine_QuickOnHook, 1, 0);
    }

    private int getOnHookTime(int nextTimes) {
        MainLineQuickOnHookObject cfg = MainLineQuickOnHook.getById(nextTimes);
        if (cfg == null) {
            Integer cfgId = MainLineQuickOnHook._ix_id.keySet().stream().max(Integer::compareTo).orElse(1);
            cfg = MainLineQuickOnHook.getById(cfgId);
            return cfg == null ? 0 : cfg.getTime();

        }
        return cfg.getTime();
    }

    private RetCodeEnum consumeMaterial(String playerIdx, int nextTimes, int canFreeOnHookTimes, int todayFreeOnHookTime) {
        if (todayFreeOnHookTime < canFreeOnHookTimes) {
            return RetCodeEnum.RCE_Success;
        }

        MainLineQuickOnHookObject quickConsume = getConsumeCfg(nextTimes, todayFreeOnHookTime);
        if (quickConsume == null) {
            return RetCodeEnum.RCE_UnknownError;
        }

        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, ConsumeUtil.parseConsume(quickConsume.getConsumes()),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MainLine_OnHook))) {
            return RetCodeEnum.RCE_Player_CurrencysNotEnought;
        }

        return RetCodeEnum.RCE_Success;
    }

    private MainLineQuickOnHookObject getConsumeCfg(int nextTimes, int todayFreeOnHookTime) {
        int cfgId = Math.max(1, nextTimes - todayFreeOnHookTime);
        MainLineQuickOnHookObject quickConsume = MainLineQuickOnHook.getById(cfgId);
        if (quickConsume == null) {
            quickConsume = MainLineQuickOnHook._ix_id.values().stream()
                    .max(Comparator.comparingInt(MainLineQuickOnHookObject::getId)).orElse(null);
        }
        return quickConsume;
    }

    private int getPlayerOnHookUpperLimit(playerEntity player) {

        VIPConfigObject byId = VIPConfig.getById(player.getVip());
        if (byId == null) {
            return 0;
        }
        return byId.getQuickonhooktimes()
                + player.queryPrivilegedCardNum(MonthCard.PrivilegedCardFunction.PCF_FreeOnHook)
                + player.queryPrivilegedCardNum(MonthCard.PrivilegedCardFunction.PCF_ExOnHookTimes);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MainLine;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_QuickOnHook_VALUE, SC_QuickOnHook.newBuilder().setRetCode(retCode));
    }
}
