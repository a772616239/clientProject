/**
 * created by tool DAOGenerate
 */
package model.itembag.entity;

import cfg.GameConfig;
import cfg.Item;
import cfg.ItemObject;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import java.util.LinkedList;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.consume.ConsumeManager;
import model.itembag.BlindBoxManager;
import model.itembag.ItemConst.ItemType;
import model.itembag.ItemUtil;
import model.itembag.dbCache.itembagCache;
import model.mainLine.dbCache.mainlineCache;
import model.mainLine.entity.mainlineEntity;
import model.mistforest.MistForestManager;
import model.obj.BaseObj;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.StatisticsLogUtil;
import platform.logs.entity.DailyDateLog;
import protocol.Bag;
import protocol.Bag.ItemInfo;
import protocol.Bag.SC_RefreashItem;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.RandomReward;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.ItemBagDB;
import protocol.ItemBagDB.DB_ItemBag;
import protocol.ItemBagDB.DB_ItemBag.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import server.handler.itemBag.UseItemHandler;
import util.ArrayUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static common.GameConst.CrossArenaScoreItemId;
import static protocol.Common.RewardSourceEnum.*;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class itembagEntity extends BaseObj {

    public static final int MUST_RANDOM_TOTAL_ODDS = 1000;


    public String getClassType() {
        return "itembagEntity";
    }

    /**
     *
     */
    private String idx;

    /**
     * 关联玩家id
     */
    private String linkplayeridx;

    /**
     *
     */
    private byte[] iteminfo;


    /**
     * 获得
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得关联玩家id
     */
    public String getLinkplayeridx() {
        return linkplayeridx;
    }

    /**
     * 设置关联玩家id
     */
    public void setLinkplayeridx(String linkplayeridx) {
        this.linkplayeridx = linkplayeridx;
    }

    /**
     * 获得
     */
    private byte[] getIteminfo() {
        return this.iteminfo;
    }

    /**
     * 设置
     */
    private void setIteminfo(byte[] itemInfo) {
        this.iteminfo = itemInfo;
    }


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return idx;
    }

    /**
     * 此方法不要用于创建新对象
     */
    private itembagEntity() {
    }


    /**
     * =====================================================================
     */

    @Override
    public void putToCache() {
        itembagCache.put(this);
    }

    private DB_ItemBag.Builder db_data;

    public DB_ItemBag.Builder getDb_data() {
        if (db_data == null) {
            db_data = getDBItem();
        }
        return db_data;
    }

    private DB_ItemBag.Builder getDBItem() {
        try {
            if (this.iteminfo != null) {
                return DB_ItemBag.parseFrom(iteminfo).toBuilder();
            } else {
                return DB_ItemBag.newBuilder();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    @Override
    public void transformDBData() {
        this.iteminfo = getDb_data().build().toByteArray();
    }

    public itembagEntity(String playerIdx) {
        this.idx = IdGenerator.getInstance().generateId();
        this.linkplayeridx = playerIdx;
    }

    public long getItemCount(int itemCfgId) {
        if (itemCfgId == 0) {
            return 0;
        }
        Map<Integer, Long> itemsMap = getDb_data().getItemsMap();
        if (itemsMap.containsKey(itemCfgId)) {
            return itemsMap.get(itemCfgId);
        }
        return 0;
    }

    /**
     * 道具是否足够
     *
     * @param cfgId
     * @param needCount
     * @return
     */
    public boolean itemIsEnough(int cfgId, long needCount) {
        return getItemCount(cfgId) >= needCount;
    }


    public boolean removeItem(int itemCfgId, long removeCount, Reason reason, boolean needRefresh) {
        if (itemCfgId <= 0 || removeCount <= 0) {
            LogUtil.warn("removeItem, error params, cfgId = " + itemCfgId + ", removeCount = " + removeCount);
            return false;
        }

        DB_ItemBag.Builder itemInfo = getDb_data();
        long ownedCount = getItemCount(itemCfgId);
        if (ownedCount < removeCount) {
            LogUtil.warn("itembagEntity.removeItem, playerIdx:" + getLinkplayeridx() + "removeCount > ownedCount , remove failed");
            return false;
        } else if (ownedCount == removeCount) {
            itemInfo.removeItems(itemCfgId);
        } else {
            itemInfo.putItems(itemCfgId, ownedCount - removeCount);
        }
        itemInfo.setUsedCapacity(itemInfo.getUsedCapacity() - removeCount);

        long remain = getItemCount(itemCfgId);
        if (needRefresh) {
            sendRefreshItemMsg(itemCfgId, remain);
        }

        LogService.getInstance().submit(new DailyDateLog(getLinkplayeridx(), true, RewardTypeEnum.RTE_Item,
                itemCfgId, ownedCount, removeCount, remain, reason));
        return true;
    }

    public boolean removeItemByMap(Map<Integer, Integer> removeMap, Reason reason) {
        if (removeMap == null || removeMap.isEmpty()) {
            return true;
        }

        //检查道具是否足够
        for (Entry<Integer, Integer> entry : removeMap.entrySet()) {
            if (getItemCount(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        for (Entry<Integer, Integer> entry : removeMap.entrySet()) {
            if (!removeItem(entry.getKey(), entry.getValue(), reason, false)) {
                LogUtil.error("removeItemFailed, unknown error");
                return false;
            }
        }

        sendRefreshItemMsgBySet(removeMap.keySet());
        return true;
    }

    /**
     * 添加Item，会过滤掉未在Item表的Id
     *
     * @param itemCfgId
     * @param addCount
     * @return 增加后的数量
     */
    public void addItem(int itemCfgId, long addCount, Reason reason, boolean needRefresh) {
        ItemObject itemCfg = Item.getById(itemCfgId);
        if (itemCfgId <= 0 || addCount <= 0 || itemCfg == null || itemCfg.getSpecialtype() == ItemType.ONLY_USE_FOR_DISPLAY) {
            LogUtil.info("addItem, error params, itemCfgId = " + itemCfgId + ", addCount = " + addCount
                    + ", special type:" + (itemCfg == null ? "" : itemCfg.getSpecialtype()));
            return;
        }

        DB_ItemBag.Builder itemInfo = getDb_data();
        Map<Integer, Long> itemsMap = itemInfo.getItemsMap();
        long ownedCount = 0;
        if (itemsMap.containsKey(itemCfgId)) {
            ownedCount = itemsMap.get(itemCfgId);
        }

        long newCount = GameUtil.sumLong(ownedCount, addCount);

        //拥有限制, -1无限制
        int ownedLimit = itemCfg.getMaxownedcount();
        if (ownedLimit != -1 && newCount > ownedLimit) {
            newCount = ownedLimit;
        }

        //数量未变化
        if (newCount == ownedCount) {
            return;
        }

        itemInfo.putItems(itemCfgId, newCount);
        itemInfo.setUsedCapacity(GameUtil.sumLong(itemInfo.getUsedCapacity(), addCount));

        if (itemCfg.getUsable() && itemCfg.getAutouse()) {
            useItemLong(itemCfgId, newCount, null);
        }

        if (needRefresh) {
            sendRefreshItemMsg(itemCfgId, getItemCount(itemCfgId));
        }

        LogService.getInstance().submit(new DailyDateLog(getLinkplayeridx(), false, RewardTypeEnum.RTE_Item,
                itemCfgId, ownedCount, addCount, newCount, reason));

        //任务累积获得功勋
        if (itemCfgId == GameConst.ITEM_ID_FEATS) {
            EventUtil.triggerUpdateTargetProgress(getLinkplayeridx(), TargetTypeEnum.TEE_Feats_CumuGain,
                    (int) Math.min(Integer.MAX_VALUE, addCount), 0);
        }
        //累计收集道具
        EventUtil.triggerUpdateTargetProgress(getLinkplayeridx(), TargetTypeEnum.TEE_Item_CumuCollect,
                (int) Math.min(Integer.MAX_VALUE, addCount), itemCfgId);

        if (itemCfgId == GameConst.ITEM_ID_MazeFragment && MistForestManager.getInstance().getMazeManager().isOpen()) {
            EventUtil.triggerCollectMazeItemCount(getLinkplayeridx(), (int) addCount);
        }
    }

    public void useItemLong(int itemCfgId, long useCount, List<Integer> params) {
        if (useCount <= 0) {
            return;
        }
        long remainNeedUseCount = useCount;
        while (remainNeedUseCount > 0) {
            int newOpenCount = (int) Math.min(remainNeedUseCount, Integer.MAX_VALUE);
            CanUseItemResult canUseItemResult = UseItemHandler.calcCanUseCount(getLinkplayeridx(), itemCfgId, newOpenCount);
            RetCodeEnum useResult = useItem(itemCfgId, canUseItemResult.getCanUseCount(), params, canUseItemResult.getOpenMaterial());
            if (useResult == RetCodeEnum.RCE_Success) {
                remainNeedUseCount -= canUseItemResult.getCanUseCount();
            } else {
                break;
            }
        }
    }


    /**
     * 玩家玩 悬赏任务、主擂台对战、十连胜、组队玩法的上限手
     */
    private static final List<Common.RewardSourceEnum> checkSource = Arrays.asList(RSE_OFFER_REWARD,RSE_OFFER_REWARD_FIGHT,RSE_OFFER_REWARD_OVER,RSE_OFFER_REWARD_BATTLE
            , RSE_LT_CP,RSE_CrossArena10Win,RSE_CrossArenaDailyMission,RSE_CrossArenaWinTask);

    /**
     * 是否每周限制获取数量道具
     * @param reason
     * @param itemId
     * @return
     */
    public static boolean crossArenaLimitScoreItem( int itemId,ReasonManager.Reason reason) {
        return itemId == CrossArenaScoreItemId && reason != null && checkSource.contains(reason.getSourceEnum());
    }

    /**
     * 添加Item，会过滤掉未在Item表的Id
     *
     * @return 增加后的数量 <itemCfgId,remain>
     */
    public void addItem(Map<Integer, Integer> addMap, Reason reason) {
        if (addMap == null || addMap.isEmpty()) {
            return;
        }

        for (Entry<Integer, Integer> entry : addMap.entrySet()) {
            addItem(entry.getKey(), entry.getValue(), reason, false);
            if (weeklyLimitItem(entry.getKey(), reason)) {
                addWeeklyLimitItemGainCount(entry.getKey(), entry.getValue());
            }
        }
        sendRefreshItemMsgBySet(addMap.keySet());
    }

    private static boolean weeklyLimitItem(int itemId, Reason reason) {
        return crossArenaLimitScoreItem(itemId, reason);
    }


    /**
     * 传入ItemCfgId
     *
     * @param refreshList
     */
    public void sendRefreshItemMsgBySet(Set<Integer> refreshList) {
        if (refreshList == null || refreshList.isEmpty()) {
            return;
        }

        SC_RefreashItem.Builder builder = SC_RefreashItem.newBuilder();
        for (Integer itemCfgId : refreshList) {
            ItemInfo itemInfo = builderItemInfo(itemCfgId);
            if (itemInfo != null) {
                builder.addItemInfo(itemInfo);
            }
        }
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreashItem_VALUE, builder);
    }

    /**
     * 传入<ItemCfgId,remain>
     *
     * @param refreshMap
     */
    private void sendRefreshItemMsgByMap(Map<Integer, Integer> refreshMap) {
        if (refreshMap == null || refreshMap.isEmpty()) {
            return;
        }

        SC_RefreashItem.Builder builder = SC_RefreashItem.newBuilder();
        for (Entry<Integer, Integer> entry : refreshMap.entrySet()) {
            ItemInfo.Builder itemInfo = ItemInfo.newBuilder();
            itemInfo.setItemCfgId(entry.getKey());
            itemInfo.setNewItemCount(entry.getValue());
            builder.addItemInfo(itemInfo);
        }
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreashItem_VALUE, builder);
    }

    /**
     * 传入ItemCfgId,remain
     */
    private void sendRefreshItemMsg(int cfgId, long remain) {
        SC_RefreashItem.Builder builder = SC_RefreashItem.newBuilder();
        ItemInfo.Builder itemInfo = ItemInfo.newBuilder();
        itemInfo.setItemCfgId(cfgId);
        itemInfo.setNewItemCount(remain);
        builder.addItemInfo(itemInfo);

        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_RefreashItem_VALUE, builder);
    }

    private ItemInfo builderItemInfo(int itemCfgId) {
        if (itemCfgId <= 0) {
            return null;
        }

        ItemInfo.Builder itemInfo = ItemInfo.newBuilder();
        itemInfo.setItemCfgId(itemCfgId);
        itemInfo.setNewItemCount(getItemCount(itemCfgId));

        return itemInfo.build();
    }

    private RetCodeEnum useBeforeCheck(ItemObject itemCfg, int useCount, List<Consume> material) {
        if (itemCfg == null || useCount <= 0) {
            LogUtil.info("params error, itemCfg = " + itemCfg + ", itemCount" + useCount);
            return RetCodeEnum.RCE_ErrorParam;
        }

        //道具不能使用
        if (!itemCfg.getUsable()) {
            return RetCodeEnum.RCE_Itembag_ItemCanNotUsed;
        }

        //是否满足等级要求
        int playerLv = PlayerUtil.queryPlayerLv(getLinkplayeridx());
        if (itemCfg.getUseneedlv() > playerLv) {
            return RetCodeEnum.RCE_LvNotEnough;
        }

        RetCodeEnum codeEnum = checkPurItemUseable(getLinkplayeridx(), itemCfg);
        if (codeEnum != RetCodeEnum.RCE_Success) {
            return codeEnum;
        }

        //消耗材料是否成功
        if (ArrayUtil.checkArraySize(itemCfg.getUsecostitem(), 1, 1)) {
            if (CollectionUtils.isEmpty(material)) {
                return RetCodeEnum.RCE_MatieralNotEnough;
            }

            List<Consume> itemConsume = new ArrayList<>();
            List<Consume> otherConsume = new ArrayList<>();
            for (Consume consume : material) {
                if (consume.getRewardType() == RewardTypeEnum.RTE_Item) {
                    itemConsume.add(consume);
                } else {
                    otherConsume.add(consume);
                }
            }

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Use);
            if (!ConsumeManager.getInstance().consumeMaterialByList(getLinkplayeridx(), itemConsume, reason)
                    || !ConsumeManager.getInstance().asyncConsumeMaterialByList(getLinkplayeridx(), otherConsume, reason)) {
                return RetCodeEnum.RCE_MatieralNotEnough;
            }
        }

        return RetCodeEnum.RCE_Success;
    }

    private RetCodeEnum checkPurItemUseable(String playerIdx, ItemObject itemCfg) {
        if (ItemType.Purchase != itemCfg.getSpecialtype()) {
            return RetCodeEnum.RCE_Success;
        }
        int[][] reward = itemCfg.getMustreward();
        if (ArrayUtils.isEmpty(reward)) {
            return RetCodeEnum.RCE_Success;
        }
        for (int[] ints : reward) {
            if (protocol.Common.RewardTypeEnum.RTE_MonthCard_VALUE == ints[0]) {
                playerEntity player = playerCache.getByIdx(playerIdx);
                if (player == null || player.activeMonthCard(ints[1])) {
                    return RetCodeEnum.RCE_MonthCard_AlreadyActive;
                }
            }
            if (protocol.Common.RewardTypeEnum.RTE_AdvancedFeats_VALUE == ints[0]) {
                targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
                if (target == null || target.activeAdvanceFeats(GameConst.FEAT_TYPE_HUOYUE)) {
                    return RetCodeEnum.RCE_Feats_AlreadyActive;
                }
            }
            if (protocol.Common.RewardTypeEnum.RTE_AdvancedFeats_WUJIN_VALUE == ints[0]) {
            	targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
            	if (target == null || target.activeAdvanceFeats(GameConst.FEAT_TYPE_WUJIN)) {
            		return RetCodeEnum.RCE_Feats_AlreadyActive;
            	}
            }
            if (protocol.Common.RewardTypeEnum.RTE_AdvancedFeats_XUKONG_VALUE == ints[0]) {
            	targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
            	if (target == null || target.activeAdvanceFeats(GameConst.FEAT_TYPE_XUKONG)) {
            		return RetCodeEnum.RCE_Feats_AlreadyActive;
            	}
            }
        }
        return RetCodeEnum.RCE_Success;
    }

    /**
     * @param itemCfgId
     * @param itemCount
     * @param params
     * @param material  开启宝箱需要消耗的材料
     * @return
     */
    public RetCodeEnum useItem(int itemCfgId, int itemCount, List<Integer> params, List<Consume> material) {
        ItemObject itemCfg = Item.getById(itemCfgId);
        RetCodeEnum checkRet = useBeforeCheck(itemCfg, itemCount, material);
        if (checkRet != RetCodeEnum.RCE_Success) {
            return checkRet;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Use,
                StatisticsLogUtil.getNameByTypeAndId(RewardTypeEnum.RTE_Item, itemCfgId), itemCount);

        //功能性奖励
        ItemFunctionResult functionResult = doItemFunctionRewards(itemCfg, itemCount, params);
        if (functionResult.getRet() != RetCodeEnum.RCE_Success) {
            return functionResult.getRet();
        }

        //必得奖励
        List<Reward> mustRewards
                = RewardUtil.multiReward(RewardUtil.parseRewardIntArrayToRewardList(itemCfg.getMustreward()), itemCount);

        //随机奖励2
        List<Reward> random2Rewards =
                RewardUtil.drawMustRandomReward(itemCfg.getRandomrewards(), MUST_RANDOM_TOTAL_ODDS, itemCount * itemCfg.getRandomtimes2());

        List<Reward> totalRewards = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(functionResult.getRewards())) {
            totalRewards.addAll(functionResult.getRewards());
        }

        if (CollectionUtils.isNotEmpty(mustRewards)) {
            totalRewards.addAll(mustRewards);
        }

        if (CollectionUtils.isNotEmpty(random2Rewards)) {
            totalRewards.addAll(random2Rewards);
        }

        if (CollectionUtils.isEmpty(totalRewards)) {
            LogUtil.error("model.itembag.entity.itembagEntity.useItem, use item failed, reward is empty, cfg id :" + itemCfgId);
            return RetCodeEnum.RCE_UnknownError;
        }

        //移除使用的道具
        if (removeItem(itemCfgId, itemCount, reason, true)) {
            doRewardAndShow(itemCfg, reason, functionResult, totalRewards);
            recordItemUseCount(itemCfgId, itemCount);
            return RetCodeEnum.RCE_Success;
        } else {
            return RetCodeEnum.RCE_Itembag_ItemNotEnought;
        }
    }

    private boolean triggerPurchaseItem(ItemObject itemCfg) {
        for (int[] ints : itemCfg.getMustreward()) {
            if (ints[0] == RewardTypeEnum.RTE_RechargeProduct_VALUE) {
                return true;
            }
        }
        return false;
    }

    private void doRewardAndShow(ItemObject itemCfg, Reason reason, ItemFunctionResult functionResult, List<Reward> totalRewards) {
        boolean show = isShow(itemCfg);
        RewardManager.getInstance().doRewardByList(getLinkplayeridx(), totalRewards, reason, show);

        if (itemCfg.getSpecialtype() == ItemType.Blind_Box) {
            sendUseBlindBoxInfo(itemCfg, functionResult);
        } else if (ItemUtil.mistBox(itemCfg)) {
            sendMistSpecialInfo(itemCfg, functionResult);
        }

    }

    private void sendMistSpecialInfo(ItemObject itemCfg, ItemFunctionResult functionResult) {

        List<Reward> rareReward = ItemUtil.parseMistMarqueeRareReward(functionResult.getRewards());

        if (CollectionUtils.isEmpty(rareReward)) {
            return;
        }

        List<Reward> msgRewardList = mistReward2MsgReward(rareReward);
        for (Reward reward : msgRewardList) {
            sendMistBoxOpenRareRewardMarquee(itemCfg, reward);
        }

    }

    private void sendMistBoxOpenRareRewardMarquee(ItemObject itemCfg, Reward reward) {
        List<Reward> msgReward = new ArrayList<>();
        Reward item = Reward.newBuilder().setRewardType(RewardTypeEnum.RTE_Item).setId(itemCfg.getId()).build();
        msgReward.add(item);
        msgReward.add(reward);
        GlobalData.getInstance().sendSpecialMarqueeToAllOnlinePlayer(GameConfig.getById(GameConst.CONFIG_ID).getMistboxrarerewardmarqueeid()
                , msgReward, PlayerUtil.queryPlayerName(getLinkplayeridx()));
    }

    private List<Reward> mistReward2MsgReward(List<Reward> rareReward) {
        List<Reward> result = new LinkedList<>();
        for (Reward reward : rareReward) {
            Reward temp = reward.toBuilder().setCount(1).build();
            for (int i = 0; i < reward.getCount(); i++) {
                result.add(temp);
                if (result.size() == GameConfig.getById(GameConst.CONFIG_ID).getMistboxrewadmaxmarqueesize()) {
                    return result;
                }
            }

        }
        return result;
    }


    private void sendUseBlindBoxInfo(ItemObject itemCfg, ItemFunctionResult functionResult) {
        Bag.SC_ClaimBlindBoxReward.Builder blindMsg = Bag.SC_ClaimBlindBoxReward.newBuilder()
                .setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success)).setRewardIndex(functionResult.getRewardIndex()).addAllRewards(functionResult.getRewards());
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ClaimBlindBoxReward_VALUE, blindMsg);
        resetAndPushBlindBoxInfo(itemCfg.getId());
    }

    private boolean isShow(ItemObject itemCfg) {
        int[][] mustReward = itemCfg.getMustreward();
        for (int[] ints : mustReward) {
            if (ints[0] == RewardTypeEnum.RTE_RechargeProduct_VALUE) {
                return false;
            }
        }
        return ItemType.Blind_Box != itemCfg.getSpecialtype();
    }

    private void resetAndPushBlindBoxInfo(int itemCfgId) {
        Bag.SC_BlindBoxInfo.Builder msg = Bag.SC_BlindBoxInfo.newBuilder();
        Collection<Bag.BlindBoxReward> blindBoxRewards = BlindBoxManager.getInstance().randomBlindBoxShowRewards(itemCfgId);

        ItemBagDB.DB_BlindBoxReward.Builder builder = ItemBagDB.DB_BlindBoxReward.newBuilder();
        builder.addAllRewardList(blindBoxRewards);
        getDb_data().putBlindBoxes(itemCfgId, builder.build());

        msg.addAllRewardList(blindBoxRewards);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_BlindBoxInfo_VALUE, msg);
    }

    private void recordItemUseCount(int itemCfgId, int itemCount) {
        Integer oldTimes = getDb_data().getItemUseCountMap().get(itemCfgId);
        if (oldTimes == null) {
            oldTimes = 0;
        }
        getDb_data().putItemUseCount(itemCfgId, oldTimes + itemCount);

        //更新每日限制使用
        increaseDailyLimitUseItemCount(itemCfgId,itemCount);
    }

    private ItemFunctionResult doItemFunctionRewards(ItemObject itemCfg, int itemCount, List<Integer> params) {
        //权重一共1000,方便调整随机道具的出现概率
        if ("mustRandom".equalsIgnoreCase(itemCfg.getParamname())) {
            List<RandomReward> randomRewards = RewardUtil.parseIntArrayToRandomRewardList(itemCfg.getParamstr());
            List<Reward> mustRandomReward = RewardUtil.drawMustRandomReward(randomRewards
                    , MUST_RANDOM_TOTAL_ODDS, itemCount * itemCfg.getRandomtimes());
            return new ItemFunctionResult(RetCodeEnum.RCE_Success, mustRandomReward);

        } else if ("onHookGold".equalsIgnoreCase(itemCfg.getParamname())) {
            return doOnHookFunction(itemCfg, itemCount,
                    e -> e.getRewardType() == RewardTypeEnum.RTE_Gold);

        } else if ("onHookLifeStone".equalsIgnoreCase(itemCfg.getParamname())) {
            return doOnHookFunction(itemCfg, itemCount,
                    e -> e.getRewardType() == RewardTypeEnum.RTE_Item && e.getId() == GameConst.ITEM_ID_LIFE_STONE);

        } else if ("lvReward".equalsIgnoreCase(itemCfg.getParamname())) {
            return new ItemFunctionResult(RetCodeEnum.RCE_Success, doLvRewardFunction(itemCfg, itemCount));

        } else if ("chooseReward".equalsIgnoreCase(itemCfg.getParamname())) {
            return new ItemFunctionResult(RetCodeEnum.RCE_Success, doChooseReward(itemCfg, itemCount, params));

        } else if ("blindBox".equalsIgnoreCase(itemCfg.getParamname())) {
            Pair<Integer, List<Reward>> result = doBlindBoxReward(itemCfg, itemCount);
            if (result != null) {
                return new ItemFunctionResult(RetCodeEnum.RCE_Success, result.getValue(), result.getKey());
            }
            return new ItemFunctionResult(RetCodeEnum.RCE_UnknownError);
        }

        return new ItemFunctionResult(RetCodeEnum.RCE_Success);
    }

    private Pair<Integer, List<Reward>> doBlindBoxReward(ItemObject itemCfg, int itemCount) {
        Pair<Integer, List<Reward>> result = randomBlindRewardFromShowReward(itemCfg);
        if (itemCount <= 1) {
            return result;
        }
        List<Reward> batchRandom = BlindBoxManager.getInstance().batchRandomReward(itemCfg.getId(), itemCount - 1);
        ArrayList<Reward> allRewards = new ArrayList<>(batchRandom);
        int showRewardIndex = 0;
        if (result != null) {
            allRewards.addAll(result.getValue());
            showRewardIndex = result.getKey();
        }
        return new Pair<>(showRewardIndex, allRewards);
    }

    private Pair<Integer, List<Reward>> randomBlindRewardFromShowReward(ItemObject itemCfg) {
        String playerIdx = getLinkplayeridx();
        int itemCfgId = itemCfg.getId();
        ItemBagDB.DB_BlindBoxReward db_blindBoxReward = getDb_data().getBlindBoxesMap().get(itemCfgId);
        if (db_blindBoxReward == null) {
            LogUtil.error("getBlindBoxRewardByItemCfg error playerIdx:{},reward not init ", playerIdx);
            return null;
        }
        List<Bag.BlindBoxReward> rewardListList = db_blindBoxReward.getRewardListList();
        if (CollectionUtils.isEmpty(rewardListList)) {
            LogUtil.error("getBlindBoxRewardByItemCfg error playerIdx:{},rewardListList is empty ", playerIdx);
            return null;
        }
        return BlindBoxManager.getInstance().randomRewardFromShowReward(rewardListList);
    }


    private List<Reward> doChooseReward(ItemObject itemCfg, int itemCount, List<Integer> params) {
        if (CollectionUtils.isEmpty(params)) {
            return null;
        }

        int chooseIndex = params.get(0);
        if (chooseIndex >= itemCfg.getParamstr().length) {
            return null;
        }

        return Collections.singletonList(RewardUtil.parseAndMulti(itemCfg.getParamstr()[chooseIndex], itemCount));
    }

    /**
     * 玩家随机奖励
     *
     * @param itemCfg
     * @param useCount
     * @return
     */
    private List<Reward> doLvRewardFunction(ItemObject itemCfg, int useCount) {
        if (itemCfg == null || useCount <= 0) {
            return null;
        }

        int playerLv = PlayerUtil.queryPlayerLv(getLinkplayeridx());

        return Stream.of(itemCfg.getParamstr())
                .filter(e -> {
                    if (e.length < 3) {
                        LogUtil.error("model.itembag.entity.itembagEntity.doLvRewardFunction, function param length is less than 3");
                        return false;
                    }
                    return GameUtil.inScope(e[0], e[1], playerLv);
                })
                .map(e -> RewardUtil.multiReward(RewardUtil.getRewardsByRewardId(e[2]), useCount))
                .reduce((e1, e2) -> {
                    if (e2 != null) {
                        e1.addAll(e2);
                    }
                    return e1;
                }).orElse(Collections.emptyList());
    }

    /**
     * 快速挂机相关功能
     *
     * @param itemCfg
     * @param itemCount
     * @param predicate
     * @return
     */
    private ItemFunctionResult doOnHookFunction(ItemObject itemCfg, int itemCount, Predicate<Reward> predicate) {
        if (itemCfg == null || itemCount <= 0 || predicate == null) {
            return new ItemFunctionResult(RetCodeEnum.RCE_ErrorParam);
        }

        mainlineEntity entity = mainlineCache.getInstance().getMainLineEntityByPlayerIdx(getLinkplayeridx());
        if (entity == null || !entity.canQuickOnHook()) {
            return new ItemFunctionResult(RetCodeEnum.RCE_MainLine_PlayerHaveNoOnhook);
        }

        if (!ArrayUtil.checkArraySize(itemCfg.getParamstr(), 1, 1)) {
            LogUtil.error("itembagEntity.useItem, item cfg is error, itemCfgId:" + itemCfg.getId() + "paramName:"
                    + itemCfg.getParamname() + ", paramStr:" + Arrays.toString(itemCfg.getParamstr()));
            return new ItemFunctionResult(RetCodeEnum.RCE_ErrorParam);
        }

        long onHookValidTime = TimeUtil.MS_IN_A_HOUR * itemCfg.getParamstr()[0][0];

        List<Reward> reward = RewardUtil.filterRewards(entity.calculateOnHookReward(onHookValidTime), predicate);
        reward = RewardUtil.multiReward(reward, itemCount);
        return new ItemFunctionResult(RetCodeEnum.RCE_Success, reward);
    }

    public void clearAllMistItem() {
        if (getDb_data() == null || getDb_data().getItemsMap() == null) {
            return;
        }

        List<ItemObject> needClearList = new ArrayList<>();
        List<ItemObject> bossBoxItem = Item.getAllItemBySpecialType(ItemType.Mist_Boss_Box);
        if (CollectionUtils.isNotEmpty(bossBoxItem)) {
            needClearList.addAll(bossBoxItem);
        }
        List<ItemObject> teamBox = Item.getAllItemBySpecialType(ItemType.Mist_Team_Box);
        if (CollectionUtils.isNotEmpty(teamBox)) {
            needClearList.addAll(teamBox);
        }

        if (CollectionUtils.isNotEmpty(needClearList)) {
            clearItem(needClearList.stream().map(ItemObject::getId).collect(Collectors.toSet()),
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Expire));
        }

        LogUtil.info("playerIdx[" + getLinkplayeridx() + "] ClearMistItem finish");
    }

    public void sendDailyLimitItemUse() {
        Bag.SC_ItemDailyLimitUseCount.Builder msg = Bag.SC_ItemDailyLimitUseCount.newBuilder();
        Map<Integer, Integer> countMap = getDb_data().getItemDailyUseCountMap();
        msg.addAllItemCfgId(countMap.keySet());
        msg.addAllUseCount(countMap.values());
        GlobalData.getInstance().sendMsg(getLinkplayeridx(), MsgIdEnum.SC_ItemDailyLimitUseCount_VALUE, msg);

    }


    /**
     * 删除指定itemCfgId的道具
     */
    public void clearItem(Set<Integer> clearSet, Reason reason) {
        if (clearSet == null || clearSet.isEmpty()) {
            return;
        }
        Builder db_data = getDb_data();
        if (db_data == null) {
            return;
        }

        Set<Integer> needRefresh = new HashSet<>();
        for (Integer cfgId : clearSet) {
            long itemCount = getItemCount(cfgId);
            db_data.removeItems(cfgId);
            if (itemCount > 0) {
                needRefresh.add(cfgId);

                LogService.getInstance().submit(new DailyDateLog(getLinkplayeridx(), true, RewardTypeEnum.RTE_Item,
                        cfgId, itemCount, itemCount, 0, reason));
            }
        }

        sendRefreshItemMsgBySet(needRefresh);
    }

    public void increaseDailyLimitUseItemCount(int itemCfgId, int itemCount) {
        ItemObject item = Item.getById(itemCfgId);
        if (item == null || item.getDailyusetimeslimit() == -1) {
            return;
        }
        itemCount = itemCount <= 0 ? 1 : itemCount;
        Integer useCount = getDb_data().getItemDailyUseCountMap().get(itemCfgId);

        int nowUseCount = useCount == null ? itemCount : useCount + itemCount;

        getDb_data().putItemDailyUseCount(itemCfgId, nowUseCount);

        sendDailyLimitItemUse();
    }

    public int queryDailyRemainUseTime(ItemObject itemCfg) {
        if (itemCfg == null) {
            return 0;
        }
        int dailyUseTimesLimit = itemCfg.getDailyusetimeslimit();
        if (dailyUseTimesLimit == -1) {
            return Integer.MAX_VALUE;
        }
        Integer todayUse = getDb_data().getItemDailyUseCountMap().get(itemCfg.getId());
        if (todayUse == null) {
            return dailyUseTimesLimit;
        }
        return Math.max(dailyUseTimesLimit - todayUse, 0);
    }

    public void updateDailyData(boolean sendMsg) {
        getDb_data().clearItemDailyUseCount();
        if (sendMsg) {
            sendDailyLimitItemUse();
        }
    }

    public void updateWeeklyData() {
        getDb_data().clearWeeklyLimitAlreadyGain();
    }

    public int getWeeklyLimitItemGainCount(int itemId) {
        return getDb_data().getWeeklyLimitAlreadyGainMap().getOrDefault(itemId, 0);
    }

    public void addWeeklyLimitItemGainCount(int itemId, int add) {
        getDb_data().putWeeklyLimitAlreadyGain(itemId, getWeeklyLimitItemGainCount(itemId) + add);
    }
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class ItemFunctionResult {
    private RetCodeEnum ret;
    private List<Reward> rewards;
    private int rewardIndex;

    public ItemFunctionResult(RetCodeEnum ret, List<Reward> rewards) {
        this.rewards = rewards;
        this.ret = ret;
    }

    public ItemFunctionResult(RetCodeEnum ret) {
        this.ret = ret;
    }
}