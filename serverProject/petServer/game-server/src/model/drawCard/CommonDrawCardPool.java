package model.drawCard;

import cfg.DrawCard;
import cfg.DrawCardAdvanced;
import cfg.DrawCardAdvancedObject;
import cfg.DrawCommonCardConfig;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.Common.RewardTypeEnum;
import protocol.PlayerDB.CommonAdvanceInfo;
import protocol.PlayerDB.CommonAdvanceInfo.Builder;
import protocol.PlayerDB.DB_DrawCardData;
import util.ArrayUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/07/27
 */
public class CommonDrawCardPool extends DrawCardPool {

    private final Map<Integer, List<OddsRandom>> vipLvSpecialPoolMap = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> vipLvSpecialPoolTotalOddsMap = new ConcurrentHashMap<>();

    /**
     * vip随机也在轮次内生效
     * 轮次内保底特殊处理,
     *
     * @param playerIdx
     * @param quality
     * @return
     */
    @Override
    public OddsRandom randomByQuality(String playerIdx, final int quality, boolean mustGetFlag) {
        boolean mustGet = DrawCardUtil.canMustGet(playerIdx, mustGetFlag);
        final int finalQuality = mustGet ? DrawCardManager.HIGHEST_QUALITY : quality;

        OddsRandom result;
        //特殊抽卡
        if (finalQuality == DrawCardManager.HIGHEST_QUALITY
                && canGetSpecialReward(playerIdx)) {
            result = randomInSpecialPool(playerIdx);
            reduceSpecialTimes(playerIdx);
        } else {
            result = commonRandom(playerIdx, finalQuality, mustGet);
        }

        if (mustGet) {
            clearMustGetDrawTimes(playerIdx);
        } else if (!mustGetFlag) {
            addMustGetDrawTimes(playerIdx);
        }
        return result;
    }

    private OddsRandom commonRandom(String playerIdx, int quality, boolean mustGet) {
        OddsRandom result = null;

        CommonAdvanceInfo playerAdvance = getPlayerCommonAdvance(playerIdx);
        DrawCardAdvancedObject advanceCfg;
        //还在固定轮次内
        if (playerAdvance != null
                && (advanceCfg = DrawCardAdvanced.getById(playerAdvance.getAdvanceId())) != null
                && advanceCfg.getGroup().length > playerAdvance.getNextAdvanceIndex()) {
            int[] reward = advanceCfg.getGroup()[playerAdvance.getNextAdvanceIndex()];

            //当前奖励不是最高品质
            int rewardQuality = RewardUtil.getQuality(reward[0], reward[1]);
            if (mustGet) {
                if (rewardQuality != DrawCardManager.HIGHEST_QUALITY
                        || !isCompletePet(reward)) {
                    result = randomMustGetInAdvance(playerIdx, advanceCfg);
                    LogUtil.info("CommonDrawCardPool.commonRandom, random must get in advance, result:"
                            + ArrayUtil.toString(result == null ? new int[]{} : result.getRewards()));
                }
            }

            //如果不是必出直接用当前序号奖励
            if (result == null) {
                result = new OddsRandomImpl(reward).setQuality(rewardQuality);
            }

            //增加玩家固定轮次序号
            addPlayerAdvanceIndex(playerIdx);
        } else {
            result = super.randomByQuality(playerIdx, quality, false);
        }
        return result;
    }

    /**
     * 判断碎片奖励是否能合成一个完整的紫色宠物
     *
     * @param reward
     * @return
     */
    private boolean isCompletePet(int[] reward) {
        if (reward == null || reward.length < 3) {
            return true;
        }

        if (reward[0] == RewardTypeEnum.RTE_PetFragment_VALUE) {
            PetFragmentConfigObject fragmentConfig = PetFragmentConfig.getById(reward[1]);
            if (fragmentConfig == null) {
                return true;
            }
            return reward[2] >= fragmentConfig.getAmount();
        }

        return true;
    }

    private List<OddsRandom> getVipLvSpecialPool(int vipLv) {
        List<OddsRandom> oddsRandoms = this.vipLvSpecialPoolMap.get(vipLv);
        if (CollectionUtils.isNotEmpty(oddsRandoms)) {
            return oddsRandoms;
        }

        VIPConfigObject vipCfg = VIPConfig.getById(vipLv);
        if (vipCfg == null) {
            LogUtil.error("model.drawCard.CommonDrawCardPool.getVipLvPool, vipLv:" + vipLv + " is not exist");
            return null;
        }

        List<OddsRandom> vipLvPool = Arrays.stream(vipCfg.getCommonspecialdrawpool())
                .boxed()
                .map(DrawCommonCardConfig::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.vipLvSpecialPoolMap.put(vipLv, vipLvPool);
        return vipLvPool;
    }

    private int getVipLvSpecialPoolTotalOdds(int vipLv) {
        Integer totalOdds = this.vipLvSpecialPoolTotalOddsMap.get(vipLv);
        if (totalOdds == null) {
            List<OddsRandom> pool = getVipLvSpecialPool(vipLv);
            if (CollectionUtils.isNotEmpty(pool)) {
                totalOdds = pool.stream()
                        .map(OddsRandom::getOdds)
                        .reduce(Integer::sum)
                        .orElse(0);
                if (totalOdds <= 0) {
                    LogUtil.error("CommonDrawCardPool.getVipLvSpecialPoolTotalOdds, vipLv:" + vipLv + ", total odds is less than 0");
                }
                this.vipLvSpecialPoolTotalOddsMap.put(vipLv, totalOdds);
            }
        }
        return totalOdds;
    }

    private OddsRandom randomInSpecialPool(String playerIdx) {
        int vipLv = PlayerUtil.queryPlayerVipLv(playerIdx);
        List<OddsRandom> vipLvSpecialPool = getVipLvSpecialPool(vipLv);
        if (CollectionUtils.isEmpty(vipLvSpecialPool)) {
            return null;
        }
        return random(vipLvSpecialPool, getVipLvSpecialPoolTotalOdds(vipLv));
    }

    /**
     * 是否可以获得特殊奖励， 奖励次数由vip提升获取
     */
    private boolean canGetSpecialReward(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }

        return SyncExecuteFunction.executePredicate(player, e -> {
            int remainSpecialTimes = player.getDb_data().getDrawCard().getCommonRemainSpecialTimes();
            if (remainSpecialTimes > 0) {
                return DrawCard.getById(GameConst.CONFIG_ID).getCommondrawspecialodds() > RandomUtils.nextInt(100);
            }
            return false;
        });
    }

