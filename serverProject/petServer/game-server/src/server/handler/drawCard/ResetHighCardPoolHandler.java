package server.handler.drawCard;

import cfg.DrawCard;
import cfg.DrawHighCardConfig;
import cfg.DrawHighCardConfigObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Collection;
import model.drawCard.DrawCardManager;
import model.drawCard.DrawCardUtil;
import model.drawCard.OddsRandom;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import protocol.Common.EnumFunction;
import protocol.DrawCard.CS_ResetHighCardPool;
import protocol.DrawCard.SC_ResetHighCardPool;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_DrawCardData;
import protocol.PlayerDB.DB_HighCard;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

/**
 * 重置远古召唤
 */
@MsgId(msgId = MsgIdEnum.CS_ResetHighCardPool_VALUE)
public class ResetHighCardPoolHandler extends AbstractBaseHandler<CS_ResetHighCardPool> {
    @Override
    protected CS_ResetHighCardPool parse(byte[] bytes) throws Exception {
        return CS_ResetHighCardPool.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ResetHighCardPool req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ResetHighCardPool.Builder resultBuilder = SC_ResetHighCardPool.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.DrawCard)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_ClaimDrawCardInfo_VALUE, resultBuilder);
            return;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.info("playerIdx [" + playerIdx + "] entity or itemBag is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ResetHighCardPool_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_DrawCardData.Builder drawCardBuilder = player.getDb_data().getDrawCardBuilder();
            int openHighCardNeedExp = getOpenHighCardNeedExp(drawCardBuilder.getHighOpenedTimes());
            if (drawCardBuilder.getCumulateExp() < openHighCardNeedExp) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
                gsChn.send(MsgIdEnum.SC_ResetHighCardPool_VALUE, resultBuilder);
                return;
            }
            int corePetFloorTimesLimit = DrawCard.getById(GameConst.CONFIG_ID).getHighcardcorefloortimes();
            int highCardFloorTimes = drawCardBuilder.getHighCardFloorTimes();
            boolean mustDrawCorePet = highCardFloorTimes >= corePetFloorTimesLimit;
            Collection<OddsRandom> randomResult = DrawCardManager.getInstance().drawHighCard(playerIdx, mustDrawCorePet);
            //如果奖励列表为空,则未随机成功
            if (GameUtil.collectionIsEmpty(randomResult)) {
                LogUtil.error("random high card reward pool error");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ResetHighCardPool_VALUE, resultBuilder);
                return;
            }

            boolean containCorePet = false;
            for (OddsRandom oddsRandom : randomResult) {
                if (oddsRandom.getIscorepet()) {
                    containCorePet = true;
                    break;
                }
            }
            if (containCorePet) {
                drawCardBuilder.clearHighCardFloorTimes();
            } else {
                drawCardBuilder.setHighCardFloorTimes(++highCardFloorTimes);
            }

            drawCardBuilder.setHighOpenedTimes(drawCardBuilder.getHighOpenedTimes() + 1);
            //扣除经验值
            player.addDrawCardExp(-openHighCardNeedExp);

//            //随机出特殊处理品质后后需要清空消费金额计数
//            if (DrawCardUtil.containSpecifyQuality(randomResult,
//                    DrawCard.getById(GameConst.CONFIG_ID).getHighcardspecialquality())) {
//                drawCardBuilder.clearDrawCardConsume();
//            }

            //刷新高级抽卡奖次
            drawCardBuilder.clearHighCards();
            randomResult.forEach((e) -> {
                DB_HighCard.Builder builder = DB_HighCard.newBuilder();
                builder.setClaimed(false);
                builder.setReward(RewardUtil.parseReward(e.getRewards()));

                DrawHighCardConfigObject highCfg = DrawHighCardConfig.getById(e.getId());
                builder.setOdds(highCfg != null ? highCfg.getRouletterate() : e.getOdds());

                builder.setIndex(e.getId());
                builder.setQuality(e.getQuality());
//                LogUtil.debug("drawCard.ResetHighCardPoolHandler, playerIdx:"
//                        + playerIdx + "add high card:" + builder.toString());
                drawCardBuilder.addHighCards(builder);
            });

            //如果是特殊处理轮次,重置概率
            int[] config = DrawCardUtil.getHighSpecialDealConfig(drawCardBuilder.getHighOpenedTimes());
            if (config != null) {
                drawCardBuilder.setCurHighPoolSpecialDealRedQuality(config[1]);
            }

            resultBuilder.addAllRewardPool(player.getHighCardPool());
            resultBuilder.setHighPoolOpenTimes(drawCardBuilder.getHighOpenedTimes());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ResetHighCardPool_VALUE, resultBuilder);
        });
    }

    private int getOpenHighCardNeedExp(int openedTimes) {
        int[] ints = DrawCard.getById(GameConst.CONFIG_ID).getOpenhighcardneedexp();
        if (ints.length <= openedTimes) {
            return ints[ints.length - 1];
        } else {
            return ints[openedTimes];
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.DrawCard_AncientCall;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ResetHighCardPool_VALUE, SC_ResetHighCardPool.newBuilder().setRetCode(retCode));
    }
}
