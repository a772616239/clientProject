package server.handler.player;

import cfg.GoldExchange;
import cfg.GoldExchangeObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.mainLine.util.MainLineUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.GamePlayLog;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_PlayerData.Builder;
import protocol.PlayerInfo.CS_GoldExchange;
import protocol.PlayerInfo.SC_GoldExchange;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_GoldExchange_VALUE)
public class GoldExchangeHandler extends AbstractBaseHandler<CS_GoldExchange> {
    @Override
    protected CS_GoldExchange parse(byte[] bytes) throws Exception {
        return CS_GoldExchange.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GoldExchange req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_GoldExchange.Builder resultBuilder = SC_GoldExchange.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.error("GoldExchangeHandler, playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_GoldExchange_VALUE, resultBuilder);
            return;
        }

        int playerVipLv = player.getVip();
        VIPConfigObject vipCfg = VIPConfig.getById(playerVipLv);
        if (vipCfg == null) {
            LogUtil.error("GoldExchangeHandler, VipCfg is null, vipLv[" + playerVipLv + "]");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MsgIdEnum.SC_GoldExchange_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            Builder db_data = player.getDb_data();
            if (db_data == null) {
                LogUtil.error("GoldExchangeHandler, playerIdx[" + playerIdx + "] dbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_GoldExchange_VALUE, resultBuilder);
                return;
            }

            int goldExTimes = db_data.getGoldExchange().getGoldExTimes();
            if (goldExTimes >= vipCfg.getGoldexupperlimit()) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_GoldEx_ExUpperLimit));
                gsChn.send(MsgIdEnum.SC_GoldExchange_VALUE, resultBuilder);
                return;
            }

            GoldExchangeObject exCfg = getExCfg(goldExTimes);
            int canGetGoldCount = calculateCanGetGoldCount(exCfg, db_data.getGoldExchange().getOutputRate(), vipCfg, req.getIsMultiple());
            if (canGetGoldCount <= 0) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_GoldExchange_VALUE, resultBuilder);
                return;
            }

            int consumeDiamond = req.getIsMultiple() ? exCfg.getMultipleexneeddiamond() : exCfg.getExneeddiamond();
            if (!player.consumeCurrency(RewardTypeEnum.RTE_Diamond, consumeDiamond, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GoldExchange))) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_DiamondNotEnought));
                gsChn.send(MsgIdEnum.SC_GoldExchange_VALUE, resultBuilder);
                return;
            }

            player.addCurrency(RewardTypeEnum.RTE_Gold, canGetGoldCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GoldExchange));

            db_data.getGoldExchangeBuilder().setGoldExTimes(++goldExTimes);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setNewInfo(db_data.getGoldExchange());
            gsChn.send(MsgIdEnum.SC_GoldExchange_VALUE, resultBuilder);

            GlobalData.getInstance().sendDisRewardMsg(playerIdx,
                    RewardUtil.parseReward(RewardTypeEnum.RTE_Gold, 0, canGetGoldCount), RewardSourceEnum.RSE_GoldExchange);
        });

        //每日：金币兑换次数
        EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuExchangeGold, 1, 0);
        LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.GoldExchange));
    }

    /**
     * 根据当前兑换次数,得到下次兑换配置
     *
     * @param exTimes
     * @return
     */
    private GoldExchangeObject getExCfg(int exTimes) {
        int nextExTimes = exTimes + 1;
        GoldExchangeObject exCfg = GoldExchange.getByExchangetimes(nextExTimes);
        if (exCfg == null) {
            return GoldExchange.maxTimesCfg;
        }
        return exCfg;
    }

    /**
     * 基础倍数 * vip加成 * 多倍
     *
     * @param exCfg      兑换配置
     * @param playerRate 玩家产出速率
     * @param vipCfg
     * @param multi      是否加倍
     * @return
     */
    private int calculateCanGetGoldCount(GoldExchangeObject exCfg, int playerRate, VIPConfigObject vipCfg, boolean multi) {
        if (exCfg == null || vipCfg == null || playerRate <= 0) {
            return 0;
        }

        //基础总产出 = （兑换配置时间 / 主线计算间隔） * 玩家产出
        float baseCount = ((exCfg.getTime() * TimeUtil.MS_IN_A_S * 1.0F) / MainLineUtil.getOnHookCalculateInterval()) * playerRate;

        if (exCfg.getVipaddtion()) {
            baseCount = (baseCount * (100 + vipCfg.getGlodexaddtion())) / 100;
        }

        if (multi) {
            baseCount = baseCount * (exCfg.getMultiplenumber() / 1000);
        }

        return GameUtil.floatToInt(baseCount);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.GoldExchange;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_GoldExchange_VALUE, SC_GoldExchange.newBuilder().setRetCode(retCode));
    }
}