    private void reduceSpecialTimes(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(player, e -> {
            DB_DrawCardData.Builder drawCardBuilder = player.getDb_data().getDrawCardBuilder();
            drawCardBuilder.setCommonRemainSpecialTimes(drawCardBuilder.getCommonRemainSpecialTimes() - 1);
            LogUtil.debug("CommonDrawCardPool.reduceSpecialTimes, player:" + playerIdx
                    + " reduce a special times, remain times:" + drawCardBuilder.getCommonRemainSpecialTimes());
        });
    }

    private OddsRandom randomMustGetInAdvance(String playerIdx, DrawCardAdvancedObject advanceCfg) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null || advanceCfg == null) {
            return null;
        }

        CommonAdvanceInfo advanceInfo = player.getDb_data().getDrawCard().getCommonAdvanceInfo();

        List<AdvanceRandom> randomRewards = new ArrayList<>();
        int totalOdds = 0;
        for (int i = 0; i < advanceCfg.getInnermustpool().length; i++) {
            if (advanceInfo.getGainMustIndexList().contains(i)) {
                continue;
            }
            int[] randomArray = advanceCfg.getInnermustpool()[i];
            int[] rewardsArray = new int[]{randomArray[0], randomArray[1], randomArray[2]};
            randomRewards.add(new AdvanceRandom(i, rewardsArray, randomArray[3],  randomArray[4] > 0));
            totalOdds += randomArray[3];
        }

        OddsRandom result = random(randomRewards, totalOdds);
        if (result == null) {
            return null;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            CommonAdvanceInfo.Builder advanceInfoBuilder =
                    player.getDb_data().getDrawCardBuilder().getCommonAdvanceInfoBuilder();
            advanceInfoBuilder.addGainMustIndex(result.getId());
        });

        return result;
    }

    private CommonAdvanceInfo getPlayerCommonAdvance(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return null;
        }

        return SyncExecuteFunction.executeFunction(entity, e -> {
            Builder advanceInfoBuilder = entity.getDb_data().getDrawCardBuilder().getCommonAdvanceInfoBuilder();
            if (DrawCardAdvanced.getById(advanceInfoBuilder.getAdvanceId()) == null) {
                DrawCardAdvancedObject newAdvance = DrawCardAdvanced.randomAdvance();
                if (newAdvance != null) {
                    advanceInfoBuilder.setAdvanceId(newAdvance.getId());
                    LogUtil.debug("CommonDrawCardPool.getPlayerCommonAdvance, playerIdx:" + playerIdx
                            + ", set advance id:" + newAdvance.getId());
                }
            }
            return advanceInfoBuilder.build();
        });
    }

    private void addPlayerAdvanceIndex(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            Builder advanceInfoBuilder = entity.getDb_data().getDrawCardBuilder().getCommonAdvanceInfoBuilder();
            int nextIndex = advanceInfoBuilder.getNextAdvanceIndex() + 1;
            advanceInfoBuilder.setNextAdvanceIndex(nextIndex);
            LogUtil.info("CommonDrawCardPool.addPlayerAdvanceIndex, playerIdx:" + playerIdx
                    + ", cur advance index:" + advanceInfoBuilder.getNextAdvanceIndex());
        });
    }



    /**
     * 增加保底轮次
     *
     * @return
     */
    private void addMustGetDrawTimes(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_DrawCardData.Builder drawCardBuilder = entity.getDb_data().getDrawCardBuilder();
            drawCardBuilder.setCommonMustDrawCount(drawCardBuilder.getCommonMustDrawCount() + 1);
            LogUtil.info("CommonDrawCardPool.addMustGetDrawTimes, playerIdx:" + playerIdx
                    + ", add must draw times, cur times:" + drawCardBuilder.getCommonMustDrawCount());
        });
    }

    /**
     * 清除保底轮次
     *
     * @return
     */
    private void clearMustGetDrawTimes(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }

        playerEntity entity = playerCache.getByIdx(playerIdx);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_DrawCardData.Builder drawCardBuilder = entity.getDb_data().getDrawCardBuilder();
            drawCardBuilder.clearCommonMustDrawCount();
            LogUtil.info("CommonDrawCardPool.clearMustGetDrawTimes, playerIdx:" + playerIdx + ", clear common draw times");
        });
    }
}
