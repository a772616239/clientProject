package server.handler.bossTower;

import cfg.BossTowerBuyTimeConsume;
import cfg.BossTowerBuyTimeConsumeObject;
import cfg.GameConfig;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.bosstower.dbCache.bosstowerCache;
import model.bosstower.entity.bosstowerEntity;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.player.util.PlayerUtil;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.BossTower.CS_BuyBossTower;
import protocol.BossTower.SC_BuyBossTower;
import protocol.BossTower.SC_SweepBossTower;
import protocol.BossTowerDB.DB_BossTowerPassCondition;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author luoyun
 * @date 2021/06/25
 */
@MsgId(msgId = MsgIdEnum.CS_BuyBossTower_VALUE)
public class BuyBossTowerHandler extends AbstractBaseHandler<CS_BuyBossTower> {
    @Override
    protected CS_BuyBossTower parse(byte[] bytes) throws Exception {
        return CS_BuyBossTower.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyBossTower req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_BuyBossTower.Builder resultBuilder = SC_BuyBossTower.newBuilder();
        resultBuilder.setRetCode(GameUtil.buildRetCode(doExecute(playerIdx, req)));
        gsChn.send(MsgIdEnum.SC_BuyBossTower_VALUE, resultBuilder);
    }

    private RetCodeEnum doExecute(String playerIdx, CS_BuyBossTower req) {

        int time = req.getTime();
        if (time > 10000) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        if (time <= 0) {
            time = 1;
        }

        boolean itemBuy = req.getType() == 1;
        VIPConfigObject vipCfg = VIPConfig.getById(PlayerUtil.queryPlayerVipLv(playerIdx));
        if (vipCfg == null) {
            return RetCodeEnum.RCE_Failure;
        }

        bosstowerEntity entity = bosstowerCache.getByIdx(playerIdx);
        if (entity == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        DB_BossTowerPassCondition b = entity.getDbBuilder().getPassMap().get(req.getId());
        if (b == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        itembagEntity bag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (bag == null) {
            return RetCodeEnum.RCE_Failure;
        }
        int[] consumeIntArr;
        List<Consume> consume;
        if (itemBuy) {
            consumeIntArr = GameConfig.getById(GameConst.CONFIG_ID).getBosstower_buyitem();
            consume = Collections.singletonList(ConsumeUtil.parseAndMulti(consumeIntArr, time));
        } else {
            int canBuyTime = vipCfg.getPlusboostowertimes();
            int haveBuy = entity.getDbBuilder().getTodayVipBuyTime();
            if (haveBuy + time > canBuyTime) {
                return RetCodeEnum.RCE_Failure;
            }
            consume = getBuyConsume(haveBuy + 1, time);
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_BossTower);

        if (!ConsumeManager.getInstance().consumeMaterialByList(entity.getPlayeridx(), consume, reason)) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }

        int finalTime = time;
        SyncExecuteFunction.executeFunction(entity, e -> {
            entity.addBuyTimes(req.getId(), finalTime, itemBuy);
            return RetCodeEnum.RCE_Success;
        });

        return RetCodeEnum.RCE_Success;
    }

    private List<Consume> getBuyConsume(int curBuyIndex, int times) {
        BossTowerBuyTimeConsumeObject cfg;
        List<Consume> consumes = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            cfg = BossTowerBuyTimeConsume.getById(curBuyIndex++);
            if (cfg != null) {
                consumes.add(ConsumeUtil.parseConsume(cfg.getConsume()));
            } else {
                consumes.add(ConsumeUtil.parseConsume(BossTowerBuyTimeConsume.getMaxConsume()));

            }
        }
        return ConsumeUtil.mergeConsume(consumes);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.BossTower;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyBossTower_VALUE, SC_SweepBossTower.newBuilder().setRetCode(retCode));
    }
}
