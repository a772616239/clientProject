package server.handler.itemBag;

import cfg.Item;
import cfg.ItemObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.activity.ActivityManager;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.itembag.ItemConst.ItemType;
import model.itembag.ItemUtil;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.CanUseItemResult;
import model.itembag.entity.itembagEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.Bag.CS_UseItem;
import protocol.Bag.SC_UseItem;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCode.Builder;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.DropResourceEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.ArrayUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_UseItem_VALUE)
public class UseItemHandler extends AbstractBaseHandler<CS_UseItem> {
    @Override
    protected CS_UseItem parse(byte[] bytes) throws Exception {
        return CS_UseItem.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_UseItem req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        playerEntity player = playerCache.getByIdx(playerIdx);
        itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);

        int itemCfgId = req.getItemCfgId();

        SC_UseItem.Builder resultBuilder = SC_UseItem.newBuilder();
        if (itemBag == null || player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_UseItem_VALUE, resultBuilder);
            return;
        }

        ItemObject itemCfg = Item.getById(itemCfgId);
        if (itemCfg == null || req.getUseCount() <= 0
                || itemBag.getItemCount(itemCfgId) < req.getUseCount()) {
            LogUtil.error("CS_UseItem, itemId[" + itemCfgId + "] cfg is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_UseItem_VALUE, resultBuilder);
            return;
        }

        if (!itemCfg.getUsable()) {
            Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RCE_Itembag_ItemCanNotUsed);
            resultBuilder.setRetCode(retCode);
            gsChn.send(MsgIdEnum.SC_UseItem_VALUE, resultBuilder);
            return;
        }

        CanUseItemResult canUseResult = calcCanUseCount(player, itemBag, itemCfg, req.getUseCount());
        if (canUseResult.getCanUseCount() <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ItemBag_CanNotUseMore));
            gsChn.send(MsgIdEnum.SC_UseItem_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum retCode = SyncExecuteFunction.executeFunction(itemBag, t ->
                itemBag.useItem(itemCfgId, canUseResult.getCanUseCount(), req.getParamsList(), canUseResult.getOpenMaterial()));

        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_UseItem_VALUE, resultBuilder);

        LogUtil.debug("Player [" + playerIdx + "] use item[" + itemCfgId + "] count=" + canUseResult.getCanUseCount()
                + ",itemSpeType=" + itemCfg.getSpecialtype() + ",retCode=" + retCode);
        if (retCode == RetCodeEnum.RCE_Success && ItemUtil.mistBox(itemCfg)) {
            useMistBoxItem(player, itemCfg.getSpecialtype(), itemCfgId, canUseResult.getCanUseCount());
        }
    }


    public static CanUseItemResult calcCanUseCount(String playerIdx, int itemCfgId, int useCount) {
        return calcCanUseCount(playerCache.getByIdx(playerIdx), itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx),
                Item.getById(itemCfgId), useCount);
    }

    protected static CanUseItemResult calcCanUseCount(playerEntity player, itembagEntity itemBag, ItemObject itemCfg, int useCount) {
        if (player == null || itemBag == null || itemCfg == null) {
            return new CanUseItemResult(0);
        }

        //可使用道具开启数量限制
        int remainOpenTimes = SyncExecuteFunction.executeFunction(itemBag, bag -> {
            int dailyLimitRemainUse = itemBag.queryDailyRemainUseTime(itemCfg);

            int totalCanUseCount = itemCfg.getUsetimeslimit();
            if (totalCanUseCount == -1) {
                return Math.min(useCount, dailyLimitRemainUse);
            }

            Integer alreadyUseCount = itemBag.getDb_data().getItemUseCountMap().get(itemCfg.getId());
            if (alreadyUseCount == null) {
                alreadyUseCount = 0;
            }

            return Math.min(Math.min(useCount, dailyLimitRemainUse), Math.max(totalCanUseCount - alreadyUseCount, 0));
        });

        int realCanOpenCount = remainOpenTimes;

        if (itemCfg.getSpecialtype() == ItemType.Purchase) {
            realCanOpenCount = 1;
        }

        if (realCanOpenCount <= 0) {
            return new CanUseItemResult(realCanOpenCount);
        }

        //需要消耗消耗道具开启判断
        int[][] useCostItem = itemCfg.getUsecostitem();
        if (!ArrayUtil.checkArraySize(useCostItem, 1, 1)) {
            return new CanUseItemResult(realCanOpenCount);
        }

        List<Consume> material = new ArrayList<>();
        int remainNeedConsumeCount = realCanOpenCount;
        for (int[] ints : useCostItem) {
            Consume consume = ConsumeUtil.parseConsume(ints);
            if (consume == null) {
                LogUtil.error("UseItemHandler.calcCanUseCount, consume parse failed, item cfg id:" + itemCfg.getId());
                continue;
            }

            //新材料
            int newOpenCount = Math.min(remainNeedConsumeCount, ConsumeManager.getInstance().canConsumeCount(player.getIdx(), consume));
            if (newOpenCount > 0) {
                material.add(ConsumeUtil.multiConsume(consume, newOpenCount));
                remainNeedConsumeCount -= newOpenCount;
            }

            if (remainNeedConsumeCount <= 0) {
                break;
            }
        }
        return new CanUseItemResult(realCanOpenCount - remainNeedConsumeCount, material);
    }


    protected void useMistBoxItem(playerEntity player, int itemSpeType, int itemCfgId, int realUseCount) {
        DropResourceEnum dropResource = itemSpeType == ItemType.MIST_BOX ? DropResourceEnum.DRE_MistBox : DropResourceEnum.DRE_Null;
        List<Reward> rewards = ActivityManager.getInstance().calculateAllActivityDrop(player.getIdx(), dropResource, realUseCount);
        if (rewards != null) {
            RewardManager.getInstance().doRewardByList(player.getIdx(), rewards,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Use), true);
        }
        if (itemSpeType == ItemType.MIST_BOX) {
            //成就：累积开启x次x品质迷雾深林宝箱
            EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_CumuOpenMistBox, realUseCount, itemCfgId);

            //迷雾森林赛季任务：开启迷雾森林宝箱次数
            EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_MistSeasonTask_OpenBoxCount, realUseCount, 0);
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ItemBag;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_UseItem_VALUE, SC_UseItem.newBuilder().setRetCode(retCode));
    }
}
