package model.consume;

import common.GlobalThread;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import helper.StringUtils;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petfragment.dbCache.service.PetFragmentServiceImpl;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager.Reason;
import protocol.Common.Consume;
import protocol.Common.RewardTypeEnum;
import protocol.PetMessage.Pet;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class ConsumeManager {
    private static ConsumeManager instance = new ConsumeManager();

    public static ConsumeManager getInstance() {
        if (instance == null) {
            synchronized (ConsumeManager.class) {
                if (instance == null) {
                    instance = new ConsumeManager();
                }
            }
        }
        return instance;
    }

    private ConsumeManager() {
    }

    /**
     * 暂只支持金币,钻石,道具的扣除
     * 事务执行,注意不要加锁后调用
     *
     * @param playerIdx
     * @param consumeList
     * @param reason
     * @return
     */
    public boolean consumeMaterialByList(String playerIdx, List<Consume> consumeList, Reason reason) {
        if (CollectionUtils.isEmpty(consumeList)) {
            return true;
        }

        List<Consume> successConsume = new ArrayList<>();
        for (Consume consume : consumeList) {
            if (consumeMaterial(playerIdx, consume, reason)) {
                successConsume.add(consume);
            } else {
                if (!successConsume.isEmpty()) {
                    RewardManager.getInstance().doRewardByList(playerIdx, ConsumeUtil.parseConsumeToReward(successConsume), reason, false);
                }
                return false;
            }
        }

        return true;
    }

    /**
     * 消耗玩家道具，宠物和符文
     *
     * @param playerIdx
     * @param petIdxList
     * @param runeIdxList
     * @param reason
     * @return
     */
    public boolean consumeMaterial(String playerIdx, List<Consume> consumes, List<String> petIdxList, List<String> runeIdxList, Reason reason) {
        if (playerIdx == null) {
            return false;
        }

        petEntity petByPlayer = petCache.getInstance().getEntityByPlayer(playerIdx);
        petruneEntity runeByPlayer = petruneCache.getInstance().getEntityByPlayer(playerIdx);
        if (petByPlayer == null || runeByPlayer == null) {
            return false;
        }

        if (consumes != null && !consumes.isEmpty()) {
            if (!consumeMaterialByList(playerIdx, consumes, reason)) {
                return false;
            }
        }

        List<Pet> removePet = null;
        if (CollectionUtils.isNotEmpty(petIdxList)) {
            removePet = petCache.getInstance().removeByPetIdList(playerIdx, petIdxList, reason);
            if (CollectionUtils.isEmpty(removePet)) {
                if (CollectionUtils.isNotEmpty(consumes)) {
                    RewardManager.getInstance().doRewardByList(playerIdx, ConsumeUtil.parseConsumeToReward(consumes), reason, false);
                }
                return false;
            }
        }

        if (CollectionUtils.isNotEmpty(runeIdxList)) {
            boolean runeResult = petruneCache.getInstance().removeRune(runeByPlayer, runeIdxList, reason);
            if (!runeResult) {
                if (CollectionUtils.isNotEmpty(consumes)) {
                    RewardManager.getInstance().doRewardByList(playerIdx, ConsumeUtil.parseConsumeToReward(consumes), reason, false);
                }
                if (CollectionUtils.isNotEmpty(removePet)) {
                    petCache.getInstance().restorePet(playerIdx, removePet);
                }
                return false;
            }
        }

        return true;
    }

    /**
     * 暂只支持金币,钻石,道具,宠物碎片,圣水,迷雾森林体力的扣除
     * 注意不要加锁后调用
     *
     * @param playerIdx
     * @param consume
     * @param source
     * @return
     */
    public boolean consumeMaterial(String playerIdx, Consume consume, Reason source) {
        if (consume == null || consume.getCount() == 0) {
            LogUtil.warn("ConsumeManager.consumeMaterial, consume is null or consume count is 0");
            return true;
        }

        if (playerIdx == null) {
            return false;
        }

        int rewardType = consume.getRewardTypeValue();
        switch (rewardType) {
            case RewardTypeEnum.RTE_Gold_VALUE:
            case RewardTypeEnum.RTE_Diamond_VALUE:
            case RewardTypeEnum.RTE_Coupon_VALUE:
            case RewardTypeEnum.RTE_HolyWater_VALUE: {
                playerEntity player = playerCache.getByIdx(playerIdx);
                if (player == null) {
                    return false;
                }
                return SyncExecuteFunction.executeFunction(player, entity ->
                        player.consumeCurrency(consume.getRewardType(), consume.getCount(), source));
            }
            case RewardTypeEnum.RTE_Item_VALUE: {
                itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
                if (itemBag == null) {
                    return false;
                }

                return SyncExecuteFunction.executeFunction(itemBag, entity -> itemBag.removeItem(consume.getId(), consume.getCount(), source, true));
            }
            case RewardTypeEnum.RTE_PetFragment_VALUE: {
                Map<Integer, Integer> useMap = new HashMap<>();
                useMap.put(consume.getId(), consume.getCount());
                return PetFragmentServiceImpl.getInstance().removeFragment(playerIdx, useMap);
            }
            case RewardTypeEnum.RTE_MistStamina_VALUE: {
                playerEntity player = playerCache.getByIdx(playerIdx);
                if (player == null) {
                    return false;
                }
                return SyncExecuteFunction.executeFunction(player, entity->entity.removeMistStamina(consume.getCount()));
            }
            default: {
                LogUtil.warn("unSupported consumeType = " + consume.toString());
                return false;
            }
        }
    }

    /**
     * 检查指定道具是否足够, 暂且只支持道具和货币
     *
     * @param playerIdx
     * @param consumes
     * @return
     */
    public boolean materialIsEnoughByList(String playerIdx, List<Consume> consumes) {
        if (GameUtil.collectionIsEmpty(consumes)) {
            return true;
        }
        if (StringHelper.isNull(playerIdx)) {
            return false;
        }

        for (Consume consume : consumes) {
            int rewardType = consume.getRewardTypeValue();
            switch (rewardType) {
                case RewardTypeEnum.RTE_Gold_VALUE:
                case RewardTypeEnum.RTE_Diamond_VALUE:
                case RewardTypeEnum.RTE_Coupon_VALUE:
                case RewardTypeEnum.RTE_HolyWater_VALUE: {
                    playerEntity player = playerCache.getByIdx(playerIdx);
                    if (player == null) {
                        return false;
                    }

                    return SyncExecuteFunction.executeFunction(player, entity ->
                            player.currencyIsEnough(consume.getRewardType(), consume.getCount()));
                }
                case RewardTypeEnum.RTE_Item_VALUE: {
                    itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
                    if (itemBag == null) {
                        return false;
                    }

                    return SyncExecuteFunction.executeFunction(itemBag, entity -> itemBag.itemIsEnough(consume.getId(), consume.getCount()));
                }
                case RewardTypeEnum.RTE_MistStamina_VALUE: {
                    playerEntity player = playerCache.getByIdx(playerIdx);
                    if (player == null) {
                        return false;
                    }
                    return SyncExecuteFunction.executeFunction(player, entity->entity.getDb_data().getMistForestData().getStamina() >= consume.getCount());
                }
                default: {
                    LogUtil.warn("unSupported consumeType = " + consume.toString());
                    return false;
                }
            }
        }
        return true;
    }

    public boolean materialIsEnough(String playerIdx, Consume consumes) {
        return materialIsEnoughByList(playerIdx, Collections.singletonList(consumes));
    }

    public boolean asyncConsumeMaterial(String playerIdx, Consume consume, Reason reason) {
        return asyncConsumeMaterialByList(playerIdx, Collections.singletonList(consume), reason);
    }

    public boolean asyncConsumeMaterialByList(String playerIdx, List<Consume> consumes, Reason reason) {
        Future<Boolean> submit
                = GlobalThread.getInstance().submit(() -> consumeMaterialByList(playerIdx, consumes, reason));
        try {
            return Boolean.TRUE.equals(submit.get());
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
        }
        return false;
    }

    /**
     * 判断可以消耗几个单位的给定消耗，暂只支持货币和道具
     *
     * @param playerIdx
     * @param consume
     * @return
     */
    public int canConsumeCount(String playerIdx, Consume consume) {
        if (StringUtils.isEmpty(playerIdx) || consume == null) {
            return 0;
        }

        long ownerCount = this.getConsumItemCount(playerIdx,consume);

        return (int) Math.min(ownerCount / consume.getCount(), Integer.MAX_VALUE);
    }

    public long getConsumItemCount(String playerIdx, Consume consume){
        long ownerCount = 0L;

        if (consume.getRewardType() == RewardTypeEnum.RTE_Gold
                || consume.getRewardType() == RewardTypeEnum.RTE_Diamond
                || consume.getRewardType() == RewardTypeEnum.RTE_Coupon
                || consume.getRewardType() == RewardTypeEnum.RTE_HolyWater) {
            playerEntity player = playerCache.getByIdx(playerIdx);
            ownerCount = player == null ? 0 : player.getCurrencyCount(consume.getRewardType());
        } else if (consume.getRewardType() == RewardTypeEnum.RTE_Item) {
            itembagEntity entity = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
            ownerCount = entity == null ? 0 : entity.getItemCount(consume.getId());
        } else {
            LogUtil.error("ConsumeManager.canConsumeCount, unsupported judgment reward type, type:" + consume.getRewardType());
        }
        return ownerCount;
    }

}
