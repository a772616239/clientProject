package model.petfragment.dbCache.service;

import cfg.GameConfig;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import common.GameConst;
import common.GlobalData;
import common.IdGenerator;
import common.SyncExecuteFunction;
import model.pet.dbCache.petCache;
import model.pet.entity.PetComposeHelper;
import model.petfragment.dbCache.petfragmentCache;
import model.petfragment.entity.FragmentUseResult;
import model.petfragment.entity.petfragmentEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.lang.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.DailyDateLog;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.PetMessage;
import protocol.PetMessage.PetFragment;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.LogUtil;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static model.petfragment.entity.petfragmentEntity.sendFragmentAmount;

/**
 * @author xiao_FL
 * @date 2019/5/22
 */
public class PetFragmentServiceImpl implements PetFragmentService {
    /**
     * 随机宠物
     */
    private static final int RANDOM_FRAGMENT = 0;


    private petfragmentCache petFragmentCacheInstance = petfragmentCache.getInstance();

    private static PetFragmentServiceImpl petFragmentService = new PetFragmentServiceImpl();

    public static PetFragmentServiceImpl getInstance() {
        return petFragmentService;
    }

    @Override
    public petfragmentEntity getFragmentByPlayer(String playerId) {
        petfragmentEntity cache = petFragmentCacheInstance.getCacheByPlayer(playerId);
        if (cache == null) {
            // 玩家首次请求符文背包，初始化
            cache = new petfragmentEntity(playerId);
            petFragmentCacheInstance.add(cache);
            return cache;
        }
        return cache;
    }

    @Override
    public FragmentUseResult useFragment(String fragmentId, String playerId, int amount, boolean showReward, boolean useAll) {
        FragmentUseResult result = new FragmentUseResult();
        petfragmentEntity cache = petFragmentCacheInstance.getCacheByPlayer(playerId);
        if (cache == null || (!useAll && StringUtils.isBlank(fragmentId))) {
            result.setCodeEnum(RetCodeEnum.RCE_ErrorParam);
            return result;
        }
        List<PetFragment> petFragments = cache.getFragmentList();
        //如果使用所有碎片,需要先对碎片进行排序 品质->id降序
        if (useAll) {
            petFragments = new ArrayList<>(petFragments);
            sortFragmentsByRarityAndIdDesc(petFragments);
        }
        amount = useAll ? Math.min(petCache.getInstance().getRemainCapacity(playerId),
                GameConfig.getById(GameConst.CONFIG_ID).getMaxonekeycomposepetcount()) : amount;
        // 推送碎片消息
        List<PetFragment> msg = new ArrayList<>();
        //添加宠物列表
        List<Reward> gainPet = new ArrayList<>();
        List<PetMessage.PetReward> petRewards = new ArrayList<>();
        for (PetFragment fragmentTemp : petFragments) {
            if (!useAll && !fragmentId.equals(fragmentTemp.getId())) {
                continue;
            }
            // 读取配置，获得合成需要的碎片数
            PetFragmentConfigObject fragmentConfig = PetFragmentConfig.getById(fragmentTemp.getCfgId());
            if (fragmentConfig == null) {
                LogUtil.warn("useFragment continue by PetFragmentConfigObject null,cfgId:{}", fragmentTemp.getCfgId());
                continue;
            }
            int num = useAll ? fragmentTemp.getNumber() / fragmentConfig.getAmount() : amount;
            num = Math.min(num, amount);
            int total = fragmentConfig.getAmount() * num;

            RetCodeEnum checkBeforeUse = checkBeforeUse(fragmentTemp, fragmentConfig, num, total);
            if (checkBeforeUse != RetCodeEnum.RCE_Success) {
                if (!useAll) {
                    result.setCodeEnum(checkBeforeUse);
                    return result;
                }
                continue;
            }
            amount -= num;
            int finalNum = num;
            RetCodeEnum codeEnum = SyncExecuteFunction.executeFunction(cache, cacheTemp -> {
                PetFragment refreshFragment = fragmentTemp.toBuilder().setNumber(fragmentTemp.getNumber() - total).build();

                //宠物碎片合成不堆叠显示
                frameToPetReward(finalNum, fragmentConfig, gainPet, petRewards);

                //扣除碎片
                List<PetFragment> fragmentList = new ArrayList<>(cacheTemp.getFragmentList());
                fragmentList.remove(fragmentTemp);

                if (refreshFragment.getNumber() > 0) {
                    fragmentList.add(refreshFragment);
                }

                cacheTemp.setFragmentList(fragmentList);
                petFragmentCacheInstance.flush(cacheTemp);

                msg.add(refreshFragment);

                //统计日志:碎片变化
                LogService.getInstance().submit(new DailyDateLog(playerId, true, RewardTypeEnum.RTE_PetFragment,
                        fragmentTemp.getCfgId(), fragmentTemp.getNumber(), total, refreshFragment.getNumber(),
                        ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetFragment)));
                return RetCodeEnum.RCE_Success;
            });
            if (codeEnum != RetCodeEnum.RCE_Success) {
                result.setCodeEnum(codeEnum);
                return result;
            }
        }

