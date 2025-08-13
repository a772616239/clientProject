package server.handler.itemBag;

import cfg.Item;
import cfg.ItemObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import platform.logs.ReasonManager;
import protocol.Bag.CS_SellItem;
import protocol.Bag.SC_SellItem;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_SellItem_VALUE)
public class SellItemHandler extends AbstractBaseHandler<CS_SellItem> {
    @Override
    protected CS_SellItem parse(byte[] bytes) throws Exception {
        return CS_SellItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_SellItem req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        int itemCfgId = req.getItemCfgId();
        int sellCount = req.getSellCount();

        SC_SellItem.Builder resultBuilder = SC_SellItem.newBuilder();
        ItemObject itemCfg = Item.getById(itemCfgId);
        if (itemCfg == null || sellCount <= 0) {
            LogUtil.error("SellItemHandler, itemCfg is null, itemCfgId[" + itemCfgId + "]");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_SellItem_VALUE, resultBuilder);
            return;
        }

        if (!itemCfg.getSalable()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ItemBag_ItemCanNotSell));
            gsChn.send(MsgIdEnum.SC_SellItem_VALUE, resultBuilder);
            return;
        }

        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
        if (itemBag == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] itemBag is null");
            return;
        }

        SyncExecuteFunction.executeConsumer(itemBag, entity -> {
            if (!itemBag.removeItem(itemCfgId, sellCount, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Sell), true)) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Itembag_ItemNotEnought));
                gsChn.send(MsgIdEnum.SC_SellItem_VALUE, resultBuilder);
                return;
            }

            List<Reward> rewards = RewardUtil.multiReward(RewardUtil.parseRewardIntArrayToRewardList(itemCfg.getGainaftersell()), sellCount);
            if (!RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Sell), true)) {
                LogUtil.info("doReward error, SellItemHandler," + rewards.toString());
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_SellItem_VALUE, resultBuilder);
                return;
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_SellItem_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ItemBag;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_SellItem_VALUE, SC_SellItem.newBuilder().setRetCode(retCode));
    }
}
