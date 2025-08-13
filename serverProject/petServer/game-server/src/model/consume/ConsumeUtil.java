package model.consume;

import org.springframework.util.CollectionUtils;
import protocol.Common.Consume;
import protocol.Common.Consume.Builder;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumeUtil {

    public static Consume parseConsume(int[] consumeIntArr) {
        if (consumeIntArr == null || consumeIntArr.length < 3) {
            LogUtil.error("ConsumeUtil.parseConsume, error param");
            return null;
        }
        return parseConsume(consumeIntArr[0], consumeIntArr[1], consumeIntArr[2]);
    }

    public static Consume.Builder parseConsumeBuilder(int[] consumeIntArr) {
        if (consumeIntArr == null || consumeIntArr.length < 3) {
            LogUtil.error("ConsumeUtil.parseConsume, error param");
            return null;
        }
        return parseConsumeBuilder(consumeIntArr[0], consumeIntArr[1], consumeIntArr[2]);
    }

    public static List<Consume> parseToConsumeList(int[][] consumeArr) {
        if (consumeArr == null) {
            LogUtil.error("ConsumeUtil.parseConsume, error param");
            return null;
        }

        List<Consume> result = new ArrayList<>();
        for (int[] ints : consumeArr) {
            Consume consume = parseConsume(ints);
            if (consume != null) {
                result.add(consume);
            }
        }

        return result;
    }

    /**
     * 加倍材料的消耗
     *
     * @param consume
     * @param multi  > 1
     * @return
     */
    public static Consume multiConsume(Consume consume, int multi) {
        if (consume == null || multi <= 1) {
            return consume;
        }

        Builder builder = consume.toBuilder();
        builder.setCount(builder.getCount() * multi);
        return builder.build();
    }

    public static List<Consume> multiConsume(List<Consume> consumeList, int multi) {
        if (consumeList == null || multi <= 1) {
            return consumeList;
        }
        for (int i = 0; i < consumeList.size(); i++) {
            consumeList.set(i, multiConsume(consumeList.get(i), multi));
        }
        return consumeList;
    }

    /**
     * 转化并加倍
     *
     * @param intArr
     * @param multi
     * @return
     */
    public static Consume parseAndMulti(int[] intArr, int multi) {
        return multiConsume(parseConsume(intArr), multi);
    }

    /**
     * 转化并加倍
     *
     * @param intArr
     * @param multi
     * @return
     */
    public static List<Consume> parseListAndMulti(int[][] intArr, int multi) {
        return multiConsume(parseToConsumeList(intArr), multi);
    }

    /**
     * 将消耗类转化为奖励类
     *
     * @param consumeList
     * @return
     */
    public static List<Reward> parseConsumeToReward(List<Consume> consumeList) {
        if (consumeList == null || consumeList.isEmpty()) {
            return null;
        }

        List<Reward> result = new ArrayList<>();
        for (Consume consume : consumeList) {
            Reward.Builder builder = Reward.newBuilder();
            builder.setRewardType(consume.getRewardType());
            builder.setId(consume.getId());
            builder.setCount(consume.getCount());
            result.add(builder.build());
        }
        return result;
    }

    public static Consume parseConsume(int rewardType, int id, int count) {
        Consume.Builder consume  = Consume.newBuilder();
        consume.setRewardType(RewardTypeEnum.forNumber(rewardType));
        consume.setId(id);
        consume.setCount(count);
        return consume.build();
    }

    public static Consume.Builder parseConsumeBuilder(int rewardType, int id, int count) {
        Consume.Builder consume  = Consume.newBuilder();
        consume.setRewardType(RewardTypeEnum.forNumber(rewardType));
        consume.setId(id);
        consume.setCount(count);
        return consume;
    }

    /**
     * 将两个consume集合按照(type,id) 进行合并
     * @param consumes1
     * @param consumes2
     * @return
     */
    public static List<Consume> mergeConsumeByTypeAndId(List<Consume> consumes1, List<Consume> consumes2) {
        if (CollectionUtils.isEmpty(consumes1) && CollectionUtils.isEmpty(consumes2)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(consumes1) || CollectionUtils.isEmpty(consumes2)) {
            return CollectionUtils.isEmpty(consumes1) ? consumes2 : consumes1;
        }
        List<Consume> merge = new ArrayList<>();
        merge.addAll(consumes1);
        merge.addAll(consumes2);
        Map<RewardTypeEnum, Map<Integer, List<Consume>>> collect = merge.stream().collect(
                Collectors.groupingBy(Consume::getRewardType, Collectors.groupingBy(Consume::getId)));

        List<Consume> result = new ArrayList<>();
        for (Map.Entry<RewardTypeEnum, Map<Integer, List<Consume>>> integerListEntry : collect.entrySet()) {
            for (Map.Entry<Integer, List<Consume>> consumeList : integerListEntry.getValue().entrySet()) {
                Builder builder = Consume.newBuilder();
                for (Consume consume : consumeList.getValue()) {
                    builder.setCount(builder.getCount() + consume.getCount()).setRewardType(consume.getRewardType()).setId(consume.getId());
                }
                result.add(builder.build());
            }
        }
        return result;
    }

    /**
     * 获取指定类型和指定id的消耗个数
     * @param consumes
     * @param type
     * @param id  <= 0 统计所有同类型消耗个数， >0 统计指定类型和指定id的消耗
     * @return
     */
    public static int getConsumeCount(List<Consume> consumes, RewardTypeEnum type, int id) {
        if (GameUtil.collectionIsEmpty(consumes)) {
            return 0;
        }

        int result = 0;
        for (Consume consume : consumes) {
            if (consume.getRewardType() == type && (id <= 0 || id == consume.getId())) {
                result += consume.getCount();
            }
        }
        return result;
    }

    /**
     * 解析折扣消耗
     * @param consumeIntArr
     * @param discount 原价的百分之X
     * @return
     */
    public static Consume parseDisCountConsume(int[] consumeIntArr, int discount) {
        if (consumeIntArr == null || consumeIntArr.length < 3) {
            LogUtil.error("ConsumeUtil.parseDisCountConsume, error param");
            return null;
        }
        return parseConsume(consumeIntArr[0], consumeIntArr[1], consumeIntArr[2] * discount / 100);
    }

    public static List<Consume> mergeConsume(List<Consume> consumeList) {
        if (CollectionUtils.isEmpty(consumeList)) {
            return Collections.emptyList();
        }


        List<Consume.Builder> ConsumeBuilder = new ArrayList<>();
        boolean contain;
        for (Consume consume : consumeList) {
            if (consume == null) {
                continue;
            }
            contain = false;
            for (Consume.Builder builder : ConsumeBuilder) {
                if (consume.getRewardType() == builder.getRewardType() && consume.getId() == builder.getId()) {
                    builder.setCount(builder.getCount() + consume.getCount());
                    contain = true;
                }
            }
            if (!contain) {
                ConsumeBuilder.add(consume.toBuilder());
            }
        }

        List<Consume> Consumes = new ArrayList<>();
        for (Consume.Builder builder : ConsumeBuilder) {
            Consumes.add(builder.build());
        }
        return Consumes;
    }
}