        RewardManager.getInstance().doRewardByList(playerId, gainPet,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetFragment), false);

        if (showReward) {
            GlobalData.getInstance().sendDisPetRewardMsg(playerId, petRewards);
        }

        //目标：累积合成x只x品质宠物
        for (PetMessage.PetReward reward : petRewards) {
            EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_CumuCompoundPet, 1, reward.getRarity());
        }
        sendFragmentAmount(playerId, msg);
        result.setCodeEnum(RetCodeEnum.RCE_Success);
        result.setGainPet(petRewards);
        return result;
    }

    private RetCodeEnum checkBeforeUse(PetFragment fragmentTemp, PetFragmentConfigObject fragmentConfig, int num, int total) {
        if (fragmentConfig == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        // 检查碎片数是否足够
        if (num <= 0 || total > fragmentTemp.getNumber()) {
            return RetCodeEnum.RCE_Pet_FragmentNotEnough;
        }
        return RetCodeEnum.RCE_Success;
    }

    private void sortFragmentsByRarityAndIdDesc(List<PetFragment> petFragments) {
        petFragments.sort((pf1, pf2) -> {
            PetFragmentConfigObject config1 = PetFragmentConfig.getById(pf1.getCfgId());
            PetFragmentConfigObject config2 = PetFragmentConfig.getById(pf2.getCfgId());
            if (config1 != null && config2 != null && config1.getDebrisrarity() != config2.getDebrisrarity()) {
                return config2.getDebrisrarity() - config1.getDebrisrarity();
            }
            return pf2.getCfgId() - pf1.getCfgId();

        });
    }

    private void frameToPetReward(int amount, PetFragmentConfigObject fragmentConfig, List<Reward> gainPet, List<PetMessage.PetReward> petRewards) {
        //宠物碎片的宠物bookId这里需要转成合成id
        int bookId;
        if (RANDOM_FRAGMENT != fragmentConfig.getPetid()) {
            for (int i = 0; i < amount; i++) {
                bookId = fragmentConfig.getPetid();
                gainPet.add(RewardUtil.parseReward(RewardTypeEnum.RTE_Pet, PetComposeHelper.getComposeId(fragmentConfig.getPetid(),fragmentConfig.getDebrisrarity()), 1));
                petRewards.add(PetMessage.PetReward.newBuilder()
                        .setBookId(bookId).setRarity(fragmentConfig.getDebrisrarity()).build());
            }
        } else {
            for (int i = 0; i < amount; i++) {
                bookId = RandomUtil.getRandomPet(fragmentConfig.getDebrisrarity(), fragmentConfig.getDebristype(), fragmentConfig.getPetcore());
                gainPet.add(RewardUtil.parseReward(RewardTypeEnum.RTE_Pet, PetComposeHelper.getComposeId(bookId, fragmentConfig.getDebrisrarity()), 1));
                petRewards.add(PetMessage.PetReward.newBuilder()
                        .setBookId(bookId).setRarity(fragmentConfig.getDebrisrarity()).build());
            }
        }
    }


    @Override
    public boolean playerObtainFragment(String playerId, Map<Integer, Integer> fragmentMap, Reason reason) {
        petfragmentEntity cache = petFragmentCacheInstance.getCacheByPlayer(playerId);
        if (cache == null || fragmentMap == null) {
            return false;
        }
        // 查询玩家已拥有碎片
        return SyncExecuteFunction.executePredicate(cache, cacheTemp -> {
            List<PetFragment> newDisFragment = new ArrayList<>();
            List<PetFragment> oldFragmentList = cacheTemp.getFragmentList();
            List<PetFragment> newFragmentList = new ArrayList<>(oldFragmentList);
            for (Integer cfgId : fragmentMap.keySet()) {
                boolean contain = false;
                int beforeAdd = 0;
                for (PetFragment petFragment : oldFragmentList) {
                    // 检查到玩家拥有碎片和添加碎片类型相同
                    if (cfgId.equals(petFragment.getCfgId())) {
                        PetFragment fragment = petFragment;
                        beforeAdd = fragment.getNumber();
                        fragment = fragment.toBuilder().setNumber(petFragment.getNumber() + fragmentMap.get(cfgId)).build();
                        newFragmentList.remove(petFragment);
                        newFragmentList.add(fragment);
                        newDisFragment.add(fragment);
                        contain = true;
                        break;
                    }
                }

                if (!contain) {
                    PetFragment.Builder temp = PetFragment.newBuilder();
                    temp.setId(IdGenerator.getInstance().generateId());
                    temp.setCfgId(cfgId);
                    temp.setNumber(fragmentMap.get(cfgId));
                    newFragmentList.add(temp.build());
                    newDisFragment.add(temp.build());
                }
                LogService.getInstance().submit(new DailyDateLog(playerId, false, RewardTypeEnum.RTE_PetFragment,
                        cfgId, beforeAdd, fragmentMap.get(cfgId), beforeAdd + fragmentMap.get(cfgId), reason));
            }
            cacheTemp.setFragmentList(newFragmentList);
            petFragmentCacheInstance.flush(cacheTemp);
            // 推送消息
            sendFragmentAmount(playerId, newDisFragment);
            return true;
        });
    }

    @Override
    public boolean removeFragment(String playerId, Map<Integer, Integer> fragmentMap) {
        petfragmentEntity cache = petFragmentCacheInstance.getCacheByPlayer(playerId);
        if (cache != null && fragmentMap != null) {
            SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
                // 查询玩家已拥有碎片
                List<PetFragment> newFragmentList = new ArrayList<>(cacheTemp.getFragmentList());
                List<PetFragment> fragmentList = new ArrayList<>();
                // 删除列表
                for (Integer removeId : fragmentMap.keySet()) {
                    for (PetFragment fragment : cacheTemp.getFragmentList()) {
                        if (removeId.equals(fragment.getCfgId())) {
                            // 碎片被扣完
                            if (fragment.getNumber() == fragmentMap.get(removeId)) {
                                newFragmentList.remove(fragment);
                                fragmentList.add(fragment.toBuilder().setNumber(0).build());
                            } else {
                                // 碎片有剩余
                                newFragmentList.remove(fragment);
                                newFragmentList.add(fragment.toBuilder().setNumber(fragment.getNumber() - fragmentMap.get(removeId)).build());
                                // 推送数量修改
                                fragmentList.add(fragment);
                            }
                        }
                    }
                }
                sendFragmentAmount(playerId, fragmentList);
                cache.setFragmentList(newFragmentList);
                petFragmentCacheInstance.flush(cache);
            });
            return true;
        }
        return false;
    }
}
