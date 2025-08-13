package server.handler.drawCard;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.drawCard.DrawCardManager;
import model.drawCard.DrawCardUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.PetCallLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.DrawCard.CS_DrawHighCard;
import protocol.DrawCard.SC_DrawHighCard;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_DrawCardData;
import protocol.PlayerDB.DB_HighCard;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 远古召唤
 */
@MsgId(msgId = MsgIdEnum.CS_DrawHighCard_VALUE)
public class DrawHighCardHandler extends AbstractBaseHandler<CS_DrawHighCard> {
    @Override
    protected CS_DrawHighCard parse(byte[] bytes) throws Exception {
        return CS_DrawHighCard.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DrawHighCard req, int i) {
        SC_DrawHighCard.Builder resultBuilder = SC_DrawHighCard.newBuilder();
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DrawHighCard_VALUE, resultBuilder);
            return;
        }

        if (!player.functionUnLock(EnumFunction.DrawCard)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_DrawHighCard_VALUE, resultBuilder);
            return;
        }

        //是否达到抽卡上限上限
        if (!DrawCardManager.getInstance().canDraw(playerIdx, 1)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_BanShu_OutOfLimit));
            gsChn.send(MsgIdEnum.SC_DrawHighCard_VALUE, resultBuilder);
            return;
        }

        int noGainCount = SyncExecuteFunction.executeFunction(player, e -> player.getHighNoGainCount());
        if (noGainCount <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_DrawCard_HighDrawCountUpperLimit));
            gsChn.send(MsgIdEnum.SC_DrawHighCard_VALUE, resultBuilder);
            return;
        }

        int drawTimes = SyncExecuteFunction.executeFunction(player, e -> player.getDb_data().getDrawCard().getCurHighDrawTimes());
        Consume consume = consumeMaterial(playerIdx, drawTimes + 1);
        if (consume == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_DrawHighCard_VALUE, resultBuilder);
            return;
        }

        DB_HighCard result = SyncExecuteFunction.executeFunction(player, entity -> {
            DB_DrawCardData.Builder drawCardBuilder = entity.getDb_data().getDrawCardBuilder();
            //增加消耗的钻石
            if (consume.getRewardType() == RewardTypeEnum.RTE_Item) {
                drawCardBuilder.setDrawCardConsume(drawCardBuilder.getDrawCardConsume() + DrawCardUtil.getDrawCardConsumeByDrawCount(1));
            } else if (consume.getRewardType() == RewardTypeEnum.RTE_Diamond) {
                drawCardBuilder.setDrawCardConsume(drawCardBuilder.getDrawCardConsume() + consume.getCount());
            }
            entity.increaseHighDrawTimes();
            return player.drawHighCard();
        });

        if (result == null || result.getReward() == null) {
//                || !RewardManager.getInstance().doReward(playerIdx, result.getReward(),
//                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_High), false)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DrawHighCard_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setGainIndex(result.getIndex());
        gsChn.send(MsgIdEnum.SC_DrawHighCard_VALUE, resultBuilder);

        //设置计数
        DrawCardManager.getInstance().addDrawCount(playerIdx, 1);

//        //检查本轮是否已经抽取完毕
//        SyncExecuteFunction.executeConsumer(player, p -> {
//            if (player.getHighNoGainCount() <= 0) {
//                player.getDb_data().getDrawCardBuilder().clearHighCards();
//                player.sendDrawCardInfo();
//            }
//        });

//        LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.DrawCard_AncientCall));
        LogService.getInstance().submit(new PetCallLog(playerIdx, result.getReward(), consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_High)));

    }

    /**
     * 消耗材料,先消耗道具在消耗钻石
     *
     * @param times
     * @return 消耗的材料
     */
    private Consume consumeMaterial(String playerIdx, int times) {
//        Consume item = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Item_VALUE,
//                DrawCard.getById(GameConst.CONFIG_ID).getHighbookcfgid(), 1);
//        if (ConsumeManager.getInstance().consumeMaterial(playerIdx, item,
//                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_High))) {
//            return item;
//        }

        int diamondCount = DrawCardUtil.getDrawCardConsumeByDrawCount(times);
        Consume diamond = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Diamond_VALUE, 0, diamondCount);
        if (ConsumeManager.getInstance().consumeMaterial(playerIdx, diamond,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_High))) {
            return diamond;
        }
        return null;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.DrawCard_AncientCall;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_DrawHighCard_VALUE, SC_DrawHighCard.newBuilder().setRetCode(retCode));
    }
}
