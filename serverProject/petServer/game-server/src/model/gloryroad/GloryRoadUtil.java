package model.gloryroad;

import cfg.FunctionOpenLvConfig;
import cfg.GloryRoadConfig;
import cfg.GloryRoadConfigObject;
import common.GameConst;
import common.entity.RankingQuerySingleResult;
import model.arena.ArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.playerConstant;
import model.player.util.PlayerUtil;
import org.springframework.util.CollectionUtils;
import protocol.Battle.BattlePetData;
import protocol.Common.EnumFunction;
import protocol.GloryRoad.NodePlayerInfo;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author huhan
 * @date 2021/3/16
 */
public class GloryRoadUtil {

    public static final Predicate<String> LV_CONDITION
            = e -> PlayerUtil.queryFunctionUnlock(e, EnumFunction.EF_GloryRoad);

    public static List<GloryRoadGroup<RankingQuerySingleResult>> splitGroup(List<RankingQuerySingleResult> resultsList) {
        if (CollectionUtils.isEmpty(resultsList)) {
            LogUtil.error("GloryRoadUtil.splitGroup, params is empty");
            return null;
        }

        GloryRoadConfigObject gloryConfig = GloryRoadConfig.getById(GameConst.CONFIG_ID);
        if (gloryConfig == null) {
            LogUtil.error("GloryRoadUtil.splitGroup, gloryConfig is null");
            return null;
        }

        int eachGroupSize = gloryConfig.getJoinplayercount() / gloryConfig.getGroupsize();
        List<List<RankingQuerySingleResult>> rankingSplitList = GameUtil.splitList(resultsList, eachGroupSize);
        if (CollectionUtils.isEmpty(rankingSplitList)) {
            LogUtil.error("GloryRoadUtil.splitGroup, split list failed");
            return null;
        }
        //先随机交换每一组的顺序 然后再合并到一起
        List<RankingQuerySingleResult> randomList = rankingSplitList.stream()
                .map(GloryRoadUtil::randomExchange)
                .reduce((e1, e2) -> {
                    if (e2 == null) {
                        return e1;
                    }
                    e1.addAll(e2);
                    return e1;
                })
                .orElse(resultsList);


        List<List<RankingQuerySingleResult>> sizeSplit = splitList(randomList, gloryConfig.getGroupsize());
        if (CollectionUtils.isEmpty(sizeSplit)) {
            LogUtil.error("GloryRoadUtil.splitGroup, sizeSplit list split failed");
            return null;
        }

        List<GloryRoadGroup<RankingQuerySingleResult>> tempResult = sizeSplit.stream()
                .map(list -> {
                    GloryRoadGroup<RankingQuerySingleResult> group = new GloryRoadGroup<>(eachGroupSize);
                    group.addAllMembers(list);
                    return group;
                })
                .collect(Collectors.toList());

        GloryRoadGroup<GloryRoadGroup<RankingQuerySingleResult>> resultList = new GloryRoadGroup<>(tempResult.size());
        resultList.addAllMembers(tempResult);
        return new ArrayList<>(resultList.getIndexMap().values());
    }

    public static <T> List<T> randomExchange(List<T> targetList) {
        if (CollectionUtils.isEmpty(targetList)) {
            return null;
        }

        int size = targetList.size();

        //任意取一个位置放置到末尾
        Random random = new Random();
        for (int i = 0; i < size / 2; i++) {
            T oldVal = targetList.remove(random.nextInt(size));
            targetList.add(oldVal);
        }
        return targetList;
    }

    /**
     * 依次填充每个分组list
     *
     * @param list
     * @param splitSize 需要拆分的分组数量
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitList(List<T> list, int splitSize) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        if (splitSize <= 0) {
            return Collections.singletonList(list);
        }

        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < splitSize; i++) {
            result.add(new ArrayList<>());
        }

        int resultIndex = 0;
        for (T t : list) {
            List<T> subList = result.get(resultIndex);
            subList.add(t);

            if (++resultIndex >= result.size()) {
                resultIndex = 0;
            }
        }
        return result;
    }

    public static NodePlayerInfo buildNodePlayerInfo(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return null;
        }

        NodePlayerInfo.Builder resultBuilder = NodePlayerInfo.newBuilder();
        resultBuilder.setPlayerIdx(player.getIdx());
        resultBuilder.setName(player.getName());
        resultBuilder.setAvatar(player.getAvatar());
        resultBuilder.setAvatarBorder(player.getDb_data().getCurAvatarBorder());
        if (resultBuilder.getAvatarBorder() == playerConstant.AvatarBorderWithRank) {
            resultBuilder.setAvatarBorderRank(ArenaManager.getInstance().getPlayerRank(player.getIdx()));
        }
        resultBuilder.setNewTitleId(player.getDb_data().getNewTitle().getCurEquip());
        resultBuilder.setVipLv(player.getVip());

        return resultBuilder.build();
    }

    /**
     * 计算战力大的玩家胜率,双方战力相等时直接返回0.5
     *
     * @return
     */
    public static double calcLargePowerWinRate(List<BattlePetData> largePetBattle, long largePower,
                                               List<BattlePetData> smallPetBattle, long smallPower) {
        if (largePower == smallPower) {
            return 0.5D;
        }
        //小的一方战力为0时,直接判胜利
        if (smallPower == 0) {
            return 1D;
        }
        double largeVariance = calcVariance(largePetBattle, largePower);
        double smallVariance = calcVariance(smallPetBattle, smallPower);
        LogUtil.info("GloryRoadQuiz.calcOdds, largeVariance:" + largeVariance + ", smallVariance:" + smallVariance);

        return (largePower * 1.0 / smallPower) * 0.5 + (largeVariance - smallVariance) / largeVariance;
    }

    /**
     * 计算方差
     */
    public static double calcVariance(List<BattlePetData> petBattle, double power) {
        if (CollectionUtils.isEmpty(petBattle) || power <= 0) {
            return 0D;
        }

        double average = power / petBattle.size();

        double sum = 0;
        for (BattlePetData battlePetData : petBattle) {
            sum += Math.pow((battlePetData.getAbility() - average), 2);
        }

        return Math.sqrt(sum / petBattle.size());
    }

    /**
     * 战力大的一方的赔率范围
     */
    public static final double LARGE_WIN_RATE_UPPER_LIMIT = 0.9;
    public static final double LARGE_WIN_RATE_LOWER_LIMIT = 0.1;

    public static double fixWinRate(double rate) {
        if (rate < LARGE_WIN_RATE_LOWER_LIMIT) {
            return LARGE_WIN_RATE_LOWER_LIMIT;
        } else if (rate > LARGE_WIN_RATE_UPPER_LIMIT) {
            return LARGE_WIN_RATE_UPPER_LIMIT;
        }
        return rate;
    }

    public static double calcLargePowerWinRateWithFix(List<BattlePetData> largePetBattle, long largePower,
                                                      List<BattlePetData> smallPetBattle, long smallPower) {
        double winRate = calcLargePowerWinRate(largePetBattle, largePower, smallPetBattle, smallPower);
        return fixWinRate(winRate);
    }
}

