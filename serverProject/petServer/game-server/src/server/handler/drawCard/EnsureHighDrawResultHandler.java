package server.handler.drawCard;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.drawCard.DrawCardUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.GamePlayLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.DrawCard.CS_EnsureHighDrawResult;
import protocol.DrawCard.SC_EnsureHighDrawResult;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_HighCard;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * 远古召唤确定选择奖励
 * @author huhan
 * @date 2020.11.14
 */
@MsgId(msgId = MsgIdEnum.CS_EnsureHighDrawResult_VALUE)
public class EnsureHighDrawResultHandler extends AbstractBaseHandler<CS_EnsureHighDrawResult> {
    @Override
    protected CS_EnsureHighDrawResult parse(byte[] bytes) throws Exception {
        return CS_EnsureHighDrawResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_EnsureHighDrawResult req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        playerEntity player = playerCache.getByIdx(playerIdx);
        SC_EnsureHighDrawResult.Builder resultBuilder = SC_EnsureHighDrawResult.newBuilder();
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_EnsureHighDrawResult_VALUE, resultBuilder);
            return;
        }

        int newDrawTimes = 0;
        if (req.getEnsure()) {
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DrawCard_High);
            DB_HighCard curHighDrawRewards = player.getCurHighDraw();
            if (curHighDrawRewards == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
                gsChn.send(MsgIdEnum.SC_EnsureHighDrawResult_VALUE, resultBuilder);
                return;
            }

            newDrawTimes = SyncExecuteFunction.executeFunction(player, entity -> {
                player.clearHighPoolData();
                player.sendDrawCardInfo();

                return entity.getDb_data().getDrawCard().getCurHighDrawTimes();
            });

            RewardManager.getInstance().doReward(playerIdx, curHighDrawRewards.getReward(), reason, false);

        } else {
            newDrawTimes = SyncExecuteFunction.executeFunction(player, entity -> {
                player.getDb_data().getDrawCardBuilder().clearCurHighDrawIndex();
                return entity.getDb_data().getDrawCard().getCurHighDrawTimes();
            });
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setDrawCount(newDrawTimes);
        gsChn.send(MsgIdEnum.SC_EnsureHighDrawResult_VALUE, resultBuilder);

        //玩法统计日志
        LogService.getInstance().submit(new GamePlayLog(playerIdx, EnumFunction.DrawCard_AncientCall));
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
        gsChn.send(MsgIdEnum.SC_EnsureHighDrawResult_VALUE, SC_EnsureHighDrawResult.newBuilder().setRetCode(retCode));
    }
}
